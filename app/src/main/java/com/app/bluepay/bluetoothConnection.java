package com.app.bluepay;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class bluetoothConnection extends Fragment {


    private static int REQUEST_ENABLE_BT = 1;
    private static String DESKEY= "neduniversityofengineeringandtechnology";
    private Button mConnectButton;
    private Button mOpenConnection;
    private Button mRefreshButton;
    private Button mAccountInfo;
    private EditText mAmount;
    private Spinner mSpinner;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothDevice mBluetoothDevice;
    public ConnectThread mConnectThread;
    public AcceptThread mAcceptThread;
    private List<BluetoothDevice> mBluetoothConnections = new ArrayList<BluetoothDevice>();
    private View rootView;
    private BroadcastReceiver mReceiver;
    public JSONObject message;


    public bluetoothConnection() {}
        // Required empty public constructor


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        if (rootView == null){
            rootView=inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);
        }

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    if(!mBluetoothConnections.contains(device))
                        mBluetoothConnections.add(device);
                    fillList();
                    Log.d("Broadcast Receiver","Found device!");
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mConnectThread != null){
            mConnectThread.cancel();
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
        }
        if(mBluetoothAdapter!=null)
            if(mBluetoothAdapter.isDiscovering())
              mBluetoothAdapter.cancelDiscovery();
        getActivity().unregisterReceiver(mReceiver);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAmount=(EditText)getActivity().findViewById(R.id.editText);

        mConnectButton= (Button)getActivity().findViewById(R.id.connect);
        mConnectButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.touch);
                //Alert Dialog code
                final AlertDialog.Builder alertDialog= new AlertDialog.Builder(getActivity());
                alertDialog.setMessage("Do you want send Rs."+mAmount.getText()+" to this account?");
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mConnectThread = new ConnectThread(mBluetoothDevice, mBluetoothAdapter);
                        mConnectThread.registerCallback(getActivity());
                        mConnectThread.run();
                        try {
                            if (mConnectThread.threadIsConnected()) {
                                message = new JSONObject();
                                try{
                                    message.accumulate("Sender",mCallback.getAccount());
                                    message.accumulate("Amount",mAmount.getText());
                                    message.accumulate("Code",getTransactionCode());
                                }
                                catch (Exception e){

                                }
                                //Send message to receiver
                                mConnectThread.write("getReceiver".getBytes());
                                //Change text of button. This act as a check to connect/disconnect connection
                                mConnectButton.setText("Disconnect");
                                //Set open connection button to false
                                mRefreshButton.setEnabled(false);
                                mSpinner.setEnabled(false);
                                mOpenConnection.setEnabled(false);
                            } else if(mBluetoothDevice.getBondState()!=10)
                                Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getActivity(),"Transaction failed. Receiver Unavailable.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                alertDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(),"Transaction cancelled",Toast.LENGTH_SHORT).show();
                    }
                });

                mConnectButton.startAnimation(animation);

                if (mBluetoothDevice!=null) {
                    if (mConnectButton.getText().equals("Connect")) {
                        alertDialog.show();
                    } else {

                        mConnectThread.cancel();
                        mConnectButton.setText("Connect");
                        mRefreshButton.setEnabled(true);
                        mSpinner.setEnabled(true);
                        mOpenConnection.setEnabled(true);
                    }
                }
                else{
                    Toast.makeText(getContext(), "Press Refresh and Select a Device First!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        mOpenConnection= (Button)getActivity().findViewById(R.id.openConnection);
        mOpenConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation animation = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.touch);

                mOpenConnection.startAnimation(animation);

                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    if(mOpenConnection.getText().equals("Open Connection")) {
                        mAcceptThread = new AcceptThread(mBluetoothAdapter);
                        mAcceptThread.registerCallback(getActivity());
                        mAcceptThread.start();
                        Intent discoverableIntent =
                        new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
                        startActivity(discoverableIntent);
                        mOpenConnection.setText("Close Connection");
                        mRefreshButton.setEnabled(false);
                        mSpinner.setEnabled(false);
                        mConnectButton.setEnabled(false);
                    }
                    else{
                        mAcceptThread.cancel();
                        mOpenConnection.setText("Open Connection");
                        mRefreshButton.setEnabled(true);
                        mSpinner.setEnabled(true);
                        mConnectButton.setEnabled(true);
                    }
                }

            }
        });

        mRefreshButton= (Button)getActivity().findViewById(R.id.refresh);
        mSpinner = (Spinner)getActivity().findViewById(R.id.spinner);
        mRefreshButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.touch);

                mRefreshButton.startAnimation(animation);
                mBluetoothConnections.clear();
                bluetoothConnect();
                fillList();
                discoverDevices();
            }
        });

        mAccountInfo= (Button)getActivity().findViewById(R.id.accountInfo);
        mAccountInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.accountInfo();
            }
        });



//Problem Here
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mBluetoothDevice = mBluetoothConnections.get(position);
                    if(mBluetoothDevice.getBondState() == 10) {
                        if (android.os.Build.VERSION.SDK_INT >= 19)
                            mBluetoothDevice.createBond();
                    }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }



    private void discoverDevices(){
        if(mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
    }

    private  void fillList(){
        ArrayList<String> conn= new ArrayList<String>();
        for(BluetoothDevice bt:mBluetoothConnections)
            conn.add(bt.getName());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                R.layout.spinner_item,conn);
        mSpinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void bluetoothConnect(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(getActivity(),"Press refresh again!", Toast.LENGTH_SHORT).show();
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices) {
                mBluetoothConnections.add(bt);
        }
    }


    bluetoothConnection.changeFragment mCallback;

    // Container Activity must implement this interface
    public interface changeFragment {
        public String getAccount();
        public void accountInfo();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (bluetoothConnection.changeFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    private String getTransactionCode(){
        String code= "";
        String unit= "1234567890qwertyuiopasdfghjklzxcvbnm";
        for(int i=0;i<16;i++)
            code=code + unit.charAt(new Random().nextInt(unit.length()-1));
        return code;
    }
}
