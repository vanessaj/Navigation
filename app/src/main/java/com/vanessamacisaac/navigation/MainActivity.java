package com.vanessamacisaac.navigation;

import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;


public class MainActivity extends ActionBarActivity {

    protected static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO
        // set imgs for favourites
        // set text as well
        setFavImages();
    }

    private void setFavImages() {
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

        String fav1 = Integer.toString(1);
        String fav2 = Integer.toString(2);
        String fav3 = Integer.toString(3);
        String fav4 = Integer.toString(4);
        Cursor c1 = myDBH.fetchPicforId(fav1);
        Cursor c2 = myDBH.fetchPicforId(fav2);
        Cursor c3 = myDBH.fetchPicforId(fav3);
        Cursor c4 = myDBH.fetchPicforId(fav4);

        final int[] imgIds = new int[]{ R.drawable.marker, R.drawable.home, R.drawable.nature,
                R.drawable.work, R.drawable.entertainment, R.drawable.bank,
                R.drawable.restaurant, R.drawable.grocery, R.drawable.shopping,
                R.drawable.dentist, R.drawable.health, R.drawable.friend
        };

        if(c1!=null){
            c1.moveToFirst();
            int pic_id = c1.getInt(c1.getColumnIndex("pic_id"));
            String name = c1.getString(c1.getColumnIndex("name"));
            Button b = (Button) findViewById(R.id.fav1_button);
            b.setCompoundDrawablesWithIntrinsicBounds(0, imgIds[pic_id], 0, 0);
            b.setPadding(0, 64, 0, 0);
            b.setText(name);
            //ib.setImageResource(imgIds[pic_id]);
        }
        if(c2!=null){
            c2.moveToFirst();
            int pic_id = c2.getInt(c2.getColumnIndex("pic_id"));
            String name = c2.getString(c2.getColumnIndex("name"));
            Button b = (Button) findViewById(R.id.fav2_button);
            b.setCompoundDrawablesWithIntrinsicBounds(0, imgIds[pic_id], 0, 0);
            b.setPadding(0, 64, 0, 0);
            b.setText(name);
            //ib.setImageResource(imgIds[pic_id]);
        }
        if(c3!=null){
            c3.moveToFirst();
            int pic_id = c3.getInt(c3.getColumnIndex("pic_id"));
            String name = c3.getString(c3.getColumnIndex("name"));
            Button b = (Button) findViewById(R.id.fav3_button);
            b.setCompoundDrawablesWithIntrinsicBounds(0, imgIds[pic_id], 0, 0);
            b.setPadding(0, 64, 0, 0);
            b.setText(name);
            //ib.setImageResource(imgIds[pic_id]);
        }
        if(c4!=null){
            c4.moveToFirst();
            int pic_id = c4.getInt(c4.getColumnIndex("pic_id"));
            String name = c4.getString(c4.getColumnIndex("name"));
            Button b = (Button) findViewById(R.id.fav4_button);
            b.setCompoundDrawablesWithIntrinsicBounds(0, imgIds[pic_id], 0, 0);
            b.setPadding(0, 64, 0, 0);
            b.setText(name);
            //ib.setImageResource(imgIds[pic_id]);
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

    public void clickTL(View view){
        // top left button clicked
        //Intent i = new Intent(this, Directions.class);
        //startActivity(i);
        startDirections(1);
    }

    public void clickTR(View view){
        // top right button clicked
        //Intent i = new Intent(this, DeviceListActivity.class);
        //startActivity(i);
        startDirections(2);
    }

    public void clickBL(View view){
        // bottom left button clicked
        //Intent i = new Intent(this, SendDirections.class);
        //startActivity(i);
        startDirections(3);
    }

    public void clickBR(View view){
        // bottom right button clicked
        //Intent i = new Intent(this, TryBluetooth.class);
        //startActivity(i);
        startDirections(4);
    }

    public void startDirections(int placeID){
        Intent i = new Intent(this, Directions.class);
        i.putExtra("PLACE_ID", placeID);
        startActivity(i);
    }


    public void myPlacesClick(View view){
        Intent i = new Intent(this, MyPlaces.class);
        startActivity(i);
    }



}
