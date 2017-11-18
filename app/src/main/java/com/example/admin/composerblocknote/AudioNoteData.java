package com.example.admin.composerblocknote;

/**
 * Created by admin on 18.11.2017.
 */

public class AudioNoteData {
    private int delay;
    private int length;
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
}
