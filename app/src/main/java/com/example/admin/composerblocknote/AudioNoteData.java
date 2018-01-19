package com.example.admin.composerblocknote;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Created by admin on 18.11.2017.
 */

public class AudioNoteData implements Serializable{
    private static final long serialVersionUID = 1L;
    private boolean isPlaying;
    private int delay;
    private int length;
    private transient MediaPlayer mediaPlayer;
    private transient Handler handler;
    private String filePath;
    private int id;
    public AudioNoteData(int delay){
        this.delay = delay;
    }
    public void setLength(int length){
        this.length = length;
    }
    public int getDelay(){
        return delay;
    }
    public void setDelay(int newDelay){
        delay = newDelay;
    }
    public  int getLength(){
        return length;
    }
    public void play(int currentTime, String path){
        isPlaying = true;
        if(currentTime < delay){
            try{
                playAudioWithDelay(delay - currentTime, path);
            }catch(Exception e){
            }
        } else if(currentTime < delay + length){
            mediaPlayer = new MediaPlayer();
            try{
                mediaPlayer.setDataSource(path + "/" + id + ".3gp");
                mediaPlayer.prepare();
                mediaPlayer.seekTo(currentTime - delay);
                mediaPlayer.start();
            }catch(Exception e){
            }
        }

    }
    public void playAudioWithDelay(int myDelay, final String path){
        handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                if(isPlaying)
                {
                    mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(path + "/" + id + ".3gp");
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, myDelay);
    }
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    public void stop() {
        isPlaying = false;
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }
}
