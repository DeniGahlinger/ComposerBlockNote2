package com.example.admin.composerblocknote;

import android.Manifest;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.cuboid.cuboidcirclebutton.CuboidButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    private String mainDirName = "ComposerBlockNote";
    private File baseFolder;


    public void onBackPressed(){
        System.out.println("finished");
        finish();
    }
    /* Aller sur la page de choix des parties, pas sur le créateur de musiques.
    */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {

        switch(keyCode) {
            case(KeyEvent.KEYCODE_BACK):
                Intent intent = new Intent(this, PartNavigatorActivity.class);
                String name = (String) getIntent().getExtras().get("songSongName");
                FileManager fm = new FileManager(this.getBaseContext(),this);
                baseFolder = fm.getMusicStorageDir(mainDirName);
                if (baseFolder == null){
                    baseFolder = fm.createMusicStorageDir(mainDirName);
                }

                intent.putExtra("songName", name);
                intent.putExtra("mainDir", mainDirName);
                intent.putExtra("initMainDir", baseFolder);

                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        play = (CuboidButton) findViewById(R.id.play);
        recordStop = (CuboidButton) findViewById(R.id.recordStop);

        studioView = (StudioManagerView) findViewById(R.id.myStudioManager);
        currentPath = getIntent().getStringExtra("currentPath");
        File currentFile = new File(currentPath);
        studioView.setPath(currentPath);
        studioView.openSongData(currentFile.getParent());
        if((boolean)getIntent().getExtras().get("newPart")){
            studioView.openExistingPartData(currentFile.getAbsolutePath());
        }
        readAudioDataNodes(currentPath + "/.notes");
        readChordDataNodes(currentPath + "/.chords");
        outputFile = currentPath+"/" +"1.3gp";

        recordStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int permissionCheck = ContextCompat.checkSelfPermission(StudioActivity.this,
                        Manifest.permission.RECORD_AUDIO);
                if(permissionCheck != 0){

                    ActivityCompat.requestPermissions(StudioActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},0);

                }
                else{
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
                            ise.printStackTrace();
                        } catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                        recording = true;
                        play.setEnabled(false);

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
                        play.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Audio Recorded succesfully", Toast.LENGTH_LONG).show();
                        studioView.stop();
                        try{
                            writeAudioDataNodes(currentPath + "/.notes");
                            writeChordDataNodes(currentPath + "/.chords");
                        }
                        catch(FileNotFoundException fnfe){
                            fnfe.printStackTrace();
                        }
                        catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }

            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playing){
                    playing = true;
                    studioView.play(currentPath,false);
                }
                else {
                    playing = false;
                    studioView.stop();
                }
            }
        });
    }
    private void writeAudioDataNodes(String path) throws FileNotFoundException, IOException{
        studioView.saveAudioData();
        /*try{
            FileOutputStream fos =  new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(studioView.getAudioData());
            out.close();
            fos.close();
        }
        catch(FileNotFoundException fnfe){
            throw(fnfe);
        }
        catch(IOException ioe){
            throw(ioe);
        }*/
    }
    private void writeChordDataNodes(String path) throws FileNotFoundException, IOException{
        studioView.saveChordData();
        /*try{
            FileOutputStream fos =  new FileOutputStream(path);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(studioView.getChordData());
            out.close();
            fos.close();
        }
        catch(FileNotFoundException fnfe){
            throw(fnfe);
        }
        catch(IOException ioe){
            throw(ioe);
        }*/
    }

    private void readAudioDataNodes(String path){
        ArrayList<AudioNoteData> ary = null;
        try {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fin);
            ary = (ArrayList<AudioNoteData>) in.readObject();
            in.close();
            fin.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        if (ary != null){
            studioView.setAudioData(ary);
        }
    }
    private void readChordDataNodes(String path){
        ArrayList<ChordData> ary = null;
        try {
            FileInputStream fin = new FileInputStream(path);
            ObjectInputStream in = new ObjectInputStream(fin);
            ary = (ArrayList<ChordData>) in.readObject();
            in.close();
            fin.close();
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Class not found");
            c.printStackTrace();
            return;
        }
        if (ary != null){
            studioView.setChordData(ary);
        }
    }
}
