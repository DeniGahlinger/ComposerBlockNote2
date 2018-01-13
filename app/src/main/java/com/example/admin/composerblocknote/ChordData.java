package com.example.admin.composerblocknote;

import android.media.MediaPlayer;
import android.os.Handler;

import org.billthefarmer.mididriver.MidiDriver;

/**
 * Created by admin on 12.01.2018.
 */

class ChordData {
    public int begin;
    public int end;
    private int type = 0;
    private int previousType = 0;
    private int nextType = 0;
    private transient Handler handler;
    private boolean isPlaying;
    private int[][] chordNotes = {
            {-12, 0, 7, 12, 16},
            {-10, 2, 9, 14, 17},
            {-8, 4, 7, 11, 16},
            {-19, -7, 12, 17, 21},
            {-17, -5, 14, 19, 23},
            {-15, -3, 9, 12, 16},
    };
    public ChordData(int begin, int end, int type){
        this.begin = begin;
        this.end = end;
        this.type = type;
    }
    public void playChord(int tonality, MidiDriver midiDriver){
        byte ton = (byte) tonality;
        for(int i = 0; i<chordNotes[type].length; i++){
            playNote((byte)(ton + chordNotes[type][i]), midiDriver);
        }
    }
    public void stopChord(int tonality, MidiDriver midiDriver){
        byte ton = (byte) tonality;
        for(int i = 0; i<chordNotes[type].length; i++){
            stopNote((byte)(ton + chordNotes[type][i]), midiDriver);
        }
    }
    private void playNote(byte note, MidiDriver midiDriver){
        byte[] event = new byte[3];
        event[0] = (byte) (0x90 | 0x00);  // 0x90 = note On, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = (byte) 0x7F;
        midiDriver.write(event);
        //Source : https://stackoverflow.com/questions/36193250/android-6-0-marshmallow-how-to-play-midi-notes
    }
    private void stopNote(byte note, MidiDriver midiDriver){
        byte[] event = new byte[3];
        event[0] = (byte) (0x80 | 0x00);  // 0x80 = note Off, 0x00 = channel 1
        event[1] = note;  // 0x3C = middle C
        event[2] = (byte) 0x00;
        midiDriver.write(event);
        //Source : https://stackoverflow.com/questions/36193250/android-6-0-marshmallow-how-to-play-midi-notes
    }
    public void upType(){
        type += 1;
        type = type % 6;
        nextType = type + 1;
        nextType = nextType % 6;
        previousType = type - 1;
        while(previousType < 0){
            previousType += 6;
        }
    }
    public void downType(){
        type -= 1;
        type = type % 6;
        previousType = type - 1;
        while(type < 0){
            type += 6;
        }
        while(previousType < 0){
            previousType += 6;
        }
        nextType = type + 1;
        nextType = nextType % 6;
    }
    public int getType(){
        return type;
    }
    public int getNextType(){
        return nextType;
    }
    public int getPreviousType(){
        return previousType;
    }
    public int getLength(){
        return end-begin;
    }
    public int getDelay(){
        return begin;
    }
    public void displace(int delay){
        begin += delay;
        end += delay;
    }
    public void play(int currentTime, int ton, MidiDriver md){
        isPlaying = true;
        if(currentTime < begin){
            playAudioWithDelay(begin - currentTime, ton, md, false);
            playAudioWithDelay(end - currentTime, ton, md, true);
        }
    }
    public void playAudioWithDelay(int myDelay, final int tonality, final MidiDriver midiDriver, final boolean isStopping) {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isPlaying) {
                    if(isStopping){
                        stopChord(tonality, midiDriver);
                    } else {
                        playChord(tonality, midiDriver);
                    }
                }
            }

        }, myDelay);
    }
    public void stop(int tonality, MidiDriver midiDriver){
        isPlaying = false;
        stopChord(tonality, midiDriver);
    }
    public void setEnd(int time){
        if(end+time-begin>100){
            end = end+time;
        }

    }
    public void setBegin(int time){
        if(end-time-begin>100){
            begin = begin+time;
        }
    }
}