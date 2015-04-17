package com.vanessamacisaac.navigation;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;


public class EditPlace extends ActionBarActivity {

    int chosenPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);

        Bundle extras;
        int chosenItem = -1;
        if (savedInstanceState == null) {
            extras = getIntent().getExtras();
            if(extras == null) {
                chosenItem= -1;
            } else {
                chosenItem = extras.getInt("ITEM_NUM");
            }
        } else {
            //chosenItem = (int) savedInstanceState.getSerializable("ITEM_NUM");
        }
        //int chosenItem = 2;
        chosenPlace = chosenItem;

        showCurrInfo(chosenItem);
    }

    public void showCurrInfo(int chosenItem){

        // get references to all the EditText in the Edit activity
        EditText nameBox = (EditText)findViewById(R.id.edit_name);
        EditText addBox = (EditText)findViewById(R.id.edit_address);
        EditText latBox = (EditText)findViewById(R.id.edit_lat);
        EditText lonBox = (EditText)findViewById(R.id.edit_lon);

        // make a copy of the database handler
        DatabaseHandler myDBH = new DatabaseHandler(this);
        // open the database
        try {
            myDBH.createDatabase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }
        try {
            myDBH.openDatabase();
        }catch(SQLException sqle){
            throw sqle;
        }

        // create vars for edittext
        String nameText = "";
        String addrText = "";
        String latText = "";
        String lonText = "";
        Double dLat = 0.0;
        Double dLon = 0.0;

        // get info for chosenItem, passed from MyPlaces Activity
        Cursor mCursor = myDBH.fetchInfoFromID(chosenItem);
        if (mCursor != null){
            mCursor.moveToFirst();
            int nameCol = mCursor.getColumnIndex("name");
            int addCol = mCursor.getColumnIndex("address");
            int latCol = mCursor.getColumnIndex("latitude");
            int lonCol = mCursor.getColumnIndex("longitude");
            nameText = mCursor.getString(nameCol);
            addrText = mCursor.getString(addCol);
            dLat = mCursor.getDouble(latCol);
            dLon = mCursor.getDouble(lonCol);
        }

        latText = Double.toString(dLat);
        lonText = Double.toString(dLon);

        nameBox.setText(nameText);
        addBox.setText(addrText);
        latBox.setText(latText);
        lonBox.setText(lonText);

    }

    public void formValidate(View view) {
        EditText name = (EditText)findViewById(R.id.edit_name);
        EditText address = (EditText)findViewById(R.id.edit_address);
        EditText lat = (EditText)findViewById(R.id.edit_lat);
        EditText lon = (EditText)findViewById(R.id.edit_lon);

        String nameText = name.getText().toString();
        String addressText = address.getText().toString();
        String latText = lat.getText().toString();
        String lonText = lon.getText().toString();

        String LAT_REGEX = "(\\-)?\\d{1,2}(.\\d*)?";
        String LON_REGEX = "(\\-)?\\d{1,3}(.\\d*)?";


        if(nameText.length()== 0){
            name.setError("Place name is required!");
        }
        if(addressText.length()==0){
            address.setError("Address is required!");
        }
        if(latText.length()==0){
            lat.setError("Latitude is required!");
        }
        if(lonText.length()==0){
            lon.setError("Longitude is required!");
        }
        else{
            if(!latText.matches(LAT_REGEX)){
                lat.setError("Latitude is invalid format!");
            }
            if(!lonText.matches(LON_REGEX)){
                lon.setError("Longitude is invalid format!");
            }
            else{

                double latValue = Double.parseDouble(latText);
                double lonValue = Double.parseDouble(lonText);

                if((latValue<-90) || (latValue>90)){
                    lat.setError("Latitude is invalid value!");
                }
                if((lonValue<-180) || (lonValue>180)){
                    lon.setError("Longitude is invalid value!");
                }

                else{
                    updatePlaceDB(nameText, addressText, latText, lonText);
                }

            }
        }

    }

    public void updatePlaceDB(String name, String address, String lat, String lon){
        DatabaseHandler myDBH = new DatabaseHandler(this);

        String place = Integer.toString(chosenPlace);

        myDBH.updatePlace(name, address, lat, lon, place);

        // vars for retrieved db data
        String rName = "";
        String rAddr = "";
        String rLat = "";
        String rLon = "";
        Double dLat = 0.0;
        Double dLon = 0.0;

        Cursor c = myDBH.fetchInfoFromID(chosenPlace);
        if (c != null){
            c.moveToFirst();
            int nameCol = c.getColumnIndex("name");
            int addrCol = c.getColumnIndex("address");
            int latCol = c.getColumnIndex("latitude");
            int lonCol = c.getColumnIndex("longitude");
            rName = c.getString(nameCol);
            rAddr = c.getString(addrCol);
            dLat = c.getDouble(latCol);
            dLon = c.getDouble(lonCol);
        }
        else{
            Log.v("Update DB", "place ID does not exist in DB");
        }

        rLat = Double.toString(dLat);
        rLon = Double.toString(dLon);

        // compare if data in DB is same as what was entered by user for update
        if(name.equals(rName) && address.equals(rAddr) && lat.equals(rLat) && lon.equals(rLon)){
            Log.v("Update DB", "Data matches, update successful");

            // show toast to confirm update
            Context context = getApplicationContext();
            CharSequence text = rName + " was updpated!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();

            // go back to MyPlaces Activity
            Intent intent = new Intent(EditPlace.this, MyPlaces.class);
            startActivity(intent);

        }
        // if the data does not match there is an error ...
        else{
            Log.v("Update DB", "Data DOES NOT MATCH, ERROR");
            Log.v("Updates DB", "Name " + rName);
            Log.v("Updates DB", "Addr " + rAddr);
            Log.v("Updates DB", "Lat " + rLat);
            Log.v("Updates DB", "Lon " + rLon);

            Context context = getApplicationContext();
            CharSequence text = "Uh oh, something went wrong.\n" + rName + "did not update" ;
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_add_place:
                Intent newPlace = new Intent (this, AddMyPlace.class);
                startActivity(newPlace);
                break;

            case R.id.menu_home:
                Intent newHome = new Intent (this, MainActivity.class);
                startActivity(newHome);
                break;

            case R.id.menu_device_list:
                Intent deviceList = new Intent(this, DeviceListActivity.class);
                startActivity(deviceList);
                break;

            default:
                break;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
