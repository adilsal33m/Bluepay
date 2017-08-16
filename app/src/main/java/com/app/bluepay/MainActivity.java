package com.app.bluepay;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends ActionBarActivity implements
        ConnectThread.updateUI,
        AcceptThread.updateUI,
        Home.changeFragment,
        login.OnFragmentInteractionListener,
        bluetoothConnection.changeFragment,
        AccountInfo.getUserInterface{

    private static final int MY_SOCKET_TIMEOUT_MS = 300000;
    private AlertDialog dialog;
    public String client=null;
    public String account=null;
    public String pass=null;
    public String code=null;
    public String amount=null;
    private String temp=null; //to be used for Marshmallow
    public ProgressDialog pDialog;
    public login newFragment;
    public aboutUs aboutFragment;
    public bluetoothConnection bluetoothFragment;
    private static String DESKEY= "neduniversityofengineeringandtechnology";
    private static String LOGIN_URL= "https://bluepay.000webhostapp.com/android/login.php";
    private static String TRANS_URL= "https://bluepay.000webhostapp.com/android/transaction.php";
    private static String SENDER_URL= "https://bluepay.000webhostapp.com/android/sender.php";
    // directory name to store captured images and videos
    private static final String DIRECTORY_NAME = "Bluepay";
    private Uri fileUri; // file url to store image/video
    //Camera code end

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Check that the activity_main is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity_main layout
            Home firstFragment = new Home();

            // In case this activity_main was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();

        }
    }


    @Override
    public void displayMessage(String str) {
        String[] strings = str.split("\\$");
        switch (strings[0]) {
            case "0":
                Toast.makeText(MainActivity.this, strings[1], Toast.LENGTH_SHORT).show();
                String msg=bluetoothFragment.message.toString();
                msg=msg.substring(0,msg.length()-1)+",\"Receiver\":\""+strings[2]+"\"}";
                try {
                    updateSenderTable(new JSONObject(msg));
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                //Electronic Certifcate
                bluetoothFragment.mConnectThread.write(msg.getBytes());
                writeToFile(msg);
                break;
            case "1":
                try{
                    JSONObject jsonResponse = new JSONObject(strings[1]);
                    Toast.makeText(MainActivity.this,jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                    bluetoothFragment.resetConnectButton();
                }
                catch (JSONException e){

                }
                break;
        }
    }


    @Override
    public void displayMessageServer(String str) {
        if(str.equals("getReceiver")){
            Toast.makeText(this,"Client Connected...",Toast.LENGTH_SHORT).show();
        }
        else{
        //Electronic Certifcate
        writeToFile(str);
        //Alert Dialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_dialog, null, false);

        try {
            JSONObject jsonObj = new JSONObject(str);
            client= jsonObj.getString("Sender");
            amount= jsonObj.getString("Amount");
            code= jsonObj.getString("Code");
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        TextView anotherView = (TextView)view.findViewById(R.id.transaction_amount); //enter resource id
        anotherView.setText( client+" wishes to transfer Rs."+amount+" to your account.\nTransaction Code: "+code);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
        }
    }

    @Override
    public void About() {
        aboutUs aboutFrag = (aboutUs)
                getSupportFragmentManager().findFragmentById(R.id.aboutFragment);

        if (aboutFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            aboutFragment = new aboutUs();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, aboutFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            displayTitle(1);
        }

    }


    @Override
    public void makeTransaction() {
        bluetoothConnection bluetoothFrag = (bluetoothConnection)
                getSupportFragmentManager().findFragmentById(R.id.bluetoothFrag);

        if (bluetoothFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            bluetoothFragment = new bluetoothConnection();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, bluetoothFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();
            displayTitle(2);        }

    }

    @Override
    public void login() {
        login loginFrag = (login) getSupportFragmentManager().findFragmentById(R.id.loginFrag);

        if (loginFrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            newFragment = new login();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

            displayTitle(3);

        }

    }

    public void displayTitle(int position) {
        // update the main content by replacing fragments
        String title = "";
        switch (position) {
            case 0:
                title = "Home";
                break;
            case 1:
                title = "About Us";
                break;
            case 2:
                title = "Make Transaction";
                break;
            case 3:
                title = "Login";
                break;
            case 4:
                title = "Account Info";
                break;

            default:
                break;
        }
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onFragmentInteraction(String a, String p) {
        account=a;
        pass=p;
        checkLogin();
    }

    @Override
    public void setTitleLogin() {
        displayTitle(3);
    }

    @Override
    public String getAccount() {
        return account;
    }

    @Override
    public void accountInfo() {
        AccountInfo accountfrag = (AccountInfo)
                getSupportFragmentManager().findFragmentById(R.id.account_info_fragment);

        if (accountfrag != null) {
            // If article frag is available, we're in two-pane layout...

            // Call a method in the ArticleFragment to update its content
        } else {
            // Otherwise, we're in the one-pane layout and must swap frags...

            // Create fragment and give it an argument for the selected article
            AccountInfo newFragment = new AccountInfo();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, newFragment);
            transaction.addToBackStack(null);

            // Commit the transaction
            transaction.commit();

            displayTitle(4);
        }
    }

    public void updateSenderTable(final JSONObject jsonObject) {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, SENDER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                            pDialog.hide();
                            Log.d("SenderTable",response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Log.d("SenderTable",error.toString());
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(),"Unable to connect to server!", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                try {
                    params.put("sender", jsonObject.getString("Sender"));
                    params.put("code", jsonObject.getString("Code"));
                    params.put("amount", jsonObject.getString("Amount"));
                    params.put("receiver", jsonObject.getString("Receiver"));
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);

    }


    private void checkLogin(){
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            pDialog.hide();
                            newFragment.clearTextFields();
                            Log.d("Login",response);
                            JSONObject jsonResponse = new JSONObject(response);
                            if(jsonResponse.get("success").equals("1")){
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                makeTransaction();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(),"Unable to connect to server!", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("username", account);
                params.put("password", pass);
                return params;
            }
        };
        Volley.newRequestQueue(this).add(postRequest);
    }

    private void transactionFinal(){
        StringRequest postRequest = new StringRequest(Request.Method.POST, TRANS_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pDialog.hide();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if(jsonResponse.get("success").equals("1")){
                                Log.d("Transaction",response);
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                bluetoothFragment.mAcceptThread.write(("1$"+jsonResponse.toString()).getBytes());
                            }
                            else{
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                bluetoothFragment.mAcceptThread.write(("1$"+jsonResponse.toString()).getBytes());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        bluetoothFragment.resetOpenConnectionButton();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(),"Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("sender", client);
                params.put("receiver", account);
                params.put("amount", amount);
                params.put("code",code);
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(postRequest);
    }

    @Override
    public String getUsername() {
        return account;
    }

    @Override
    public void closeAccountInfo() {
        displayTitle(2);
    }

    public void acceptPressed(View view) {
        transactionFinal();
        if(dialog!=null){
            dialog.dismiss();
            dialog=null;
        }
    }

    public void cancelPressed(View view) {
        if(dialog!=null){
            dialog.dismiss();
            Toast.makeText(this,"Transaction cancelled",Toast.LENGTH_SHORT).show();
            dialog=null;
        }
    }

    public File getNewFile(){
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                DIRECTORY_NAME);
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(DIRECTORY_NAME, "Oops! Failed create "
                        + DIRECTORY_NAME + " directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File file;
        file = new File(mediaStorageDir, DIRECTORY_NAME +timeStamp + ".txt");
        try {
            file.createNewFile();
        }
        catch(IOException e){

        }
        return file;
    }

    private void writeToFile(String data) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    writeFileDetails(data);
                } else {
                    temp = data;
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
            else {
                writeFileDetails(data);
            }
        }

    private void writeFileDetails(String data){
        File f = getNewFile();
        try {
            String timeStamp = new SimpleDateFormat("yyyy.MM.dd '|' HH:mm:ss z",
                    Locale.getDefault()).format(new Date());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(f));
            outputStreamWriter.write(data.substring(0, data.length() - 1) + "\n\"TimeStamp\":\"" + timeStamp + "\"}");
            outputStreamWriter.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            //resume tasks needing this permission
            writeFileDetails(temp);
        }
    }
}

