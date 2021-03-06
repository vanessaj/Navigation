package com.vanessamacisaac.navigation;

import android.database.Cursor;
import android.database.SQLException;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;


public class Directions extends ActionBarActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "Directions";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 3000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mCurrentLocation;

    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mDirectionsInfo;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    protected Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    protected String mLastUpdateTime;

    // DB ID for destination
    protected int destinationID;

    // JSONarray for steps
    protected JSONArray mSteps;
    protected int stepNum;

    protected Boolean checkedDirections = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        // get bundle extras
        Bundle extras = getIntent().getExtras();
        if(extras != null){

            destinationID = extras.getInt("PLACE_ID");

            //destinationID = extras.getInt("PLACE_ID");
            Log.e(TAG, "Place ID = " + destinationID);
        }
        else{
            Toast.makeText(this, "ERROR: No destination specified from main menu!",
                    Toast.LENGTH_SHORT).show();
        }

        // Locate the UI widgets.
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        //mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        //mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mDirectionsInfo = (TextView) findViewById(R.id.directionsRequest);

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();
        //startLocationUpdates();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        Log.i(TAG, "Updating values from bundle");
        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();




        }
    }



    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    /**
     * Handles the Stop Updates button, and requests removal of location updates. Does nothing if
     * updates were not previously requested.
     */
    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    public void recalculateHandler(View view){
        Toast.makeText(this, "Recalculating directions!", Toast.LENGTH_SHORT).show();
        getDirections();
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        if (mCurrentLocation != null) {
           // mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
           // mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
            getAddress();
            // current lng lat
            // destination lng lat

            //TODO
            // check directions
            if(!checkedDirections){
                getDirections();
            }
            //TODO
            // check current journey progress
            if(mSteps!=null && stepNum <= (mSteps.length()-1)){
                JSONObject currentStep = null;
                try {
                        Log.e(TAG, "getting info for step # " + stepNum);
                        Log.e(TAG, "# of steps = " + mSteps.length());
                        currentStep = mSteps.getJSONObject(stepNum);
                        // calculate start and end point difference
                        String instructions = currentStep.get("html_instructions").toString();
                        instructions = instructions.replaceAll("\\<.*?>"," ");
                        String start = currentStep.get("start_location").toString();
                        JSONObject startLoc = currentStep.getJSONObject("start_location");
                        Double startLat = (Double) startLoc.get("lat");
                        Double startLng = (Double )startLoc.get("lng");
                        //mDirectionsInfo.setText(instructions + "\n" + "Lat : " + startLat + " Lng : " + startLng);
                        mDirectionsInfo.setText("" + instructions);
                        if(stepNum == (mSteps.length()-1)){
                            Toast.makeText(this, "REACHED DESTINATION!", Toast.LENGTH_SHORT).show();
                            stopLocationUpdates();
                            if (mRequestingLocationUpdates) {
                                mRequestingLocationUpdates = false;
                                setButtonsEnabledState();
                                //stopLocationUpdates();
                            }
                        }
                        Double currLat = mCurrentLocation.getLatitude();
                        Double currLng = mCurrentLocation.getLongitude();
                        // Check if reached start location of next step
                        double currDist = checkDistance(currLat, currLng, startLat, startLng);
                        // if <= 5 m next step, else keep going forward
                        TextView distTV = (TextView) findViewById(R.id.distance);
                        int currDistOutput = (int) currDist;
                        distTV.setText("" + currDistOutput);
                        if(currDist <= 8){
                            stepNum = stepNum + 1;
                        }
                        else{
                            // continue fwd
                        }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public double checkDistance(double currLat, double currLng, double destLat, double destLng){
        double dist;
        // calculate distance between coordinates using spherical geometry
        double radius = 6378.137; // Radius of earth in KM
        double dLat = (destLat - currLat) * Math.PI / 180;
        double dLon = (destLng - currLng) * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(currLat * Math.PI / 180) * Math.cos(destLat * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        dist = radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)) * 1000;

        return dist;
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        // If the user presses the Start Updates button before GoogleApiClient connects, we set
        // mRequestingLocationUpdates to true (see startUpdatesButtonHandler()). Here, we check
        // the value of mRequestingLocationUpdates and if it is true, we start location updates.
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        /*Toast.makeText(this, getResources().getString(R.string.location_updated_message),
                Toast.LENGTH_SHORT).show();*/
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_directions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getAddress(){
        double mLat = mCurrentLocation.getLatitude();
        double mLon = mCurrentLocation.getLongitude();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+mLat+","+mLon+"&key=AIzaSyB--SRzc9zZHAq7Y0Ln7iNC5JK3Tp1S4I0";
        new RequestCurrentAddress().execute(url);
    }

    // Request current address using http request
    class RequestCurrentAddress extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            TextView mTV = (TextView) findViewById(R.id.currentAddress);
            mTV.setText(result);
            mTV.setMovementMethod(new ScrollingMovementMethod());
            // Log.v(TAG, result);
            try{
                // get JSON object returned from request
                JSONObject json = new JSONObject(result);
                JSONArray resultsArray = json.getJSONArray("results");
                JSONObject rObj = resultsArray.getJSONObject(0);
                String addr = rObj.getString("formatted_address");
                //JSONObject resObj = resultsArray.getJSONObject(0);
                //String addr = json.getString("formatted_address");
                Log.e(TAG, "** trial **: " + addr);

                mTV.setText(addr);
            } catch (Exception e) {
                Log.v(TAG, "JSON failed");
            }

        }


    }

    public void getDirections(){
        checkedDirections = true;
        double mLat = mCurrentLocation.getLatitude();
        double mLon = mCurrentLocation.getLongitude();
        // lookup destination coordinates
        DatabaseHandler myDBH = new DatabaseHandler(this);
        try {
            myDBH.createDatabase();
        } catch (IOException ioe) {
            Log.e(TAG, "unable to create database");
            throw new Error("Unable to create database");
        }
        try {
            myDBH.openDatabase();
        }catch(SQLException sqle){
            throw sqle;}

        Cursor placeInfo = myDBH.fetchInfoFromID(destinationID);
        myDBH.close();



        if(placeInfo!=null){
            placeInfo.moveToFirst();
            Double destLat = placeInfo.getDouble(placeInfo.getColumnIndex("latitude"));
            Double destLon = placeInfo.getDouble(placeInfo.getColumnIndex("longitude"));
            Log.e(TAG, "** Destination lat = " + destLat);
            Log.e(TAG, "** Destination lon = " + destLon);
            TextView destTV = (TextView) findViewById(R.id.destinationName);
            destTV.setText(placeInfo.getString(placeInfo.getColumnIndex("name")));
            String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+mLat+","+mLon+"&destination="+destLat+","+destLon+"&mode=walking&key=AIzaSyB--SRzc9zZHAq7Y0Ln7iNC5JK3Tp1S4I0";
            Log.e(TAG, url);
            new RequestDirections().execute(url);
        }
        else{
            Log.e(TAG, "Error getting info on destination");
        }

    }

    // Request directions using http request
    class RequestDirections extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
            mDirectionsInfo.setText(result);
            mDirectionsInfo.setMovementMethod(new ScrollingMovementMethod());
            // Log.v(TAG, result);
            try{
                // get JSON object returned from request
                JSONObject json = new JSONObject(result);
                JSONArray routesArray = json.getJSONArray("routes");
                JSONObject rObj = routesArray.getJSONObject(0);
                JSONArray legsArray = rObj.getJSONArray("legs");
                JSONObject lObj = legsArray.getJSONObject(0);
                JSONArray steps = lObj.getJSONArray("steps");
                mSteps = steps;
                stepNum = 0;
                JSONObject currentStep = mSteps.getJSONObject(stepNum);
                String instructions = currentStep.get("html_instructions").toString();
                String start = currentStep.get("start_location").toString();
                JSONObject startLoc = currentStep.getJSONObject("start_location");
                Double startLat = (Double) startLoc.get("lat");
                Double startLng = (Double )startLoc.get("lng");
                instructions = instructions.replaceAll("\\<.*?>","");
                mDirectionsInfo.setText("" + instructions);
                //mDirectionsInfo.setText(instructions + "\n" + "Lat: " + startLat + " Lng: " + startLng);
                //mDirectionsInfo.setText(currentStep.toString());

            } catch (Exception e) {
                Log.v(TAG, "JSON Failed");
            }

        }


    }
}
