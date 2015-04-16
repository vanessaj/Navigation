package com.vanessamacisaac.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class TryBluetooth extends ActionBarActivity {

    private static final String TAG = "TryBluetooth";
    // WIT var
    //Wit _wit;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // Well known SPP UUID
    private static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Insert your server's MAC address
    //private static String address = "00:12:07:13:42:46";
    private static String address = "00:12:09:12:00:26";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_try_bluetooth);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
        //signals();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_try_bluetooth, menu);
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


    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (outStream != null) {
            try {
                outStream.flush();
            } catch (IOException e) {
                errorExit("Fatal Error", "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
            }
        }

        try {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        // Need Mac address and A Service ID or UUID
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            if (btSocket != null) {
                Log.e(TAG, "** Btsocket is NOT null :D ");
            }
        } catch (IOException e) {
            errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        Log.e(TAG, "...Connecting to Remote...");
        try {
            Log.e(TAG, "...Connection established and data link opened...");
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
            Log.e(TAG, "*** DOES IT REACH HERE ??? getting output stream****");
            if (outStream != null) {
                Log.e(TAG, "** outStream is not null");
            }
        } catch (IOException e) {
            Log.e(TAG, "**In onResume() an output stream creation failed " + e.getMessage() + ".");
            errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
//        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        //      savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        //    savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void rightBT(View view) {
        checkBTState();
        sendData("R");
        //Toast msg = Toast.makeText(getBaseContext(),"You have clicked On", Toast.LENGTH_SHORT);
        //msg.show();
    }

    public void leftBT(View view) {
        checkBTState();
        sendData("L");
        //Toast msg = Toast.makeText(getBaseContext(),"You have clicked On", Toast.LENGTH_SHORT);
        //msg.show();
    }


    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter == null) {
            errorExit("Fatal Error", "Bluetooth Not supported. Aborting.");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth is enabled...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(btAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

    }

    private void errorExit(String title, String message) {
        Toast msg = Toast.makeText(getBaseContext(),
                title + " - " + message, Toast.LENGTH_SHORT);
        msg.show();
        finish();
    }

    private void sendData(String message) {
        Log.e(TAG, "sendData !!!!");
        //message = message + "\r";
        Log.e(TAG, "" + message);
        byte[] msgBuffer = message.getBytes();


        Log.d(TAG, "...Sending data: " + message + "...");

        //btSocket
        Log.e(TAG, "** CHECK if BTSOCKET is connected? ** : " + btSocket.isConnected());

        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        if (!btSocket.isConnected()) {
            Log.e(TAG, "** not connected, trying to connect");

            if (device.getBondState() == device.BOND_BONDED) {
                Log.d(TAG, device.getName());
                //BluetoothSocket mSocket=null;
                try {
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    Log.d(TAG, "socket not created");
                    e1.printStackTrace();
                }
                try {
                    btSocket.connect();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                        Log.d(TAG, "Cannot connect");
                    } catch (IOException e1) {
                        Log.d(TAG, "Socket not closed");
                        e1.printStackTrace();
                    }
                }

                try {
                    if (outStream != null) {
                        Log.e(TAG, "** outstream isn't null");
                    }
                    device = btAdapter.getRemoteDevice(address);
                    //btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);


                    if (btSocket != null) {
                        Log.e(TAG, "** Btsocket is NOT null :D ");
                    }
                    Thread.sleep(10000);
                    btSocket.connect();
                    Log.e(TAG, "*about to sleep");
                    Thread.sleep(2000);
                    Log.e(TAG, "...Connection established and data link opened...");
                } catch (IOException e) {
                    try {
                        Log.e(TAG, "Connecting threw an IO Exception: " + e.getMessage());
                        btSocket.close();
                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Create a data stream so we can talk to server.
                Log.e(TAG, "...Creating Socket...");
                Log.e(TAG, "* Connected ?" + btSocket.isConnected());

                try {
                    outStream = btSocket.getOutputStream();
                    Log.e(TAG, "*** Trying to get output stream ****");
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onResume() and output stream creation failed:" + e.getMessage() + ".");
                }
            }
            // TRY CATCH BLOCK
            try {
                Log.e(TAG, "** ABOUT TO WRITE ?? ** ");
                Log.e(TAG, "** CHECK if BTSOCKET is connected? ** : " + btSocket.isConnected());
                outStream.write(msgBuffer);
            } catch (IOException e) {
                String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
                if (address.equals("00:00:00:00:00:00"))
                    msg = msg + ".\n\nUpdate your server address from 00:00:00:00:00:00 to the correct address on line 37 in the java code";
                msg = msg + ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";

                errorExit("Fatal Error", msg);
            }
            String msg = msgBuffer.toString();
            Log.e(TAG, "** THE MESSAGE = " + msg);
            Log.e(TAG, "*** IT SHOULD HAVE WRITTEN HERE");
        }
    }

}
