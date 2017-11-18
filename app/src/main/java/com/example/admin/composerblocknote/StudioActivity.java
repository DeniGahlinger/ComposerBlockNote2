package com.example.admin.composerblocknote;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cuboid.cuboidcirclebutton.CuboidButton;

import java.io.File;
import java.io.IOException;

/**
 * Credits to Sylvain Saurel for his tutorial on Android:: audio recorder.
 */
public class StudioActivity extends AppCompatActivity {
    private static MediaPlayer mediaPlayer;
    private CuboidButton recordStop, play;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private boolean recording = false;
    private boolean playing = false;
    private StudioManagerView studioView;
    private String currentPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        play = (CuboidButton) findViewById(R.id.play);
        recordStop = (CuboidButton) findViewById(R.id.recordStop);
        studioView = (StudioManagerView) findViewById(R.id.myStudioManager);
        //play.setEnabled(false);

        currentPath = getIntent().getStringExtra("currentPath");


        outputFile = currentPath+"/" +"1.3gp";
        Log.d("ERROR", "outputFile: " + outputFile);

        recordStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(!recording) { // Record.
                    try {
                        myAudioRecorder = new MediaRecorder();
                        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        myAudioRecorder.setOutputFile(currentPath + "/" + studioView.getNextAudioID() + ".3gp");

                        myAudioRecorder.prepare();
                        myAudioRecorder.start();
                        studioView.addNewAudio();
                    } catch(IllegalStateException ise){

                    } catch (IOException ioe){

                    }
                    //recordStop.setText("Stop");
                    //recordStop.setText("@drawable/ic_stop_red");
                    //recordStop.setCr_icon(1);
                    recording = true;
                    //play.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                    studioView.play(currentPath,true);
                }
                else // Stop.
                {
                    studioView.finishAddingNewAudio();
                    myAudioRecorder.stop();
                    myAudioRecorder.release();
                    myAudioRecorder = null;
                    recording = false;
                    //recordStop.setText("Record");
                    //recordStop.setText("@drawable/ic_micro_white");
                    //play.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Audio Recorded succesfully", Toast.LENGTH_LONG).show();
                    studioView.stop();
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing){
                    playing = true;
                    /*mediaPlayer = new MediaPlayer();
                    try{
                        mediaPlayer.setDataSource(outputFile);
                        mediaPlayer.prepare();
                        mediaPlayer.seekTo(-2000);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    }catch(Exception e){
                        Toast.makeText(getApplicationContext(), "An error has occured!", Toast.LENGTH_LONG).show();
                    }*/
                    studioView.play(currentPath,false);
                }
                else {
                    playing = false;
                    //mediaPlayer.stop();
                    studioView.stop();
                }
            }
        });
    }
}
