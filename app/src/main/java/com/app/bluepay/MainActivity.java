package com.app.bluepay;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.bluepay.AcceptThread;
import com.app.bluepay.ConnectThread;
import com.app.bluepay.Home;
import com.app.bluepay.login;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    public String amount=null;
    public ProgressDialog pDialog;
    public login newFragment;
    public aboutUs aboutFragment;
    public bluetoothConnection bluetoothFragment;
    public Bitmap transactionImage;
    private static String DESKEY= "neduniversityofengineeringandtechnology";
    private static String LOGIN_URL= "http://192.168.1.14:8080/android/login.php";
    private static String TRANS_URL= "http://192.168.1.14:8080/android/transaction.php";

    //Camera code
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "Bluepay";
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
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void displayMessageServer(String str) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fragment_dialog, null, false);

        try {
            JSONObject jsonObj = new JSONObject(str);
            client= jsonObj.getString("User");
            amount= jsonObj.getString("Amount");
        }
        catch(JSONException e){
            e.printStackTrace();
        }

        TextView anotherView = (TextView)view.findViewById(R.id.transaction_amount); //enter resource id
        anotherView.setText( client+" has requested Rs."+amount);

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
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
    public String getClient() {
        return client;
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


    private void checkLogin(){
/*       StringRequest stringRequest = new StringRequest(Request.Method.GET, LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        // Result handling
                        System.out.println(response);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Error handling
                System.out.println("Something went wrong!");
                error.printStackTrace();

            }
        });

// Add the request to the queue
        Volley.newRequestQueue(this).add(stringRequest);
*/
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
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_LONG).show();
                                bluetoothFragment.mAcceptThread.write(jsonResponse.getString("message").getBytes());
                            }
                            else{
                                Toast.makeText(getApplicationContext(),jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                bluetoothFragment.mAcceptThread.write(jsonResponse.getString("message").getBytes());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        onBackPressed();
                        makeTransaction();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        pDialog.hide();
                        Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("sender", account);
                params.put("receiver", client);
                params.put("amount", amount);
                params.put("image",imageToString(transactionImage));
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
        captureImage();
        if(dialog!=null){
            dialog.dismiss();
            dialog=null;
        }
    }

    public void cancelPressed(View view) {
        if(dialog!=null){
            dialog.dismiss();
            dialog=null;
        }
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void previewCapturedImage() {
        try {
            // hide video preview
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 1;
            transactionImage = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
            if(transactionImage.getWidth()> transactionImage.getHeight()){
                Matrix matrix = new Matrix();
                matrix.postRotate(-90);
                transactionImage = Bitmap.createBitmap(transactionImage, 0, 0, transactionImage.getWidth(),
                        transactionImage.getHeight(), matrix, true);
            }
            //transaction occurs when image is saved
            transactionFinal();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                pDialog=new ProgressDialog(this);
                pDialog.setMessage("Processing transaction..");
                pDialog.setCancelable(false);
                pDialog.show();
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    //Use this method to convert image to string to be sent to server
    public String imageToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }
}

