package com.app.bluepay;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class Home extends Fragment {

    private Button mAboutButton;
    private Button mTransactionButton;
    private Button mLoginButton;


    public Home() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Animation animation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.pulse);
        ImageView image = (ImageView)getActivity().findViewById(R.id.logo);
        image.startAnimation(animation);

        mAboutButton = (Button)getActivity().findViewById(R.id.about);
        mAboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.About();
            }
        });

        mTransactionButton=(Button)getActivity().findViewById(R.id.transaction);
        mTransactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.makeTransaction();
            }
        });

        mLoginButton= (Button)getActivity().findViewById(R.id.login);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.login();
            }
        });
    }

    changeFragment mCallback;

    // Container Activity must implement this interface
    public interface changeFragment {
        public void About();
        public void makeTransaction();
        public void login();
        public void displayTitle(int position);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCallback.displayTitle(0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (changeFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString());
        }
    }

}
