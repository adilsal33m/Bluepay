package com.app.bluepay;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Adil Saleem on 24-Mar-17.
 */

public class AcceptThread extends Thread {
    private  BluetoothServerSocket mmServerSocket;
    private  BluetoothAdapter mBluetoothAdapter;
    public static final UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String DESKEY= "neduniversityofengineeringandtechnology";
    private static final String TAG = "MY_APP_DEBUG_TAG";
    public String str=null;
    public AcceptThread.ConnectedThread mConnectedThread;
    Handler mHandler = new Handler(Looper.getMainLooper());

    public interface updateUI{
        public void displayMessageServer(String str);
    };

    void registerCallback(Activity c){
        mCallback= (AcceptThread.updateUI)c;
        Log.d(TAG,"Callback registered");
    }
    AcceptThread.updateUI mCallback;

    public AcceptThread(BluetoothAdapter b) {
        // Use a temporary object that is later assigned to mmServerSocket
        // because mmServerSocket is final.
        mBluetoothAdapter=b;
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.
            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Server", uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                cancel();
                mConnectedThread=new AcceptThread.ConnectedThread(socket);
                mConnectedThread.start();
                mConnectedThread.write("Client Connected..".getBytes());
                break;
            }
        }
    }

    // Closes the connect socket and causes the thread to finish.
    public void cancel() {
        try {
            if(mConnectedThread != null)
                mConnectedThread.cancel();
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        byte[] mmBuffer;


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
                                mCallback.displayMessageServer(str);
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
                //Encryption performed here
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