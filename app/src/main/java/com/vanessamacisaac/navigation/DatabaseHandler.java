package com.vanessamacisaac.navigation;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by vanessamacisaac on 15-04-16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHandler";

    public static final String KEY_ROWID = "_id";
    public static final String KEY_PICID = "pic_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDR = "address";
    public static final String KEY_LON = "longitude";
    public static final String KEY_LAT = "latitude";
    public static final String KEY_REF = "refid";
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.vanessamacisaac.navigation/databases/";

    private static String DB_NAME = "navapp.db";

    private SQLiteDatabase myDatabase;
    public SQLiteDatabase myData;

    private final Context myContext;

    private static final String DB_TABLE_PLACES = "places";
    private static final String DB_TABLE_FAVS = "favourites";

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabase();
        if(dbExist){
            //do nothing - db already exists
            Log.e(TAG, "** db already exists");
        }else{
            //create db to overwrite
            Log.e(TAG, "** create db to overwrite");
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (IOException e) {
                Log.e(TAG, "** error copying db");
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exists to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDatabase(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){
            //database does't exist yet.
        }

        if(checkDB != null){
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring the bytestream.
     * */
    private void copyDatabase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDatabase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    @Override
    public synchronized void close() {
        if(myDatabase != null){
            myDatabase.close();
            super.close();
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            copyDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        openDatabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE_PLACES);
        db.execSQL("DRPOP TABLE IF EXISTS " + DB_TABLE_FAVS);
        onCreate(db);
    }



  /*
   * PUBLIC HELPER CRUD METHODS
   */

    public String getFirst(){
        String result = null;
        Cursor mcursor = null;

        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");
        mcursor = myData.rawQuery("SELECT * FROM places WHERE _id = 1", null);

        if(mcursor != null){
            mcursor.moveToFirst();
            int addr = mcursor.getColumnIndex("address");
            result = mcursor.getString(addr);
        }
        else{
            result = "Error, empty cursor!";
        }

        return result;
    }

    public Cursor fetchAllPlaces(){

        Cursor placesC = null;

        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");
        placesC = myData.query(DB_TABLE_PLACES, new String[] {KEY_ROWID, KEY_PICID, KEY_NAME, KEY_ADDR, KEY_LON, KEY_LAT}, null, null, null, null, null, null);

        return placesC;
    }

    // parameter is a picture ID
    // pic ID comes from homescreen click
    // looks up the picID and finds the refID (the id in places table)
    // the refID is then looked up in the places table and result is returned as a cursor
    public Cursor fetchPicforId(String pic){
        Cursor pictRef = null;
        Cursor picture = null;

        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS favourites (_id INTEGER, refid INTEGER)");
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");

        pictRef = myData.rawQuery("SELECT refid FROM favourites WHERE _id = ?", new String[]{pic});

        if(pictRef != null){
            pictRef.moveToFirst();
            int refCol = pictRef.getColumnIndex("refid");
            int original = pictRef.getInt(refCol);
            String str = Integer.toString(original);
            picture = myData.rawQuery("SELECT * FROM places WHERE _id = ?", new String[]{str});
        }

        return picture;
    }

    // TODO
    public int fetchLatForId(String pic){
        Cursor c = null;

        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");

        //c = myData.rawQuery("SELECT latitude FROM places WHERE ", selectionArgs)

        return 1;
    }

    public void addPlace(String name, String address, String lat, String lon){
        //getWritableDatabase();
        int pic = 0;

        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");
        myData.execSQL("INSERT INTO places (pic_id, name, address, longitude, latitude) VALUES (?, ?, ?, ?, ?)", new Object[]{pic, name, address, lon, lat});
    }

    public void deletePlace(int placeID){
        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");
        myData.execSQL("DELETE FROM places WHERE _id = ?", new Object[]{placeID});
    }

    public Cursor fetchInfoFromID(int placeID){
        Cursor c = null;
        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");

        String place = Integer.toString(placeID);

        c = myData.rawQuery("SELECT * FROM places WHERE _id = ?", new String[]{place});
        return c;

    }

    public void updatePlace(String name, String address, String lat, String lon, String placeID){
        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS places (_id INTEGER, pic_id INTEGER, name TEXT, address TEXT, longitude REAL, latitude REAL)");

        myData.execSQL("UPDATE places SET name = ?, address = ?, latitude = ?, longitude = ? WHERE _id = ?", new String[]{name, address, lat, lon, placeID});
    }

    public Cursor getFavPlaces(){
        Cursor c = null;
        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS favourites (_id INTEGER, refid INTEGER)");

        c = myData.rawQuery("SELECT * FROM favourites", null);
        return c;
    }

    public Cursor favLookup(int favPosition){
        String pos = Integer.toString(favPosition);
        Cursor c = null;
        String myPath = DB_PATH + DB_NAME;
        myData = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        myData.compileStatement("CREATE TABLE IF NOT EXISTS favourites (_id INTEGER, refid INTEGER)");

        c = myData.rawQuery("SELECT refid FROM favourites WHERE _id = ?", new String[]{pos});
        return c;
    }
}
