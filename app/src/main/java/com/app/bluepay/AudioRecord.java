package com.app.bluepay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class AudioRecord {

    private static final String DIRECTORY_NAME = "Bluepay";
    private MediaRecorder recorder;
    Context mContext;
    String path;

    public AudioRecord(Context c){
        mContext=c;
        recorder= new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // Record to the external cache directory for visibility
       }

    public void recorderPrepare() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setTitle("Pass Phrase");
        builder.setMessage("Do you want to record audio to complete transaction?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Record to the external cache directory for visibility
                path = getNewFile().getPath();
                recorder.setOutputFile(path);
                try {
                    recorder.prepare();
                }
                catch(IOException e){

                }
                recorderStart();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.show();
    }

    public void recorderStart(){
        final ProgressDialog pDialog= new ProgressDialog(mContext);
        pDialog .setTitle("Recording Message");
        pDialog.setMessage("Please say your pass phrase!");
        pDialog.setCancelable(false);
        pDialog.setMax(5);
        pDialog.setProgress(0);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.show();
        recorder.start();   // Recording is now started
        updateProgress(pDialog);
        //Stops Recording
        Handler h= new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                recorderStop();
                recorderRelease();
                pDialog.hide();
            }
        },5000);
    }

    private void updateProgress(final ProgressDialog pDialog){

        final ProgressDialog p=pDialog;
        //Progress Dialog
        Handler h2= new Handler();
        h2.postDelayed(new Runnable() {
            @Override
            public void run() {
                p.setProgress(p.getProgress()+1);
                if(pDialog.getProgress()<5)
                    updateProgress(p);
            }
        },1000);
    }

    public void recorderReset(){
        recorder.reset();
    }

    public void recorderRelease(){
        recorder.release();
    }

    public void recorderStop(){
        recorder.stop();
    }

    public File getNewFile(){
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
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
        file = new File(mediaStorageDir, DIRECTORY_NAME +timeStamp + ".amr");
        return file;
    }
}
