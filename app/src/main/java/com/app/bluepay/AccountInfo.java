package com.app.bluepay;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountInfo extends Fragment {

    public TextView mUsername;
    public TextView mAmount;
    public TextView mLast;
    private static String ACCOUNT_URL= "http://192.168.1.3:8080/android/account.php";
    ProgressDialog pDialog;
    getUserInterface mListener;

    public AccountInfo() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AccountInfo newInstance() {
        AccountInfo fragment = new AccountInfo();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.account_info, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUsername = (TextView)getActivity().findViewById(R.id.account_info_username);
        mAmount = (TextView)getActivity().findViewById(R.id.account_info_amount);
        mLast = (TextView)getActivity().findViewById(R.id.account_info_last);
        getAccountInfo();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (getUserInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    public void getAccountInfo() {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest postRequest = new StringRequest(Request.Method.POST, ACCOUNT_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            pDialog.hide();
                            JSONObject jsonResponse = new JSONObject(response);
                            mUsername.setText(jsonResponse.getString("username"));
                            mAmount.setText("Rs."+jsonResponse.getString("amount"));
                            JSONObject lT = new JSONObject(jsonResponse.getString("last"));
                            if(lT.getString("sender").equals(jsonResponse.getString("username")))
                                mLast.setText("Last Transaction: You sent "+
                                        " Rs."+lT.getString("amount_sent")+
                                " to "+lT.getString("receiver")+
                                " on\n"+lT.getString("date")+
                                        "\nTransaction#: "+lT.getString("id"));
                            else
                                mLast.setText("Last Transaction: "+
                                        lT.getString("sender")+
                                        " sent you Rs."+lT.getString("amount_sent")+
                                        " on\n"+lT.getString("date")+
                                        "\nTransaction#: "+lT.getString("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            pDialog.hide();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pDialog.hide();
                        error.printStackTrace();
                        Toast.makeText(getActivity(),"Unable to connect to server!", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("username",mListener.getUsername());
                return params;
            }
        };
        Volley.newRequestQueue(getActivity()).add(postRequest);
    }

    public interface getUserInterface {
        public String getUsername();
        public void closeAccountInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener.closeAccountInfo();
    }
}
