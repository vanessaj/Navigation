package com.vanessamacisaac.navigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by vanessamacisaac on 15-03-13.
 */
public class BtSPPHelper {
    // debugging
    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    public enum State{
        NONE,
        LISTEN,
        CONNECTING,
        CONNECTED;
    }

    // name for the SDP record when creating server socket
    private static final String NAME = "BluetoothTest";

    // unique UUID for this app
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final BtHelperHandler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private State mState;
    private Context mContext;

    // constructor
    public BtSPPHelper(Context context, BtHelperHandler handler){
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = State.NONE;
        mHandler = handler;
    }

    // set current state of chat connection
    private synchronized void setState(State state){
        if(D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // give new state to handler
        mHandler.obtainMessage(BtHelperHandler.MessageType.STATE, -1, state).sendToTarget();
    }

    // return current connection state
    public synchronized State getState(){
        return mState;
    }

    // start session (usually called for onResume() )
    // start AcceptThread to begin a session listening in server mode
    public synchronized void start(){
        if (D) Log.d(TAG, "start");
        // cancel any threads trying to make a connection
        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // cancel any thread currently running a connection
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // start the thread to listen on a BluetoothServerSocket
        if(mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(State.LISTEN);
    }

    // start to initiate connection to a remote client
    public synchronized void connect(BluetoothDevice device){
        if (D) Log.d(TAG, "connect to: " + device);
        // cancel any thread trying to make a connection
        if(mState == State.CONNECTING){
            if(mConnectThread!=null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // cancel any thread currently running a conneciton
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // start the thread to connect with given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(State.CONNECTING);
    }

    // start ConnectedThread to manage conxn
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        if (D) Log.d(TAG, "connected");

        // cancel the thread that completed the connection
        if(mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // cancel any thread currently running a connection
        if(mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // cancel the accept thread, only want to connect to one device
        // BUT WE WANT TWO???
        if (mAcceptThread!=null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // send the name of the connected device back to the UI activity
        mHandler.obtainMessage(BtHelperHandler.MessageType.DEVICE, -1, device.getName()).sendToTarget();
        setState(State.CONNECTED);

    }

    // stop all threads
    public synchronized void stop(){
        if (D) Log.d(TAG, "stop");

        if (mConnectThread!=null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // cancel any thread currently running a connection
        if (mConnectedThread!=null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // start the thread to listen on a BluetoothServerSocket
        if(mAcceptThread == null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(State.NONE);
    }

    // write to the ConnectedThread in an unsynchronized manner
    public void write(byte[] out){
        ConnectedThread r;

        //synchronize a copy of the ConnectedThread
        synchronized (this){
            if (mState != State.CONNECTED) return;
            r = mConnectedThread;
        }
        // perform the write unsynchronized
        r.write(out);
    }

    private void sendErrorMessage(int messageId){
        setState(State.LISTEN);
        mHandler.obtainMessage(BtHelperHandler.MessageType.NOTIFY, -1, mContext.getResources().getString(messageId)).sendToTarget();

    }

    // this thread listens for incoming connections
    private class AcceptThread extends Thread {
        // local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // create a new listening server socket
            try{
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, SPP_UUID);
            } catch(IOException e){
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run(){
            if(D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // listen to the server socket if we're not connected
            while(mState != BtSPPHelper.State.CONNECTED){
                try{
                    // blocking call
                    socket = mmServerSocket.accept();
                } catch (IOException e){
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // if connection accepted
                if (socket != null){
                    synchronized (BtSPPHelper.this) {
                        switch(mState){
                            case LISTEN:
                            case CONNECTING:
                                // normal situation, start connected thread
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case NONE:
                            case CONNECTED:
                                // either not ready or already connected
                                // terminate new socket
                                try{
                                    socket.close();
                                } catch (IOException e){
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }

            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel(){
            if (D) Log.d(TAG, "cancel" + this);
            try {
                mmServerSocket.close();
            } catch (IOException e){
                Log.e(TAG, "close() of server failed", e);
            }

        }
    }

    /*
    * thread runs while attempting to make an outgoing connection with a device
    * runs straight through, connection either succeeds or fails
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device){
            mmDevice = null;
            BluetoothSocket tmp = null;

            // get socket for connection with device
            try{
                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch(IOException e){
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;

        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // always cancel discovery, slows down connection
            mAdapter.cancelDiscovery();

            // make connection to socket
            try{
                // blocking call
                mmSocket.connect();
            } catch (IOException e){
                sendErrorMessage(R.string.bt_unable);
                // close the socket
                try{
                    mmSocket.close();
                } catch (IOException e2){
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // start the service over to restart listening mode
                BtSPPHelper.this.start();
                return;
            }

            // reset the ConnectThread bc we're done
            synchronized (BtSPPHelper.this){
                mConnectThread = null;
            }
            // start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

    }

    /*
    this thread runs during a connection with a remote device
    handles all incoming and outgoing transmissions
     */
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // get the BluetoothSocket input and output streams
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // keep listening to the InputStream while connected
            while(true){
                try{
                    // read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // send the obtained bytes to the UI activity
                    mHandler.obtainMessage(BtHelperHandler.MessageType.READ, bytes, buffer).sendToTarget();
                } catch (IOException e){
                    Log.e(TAG, "disconnected", e);
                    sendErrorMessage(R.string.bt_connection_lost);
                    break;
                }
            }
        }

        /*
        * write to the connected outStream
         */
        public void write(byte[] buffer){
            try{
                mmOutStream.write(buffer);
                // share the sent message back to the UI Activity
                mHandler.obtainMessage(BtHelperHandler.MessageType.WRITE, -1, buffer).sendToTarget();
            } catch (IOException e){
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
