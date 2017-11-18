package com.example.admin.composerblocknote;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by admin on 18.11.2017.
 */

public class AudioNoteData {
    private int delay;
    private int length;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    public AudioNoteData(int delay){
        this.delay = delay;
    }
    public void setLength(int length){
        this.length = length;
    }
    public int getDelay(){
        return delay;
    }
    public  int getLength(){
        return length;
    }
    public void play(int currentTime, String path, int id){
        Log.d("Playy", "play function YES for : " + id);
        Log.d("Playy", "currentTime : " + currentTime + ", And Delay : " + delay + ", And Length : " + length);
        if(currentTime < delay){
            Log.d("Playy", "currentTime < delay for : " + id);
            try{
                playAudioWithDelay(delay - currentTime, path, id);
            }catch(Exception e){
            }
        } else if(currentTime < delay + length){
            Log.d("Playy", "currentTime < delay + length for : " + id);
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
    public void playAudioWithDelay(int myDelay, final String path, final int id){
        handler.postDelayed(new Runnable(){
            @Override
            public void run() {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path + "/" + id + ".3gp");
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, myDelay);
    }
    public void stop(){
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();
        }
    }
}
