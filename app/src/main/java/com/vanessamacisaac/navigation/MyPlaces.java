package com.vanessamacisaac.navigation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.IOException;


public class MyPlaces extends ActionBarActivity {

    private static final String TAG = "MyPlaces";

    private SimpleCursorAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_places);

        displayAddress();
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

    public void displayAddress(){

        Log.e(TAG, "** displayAddress");
        DatabaseHandler myDBH = new DatabaseHandler(this);


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

        //String addr = myDBH.getFirst();
        Cursor cursor = myDBH.fetchAllPlaces();
        myDBH.close();

        // colums to be bound
        String[] columns = new String[] {
                DatabaseHandler.KEY_NAME,
                DatabaseHandler.KEY_ADDR,
        };

        // XML to be bound to
        int[] to = new int[] {
                R.id.itemTitle,
                R.id.secondLine
        };

        // create adapter with the cursor pointing to the layout components
        dataAdapter = new SimpleCursorAdapter(
                this, R.layout.places_info,
                cursor,
                columns,
                to,
                0);

        // set the listview with adapter
        ListView listView = (ListView) findViewById(R.id.listView1);
        listView.setAdapter(dataAdapter);

        final int[] imgIds = new int[]{ R.drawable.marker, R.drawable.home, R.drawable.nature,
                R.drawable.work, R.drawable.entertainment, R.drawable.bank,
                R.drawable.restaurant, R.drawable.grocery, R.drawable.shopping,
                R.drawable.dentist, R.drawable.health, R.drawable.friend
        };

        View v = listView.getChildAt(0 - listView.getFirstVisiblePosition());
        int vcount = listView.getChildCount();
        int dataCount = dataAdapter.getCount();
        
        Log.e("TAG", "# of children = " + dataCount);
        //ImageView img = (ImageView) v.findViewById(R.id.icon);
        //img.setImageResource(R.drawable.home);
        //img.setImageDrawable(R.drawable.home);



        // get ID of item clicked in listView
        AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id){
                // var id corresponds to _id in database
                Context context = getApplicationContext();
                CharSequence text = "item " + id + " was selected!" + "\n" + "pos " + position + " was selected!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                //toast.show();
                Log.v("Click", "item at pos: " + position);

                // TODO
                // CALL DIRECTIONS CLASS
                // PASS ID TO CLASS
                // START DIRECTIONS
                sendDirections(id);

            }
        };

        listView.setOnItemClickListener(mMessageClickedHandler);

        registerForContextMenu(listView);

    }

    private void sendDirections(long id) {
        int myID = (int) id;
        Intent i = new Intent(this, Directions.class);
        i.putExtra("PLACE_ID", myID);
        startActivity(i);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        int infoPos = info.position;
        menu.setHeaderTitle("Options " + infoPos);


        // inflate the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        int infoPosNum = info.position;
        int infoItemNum = (int) info.id;

        switch(item.getItemId()){
            case R.id.menuEdit:
                // do something
                Log.v("Context menu option", "Edit selected, pos " + infoPosNum);
                editPlace(infoItemNum);
                return true;
            case R.id.menuDelete:
                // do something
                Log.v("Context menu option", "Delete selected, pos " + infoPosNum);
                deletePlace(infoPosNum, infoItemNum);
                return true;
            case R.id.menuCancel:
                // do nothing
                Log.v("Context menu option", "Cancel selected, pos " + infoPosNum);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void deletePlace(int posNum, int itemNum){
        // delete the place from the database
        Log.v("Delete Place", "Id = " + posNum);

        int itemID = posNum;
        final int itemDBNum = itemNum;
        // create var for database position
        // Primary Key Integer in database starts at 1, not 0, so add 1
        //final int dbPos = itemID + 1;

        // confirm deletion
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Delete");
        alertDialog.setMessage("Are you sure you want to delete this place?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Log.v("Alert dialog", "user clicked yes");
                // DELETE THE PLACE
                // call DB method to delete
                deletePlaceFromDB(itemDBNum);
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Log.v("Alert dialog", "user clicked cancel");
            }
        });

        alertDialog.show();

    }

    public void editPlace(int infoItemNum){
        // TODO
        //Display activity with data from database to edit
        Log.v("Edit Place", "editing place " + infoItemNum);
        // FIX BACK
        //Intent intent = new Intent(this,MainActivity.class);
        Intent intent = new Intent(this, EditPlace.class);
        intent.putExtra("ITEM_NUM", infoItemNum);
        startActivity(intent);

    }

    public void deletePlaceFromDB(int position){
        DatabaseHandler myDBHandler = new DatabaseHandler(this);
        myDBHandler.deletePlace(position);
        myDBHandler.close();
        Log.v("Database modified", "Place with _id = " + position + " deleted");
        displayAddress();
    }
}
