package com.vanessamacisaac.navigation;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class AddMyPlace extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_my_place);

        Spinner categories = (Spinner) findViewById(R.id.pic_picker);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.pic_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(adapter);
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

    public void formValidate(View view) {
        EditText name = (EditText)findViewById(R.id.add_name);
        EditText address = (EditText)findViewById(R.id.add_address);
        EditText lat = (EditText)findViewById(R.id.add_lat);
        EditText lon = (EditText)findViewById(R.id.add_lon);
        Spinner cat = (Spinner)findViewById(R.id.pic_picker);

        String nameText = name.getText().toString();
        String addressText = address.getText().toString();
        String latText = lat.getText().toString();
        String lonText = lon.getText().toString();
        int pic = cat.getSelectedItemPosition();

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
                    addPlaceDB();
                }

            }
        }

    }

    public void addPlaceDB(){
        DatabaseHandler myDBH = new DatabaseHandler(this);

        EditText name = (EditText)findViewById(R.id.add_name);
        EditText address = (EditText)findViewById(R.id.add_address);
        EditText lat = (EditText)findViewById(R.id.add_lat);
        EditText lon = (EditText)findViewById(R.id.add_lon);
        Spinner cat = (Spinner)findViewById(R.id.pic_picker);

        String nameText = name.getText().toString();
        String addressText = address.getText().toString();
        String latText = lat.getText().toString();
        String lonText = lon.getText().toString();
        int pic = cat.getSelectedItemPosition();
        String picID = Integer.toString(pic);

        myDBH.addPlace(picID, nameText, addressText, latText, lonText);
        myDBH.close();

        name.setText("");
        address.setText("");
        lat.setText("");
        lon.setText("");

        Context context = getApplicationContext();
        CharSequence text = nameText + " was added!";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
