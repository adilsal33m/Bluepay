package com.app.bluepay;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class aboutUs extends Fragment {

    public ImageView mLogo;

    public aboutUs() {
        // Required empty public constructor
    }

    public static aboutUs newInstance() {
        aboutUs fragment = new aboutUs();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        try {
//            TripleDES test = new TripleDES("neduniversityofengineeringandtechnology".getBytes());
//            byte[] value=test.encrypt("Sidra".getBytes("utf-8"));
//            Log.d("Encryption",value.toString());
//            Log.d("Decryption",new String(test.decrypt(value), "UTF-8"));
//        }
//        catch(Exception e){
//            Log.d("Encryption","Error");
//        }
            // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_about_us, container, false);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setImage(Bitmap bitmap){
        mLogo=(ImageView)getActivity().findViewById(R.id.about_logo);
        mLogo.setImageBitmap(bitmap);
    }

}
