package com.vanessamacisaac.navigation;

import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.IOException;


public class FavouritePlaces extends ActionBarActivity {

    protected static final String TAG = "FavouritePlaces";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_places);

        setSpinners();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favourite_places, menu);
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

    public void setSpinners(){

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

        // get cursors for 4 favourite places
        Cursor cFav1 = myDBH.favLookup(1);
        Cursor cFav2 = myDBH.favLookup(2);
        Cursor cFav3 = myDBH.favLookup(3);
        Cursor cFav4 = myDBH.favLookup(4);

        myDBH.close();

        // get places from cursors
        if(cFav1!=null){
            cFav1.moveToFirst();
            int place1 = cFav1.getInt(cFav1.getColumnIndex("refid"));

           // int place1 = cFav1.getInt(cFav1.getColumnIndex("refid"));
            // set it in the spinner
            Spinner categories = (Spinner) findViewById(R.id.spinner1);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.pic_categories, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories.setAdapter(adapter);
            categories.setSelection(place1);
        }

        if(cFav2!=null) {
            cFav2.moveToFirst();
            int place2 = cFav2.getInt(cFav2.getColumnIndex("refid"));
            Spinner categories2 = (Spinner) findViewById(R.id.spinner2);
            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                    R.array.pic_categories, android.R.layout.simple_spinner_item);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories2.setAdapter(adapter2);
            categories2.setSelection(place2);
        }

        if(cFav3!=null) {
            cFav3.moveToFirst();
            int place3 = cFav3.getInt(cFav3.getColumnIndex("refid"));
            Spinner categories3 = (Spinner) findViewById(R.id.spinner3);
            ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,
                    R.array.pic_categories, android.R.layout.simple_spinner_item);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories3.setAdapter(adapter3);
            categories3.setSelection(place3);
        }

        if(cFav4!=null) {
            cFav4.moveToFirst();
            int place4 = cFav4.getInt(cFav4.getColumnIndex("refid"));
            Spinner categories4 = (Spinner) findViewById(R.id.spinner4);
            ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this,
                    R.array.pic_categories, android.R.layout.simple_spinner_item);
            adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categories4.setAdapter(adapter4);
            categories4.setSelection(place4);
        }

    }

    public void updateFavs(View view){

        // get references to spinners
        Spinner categories = (Spinner) findViewById(R.id.spinner1);
        Spinner categories2 = (Spinner) findViewById(R.id.spinner2);
        Spinner categories3 = (Spinner) findViewById(R.id.spinner3);
        Spinner categories4 = (Spinner) findViewById(R.id.spinner4);

        // find selections from spinners
        int fav1 = categories.getSelectedItemPosition();
        int fav2 = categories2.getSelectedItemPosition();
        int fav3 = categories3.getSelectedItemPosition();
        int fav4 = categories4.getSelectedItemPosition();

        // write to DB
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

        myDBH.updateFav(fav1, 1);
        myDBH.updateFav(fav2, 2);
        myDBH.updateFav(fav3, 3);
        myDBH.updateFav(fav4, 4);
        myDBH.close();

    }
}
