package com.app.bluepay;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link login.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class login extends Fragment {

    private OnFragmentInteractionListener mListener;
    private Button mLogButton;
    private EditText mAccount;
    private EditText mPass;


    public login() {
        // Required empty public constructor
    }

    public static login newInstance(String param1, String param2) {
        login fragment = new login();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mListener=(OnFragmentInteractionListener)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mListener.setTitleLogin();
        View rootView=inflater.inflate(R.layout.fragment_login, container, false);

        mAccount=(EditText)rootView.findViewById(R.id.account_id);
        mPass=(EditText)rootView.findViewById(R.id.password);

        mLogButton=(Button)rootView.findViewById(R.id.log_button);
        mLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.touch);

                mLogButton.startAnimation(animation);
                if(mAccount.getText().length()==0 || mPass.getText().length()==0) {
                    Toast.makeText(getContext(), "Account/Password field is empty", Toast.LENGTH_SHORT).show();
                }
                else{
                    mListener.onFragmentInteraction(mAccount.getText().toString(),mPass.getText().toString());
                }
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String account,String pass);
        void setTitleLogin();
    }

    public void clearTextFields(){
        mAccount.setText("");
        mPass.setText("");
    }
}
