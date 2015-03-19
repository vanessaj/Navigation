package com.vanessamacisaac.navigation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;


public class SendDirections extends ActionBarActivity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BtSPPHelper btSPPHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.activity_send_directions);

        // Get the Bluetooth adapter, only one for now
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //btSPPHelper = new BtSPPHelper(this, );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_directions, menu);
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
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (btSPPHelper == null) setupLink();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (btSPPHelper != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (btSPPHelper.getState() == BtSPPHelper.State.NONE) {
                // Start the Bluetooth chat services
                btSPPHelper.start();
            }
        }
    }


    private void setupLink() {
        Log.d(TAG, "set up a link");



        // Initialize the send button with a listener that for click events
        Button rightButton = (Button) findViewById(R.id.send_right);
        rightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                //TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = "R";
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        btSPPHelper = new BtSPPHelper(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the helper threads
        if (btSPPHelper != null) btSPPHelper.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private void sendMessage(String message) {
        if(D) Log.e(TAG, "++ Send Message ++");

        if(btSPPHelper == null){
            Log.e(TAG, "*** btSPPHelper is null");
        }
        /*if (btSPPHelper.getState() != BtSPPHelper.State.CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if(D) Log.e(TAG, "++ about to write ++");
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            btSPPHelper.write(send);

            mOutStringBuffer.setLength(0);
            //mOutEditText.setText(mOutStringBuffer);
        }*/
    }

    // The Handler that gets information back from the BluetoothChatService
    private final BtHelperHandler mHandler = new BtHelperHandler() {
        @Override
        public void handleMessage(Message msg) {
            BtHelperHandler.MessageType messageType =
                    BtHelperHandler.MessageType.values()[msg.what];
            switch (messageType) {
                case STATE:
                    stateChanged((BtSPPHelper.State) msg.obj);
                    break;
                case WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage;
                    try {
                        readMessage = new String(readBuf, 0, msg.arg1, "UTF-16");
                    } catch (UnsupportedEncodingException e) {
                        // Should complain
                        readMessage = "";
                    }
                    mConversationArrayAdapter.add(mConnectedDeviceName + ":  "
                            + readMessage);
                    break;
                case DEVICE:
                    // save the connected device's name
                    mConnectedDeviceName = (String) msg.obj;
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case NOTIFY:
                    Toast.makeText(getApplicationContext(), (String) msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        /*
         * If the Handler got a state-changes message, process
         * the new state here. We indicate current state in the
         * title bar
         */
        private void stateChanged(BtSPPHelper.State state) {
            switch (state) {
                case CONNECTED:
                    //mTitle.setText(R.string.title_connected_to);
                    //mTitle.append(mConnectedDeviceName);
                    Log.i(TAG, "** DEVICE CONNECTED **");
                    mConversationArrayAdapter.clear();
                    break;
                case CONNECTING:
                    //mTitle.setText(R.string.title_connecting);
                    Log.i(TAG, "** DEVICE CONNECTING **");
                    break;
                case LISTEN:
                case NONE:
                    //mTitle.setText(R.string.title_not_connected);
                    Log.i(TAG, "** DEVICE NOT CONNECTED **");
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    //String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    String address = "00:12:07:13:42:46";
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    btSPPHelper.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    setupLink();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

}
