package com.vanessamacisaac.navigation;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
