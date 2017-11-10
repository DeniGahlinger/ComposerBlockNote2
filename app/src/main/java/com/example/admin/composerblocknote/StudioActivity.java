package com.example.admin.composerblocknote;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

/**
 * Credits to Sylvain Saurel for his tutorial on Android:: audio recorder.
 */
public class StudioActivity extends AppCompatActivity {

    private Button play, recordStop;
    private MediaRecorder myAudioRecorder;
    private String outputFile;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        play = (Button) findViewById(R.id.play);
        recordStop = (Button) findViewById(R.id.recordStop);
        play.setEnabled(false);

        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";

        recordStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(recording) { // Record.
                    try {
                        myAudioRecorder = new MediaRecorder();
                        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
                        myAudioRecorder.setOutputFile(outputFile);

                        myAudioRecorder.prepare();
                        myAudioRecorder.start();
                    } catch(IllegalStateException ise){

                    } catch (IOException ioe){

                    }
                    recordStop.setText("Stop");
                    recording = false;
                    play.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                }
                else // Stop.
                {
                    myAudioRecorder.stop();
                    myAudioRecorder.release();
                    myAudioRecorder = null;
                    recording = true;
                    recordStop.setText("Record");
                    play.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Audio Recorded succesfully", Toast.LENGTH_LONG).show();
                }
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try{
                    mediaPlayer.setDataSource(outputFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();

                }catch(Exception e){

                }
            }
        });
    }
}
