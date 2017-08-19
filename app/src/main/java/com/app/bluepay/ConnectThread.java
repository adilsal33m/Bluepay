package com.app.bluepay;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ConnectThread extends Thread {

    public interface updateUI{
        public void displayMessage(String str);
    };

    void registerCallback(Activity c){
        mCallback= (updateUI)c;
    }
    updateUI mCallback;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothServerSocket mBluetoothServerSocket;
    public ConnectThread.ConnectedThread mConnectedThread;
    public String data="Null";
    public static final  UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String DESKEY= "neduniversityofengineeringandtechnology";
    public String str=null;
    private Handler mBluetoothServerHandler;
    private  BluetoothSocket mBluetoothSocket;

    public ConnectThread(BluetoothDevice device,BluetoothAdapter b) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mBluetoothAdapter=b;
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    public void run() {

        // Always cancel discovery because it will slow down a connection
        mBluetoothAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a successful connection or an exception
            Log.i(TAG, "Connecting to socket...");
            mmSocket.connect();
        } catch (IOException e) {
            Log.e(TAG, e.toString());

//                try {
//                    Log.i(TAG, "Trying fallback...");
//                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class}).invoke(mmDevice, 1);
//                    mmSocket.connect();
//                    Log.i(TAG, "Connected");
//                } catch (Exception e2) {
//                    Log.e(TAG, "Couldn't establish Bluetooth connection!");
            try {
                mmSocket.close();
            } catch (IOException e3) {
            }
            return;
        }
        mConnectedThread=new ConnectThread.ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            if(mConnectedThread != null)
                mConnectedThread.cancel();
            if(mmSocket != null)
                mmSocket.close();
            if(mBluetoothSocket!=null)
                mBluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        byte[] mmBuffer;
        Handler mHandler = new Handler(Looper.getMainLooper());


        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (mmSocket.isConnected()) {
                try {
                    // Read from the InputStream.
                    if(mmInStream.available()>0) {
                        mmBuffer = new byte[mmInStream.available()];
                        mmInStream.read(mmBuffer, 0, mmInStream.available());
                        //Decryption performed here
                        TripleDES test = new TripleDES(DESKEY.getBytes("utf-8"));
                        str = new String(test.decrypt(mmBuffer), "US-ASCII");
                        //   Send the obtained bytes to the UI activity_main.
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCallback.displayMessage(str);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }

        }


        // Call this from the main activity_main to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                //Encrypting data here
                TripleDES test = new TripleDES(DESKEY.getBytes("utf-8"));
                mmOutStream.write(test.encrypt(bytes));
            } catch (Exception e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }

        }

        // Call this method from the main activity_main to shut down the connection.
        public void cancel() {
            try {
                mmInStream.close();
                mmOutStream.close();
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }


    public boolean threadIsConnected(){
        return mConnectedThread.mmSocket.isConnected();
    }

    public void write(byte[] bytes){
        mConnectedThread.write(bytes);
    }

}
