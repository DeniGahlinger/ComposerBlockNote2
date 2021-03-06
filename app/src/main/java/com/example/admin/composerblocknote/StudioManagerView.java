package com.example.admin.composerblocknote;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.billthefarmer.mididriver.MidiDriver;
public class StudioManagerView extends View {
    private String path;

    private int MAX_DELAY_FOR_DOUBLE_TAP = 250;
    private long firstTapTimeOfDoubleTap = System.currentTimeMillis();

    private boolean isRecoding = false;

    public float mouseX = -1;
    public float mouseY = -1;

    private int[][] chordColour =  {{10,30,200},{170,80,240},{250,140,0},{10,180,25},{225,220,20},{200,30,20}};
    private int[][] chordColourWhithed =  {{15,45,250},{210,120,255},{255,190,60},{20,240,45},{255,255,80},{255,100,60}};

    private int trackHeight = 80;

    private Paint myPaint;

    private int tonality = 60;

    private int sizeButtonTonalityX = 140;
    private int sizeButtonTonalityY = 100;

    private float cursorPosition = 0;
    private int tempo = 120;
    private int signatureNb = 4;
    private int signatureNote = 4;
    private int zoom = 6000;
    private boolean isPlaying = false;
    private Timer timer = new Timer();
    public ArrayList<AudioNoteData> audioData = new ArrayList<AudioNoteData>();
    private ArrayList<ChordData> chordData = new ArrayList<ChordData>();

    MidiDriver midiDriver = new MidiDriver();
    private int chordRectHeight = 125;
    private int rectChordModifierSize = 40;
    private int touchState = 0; // 0 : none, 1 : scroll, 2 : displace audio, 3 : displace midi, 4 : resizing midi, 5 switch chord
    private int touchObjectId = -1;
    private int touchSwitchChord = 0;
    private int isDeletingChord = -1;
    private boolean mustSaveAudios;
    private boolean mustSaveChords;

    public StudioManagerView(Context context) {
        super(context);
        init(null, 0);
    }

    public StudioManagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public StudioManagerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StudioManagerView, defStyle, 0);

        a.recycle();
        myPaint = new Paint();

        this.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        touchState = 0;
                        touchObjectId = -1;
                        mouseX = -1;
                        mouseY = -1;
                        // Source of the idea : https://stackoverflow.com/questions/7314579/how-to-catch-double-tap-events-in-android-using-ontouchlistener
                        if(System.currentTimeMillis() - firstTapTimeOfDoubleTap < MAX_DELAY_FOR_DOUBLE_TAP){
                            if(event.getY()> getHeight() - chordRectHeight){
                                for(int i = 0; i< chordData.size(); i++){
                                    if(event.getX()>(getWidth() / 2) + chordData.get(i).getDelay()*
                                            zoom / 60000f - (cursorPosition * zoom / 60000f)){
                                        if(event.getX()<(getWidth() / 2) + chordData.get(i).getDelay()
                                                * zoom / 60000f + chordData.get(i).getLength() * zoom
                                                / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize){
                                            isDeletingChord = i;
                                        }
                                    }
                                }
                            } else {
                                for (int i = 0; i < audioData.size(); i++) {
                                    if (event.getX() > (getWidth() / 2) + audioData.get(i).getDelay()
                                            * zoom / 60000f - (cursorPosition * zoom / 60000f)) {
                                        if (event.getX() < (getWidth() / 2) + audioData.get(i).getDelay()
                                                * zoom / 60000f + audioData.get(i).getLength() * zoom
                                                / 60000f - (cursorPosition * zoom / 60000f)) {
                                            if (event.getY() > getPaddingTop() + 55 + i * trackHeight) {
                                                if (event.getY() < getPaddingTop() + 55 +
                                                        (trackHeight - 5) + i * trackHeight) {
                                                    isDeletingChord = - i - 3;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            doubleTap((int)event.getX());
                        }
                        else{
                            isDeletingChord = -1;
                        }
                        if(event.getX() > getPaddingLeft() + 5 && event.getY() > getPaddingTop() + 5
                                && event.getX() < getPaddingLeft() + 5 + sizeButtonTonalityX
                                && event.getY() < getPaddingTop() + 5 + sizeButtonTonalityY){
                            tonality++;
                            isDeletingChord = -2;
                            try {
                                saveTonality();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (event.getX() > getPaddingLeft() + 5
                                && event.getY() > getPaddingTop() + 5 + 2 * sizeButtonTonalityY
                                && event.getX() < getPaddingLeft() + 5 + sizeButtonTonalityX
                                && event.getY() < getPaddingTop() + 5 + 3 * sizeButtonTonalityY){
                            tonality--;
                            isDeletingChord = -2;
                            try {
                                saveTonality();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            firstTapTimeOfDoubleTap = System.currentTimeMillis();
                        }
                        try {
                            if(mustSaveAudios){
                                saveAudioData();
                            }
                            if(mustSaveChords){
                                saveChordData();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mustSaveAudios = false;
                        mustSaveChords = false;
                        v.invalidate();
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        touchState = 0;
                        mouseX = -1;
                        mouseY = -1;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mouseX != -1 && isPlaying == false){
                            if(touchState == 0 || touchState == 2) {
                                for (int i = 0; i < audioData.size(); i++) {
                                    boolean mustBeChangedButOutOfRectangle = true;
                                    if (event.getX() > (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f)) {
                                        if (event.getX() < (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f + audioData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f)) {
                                            if (event.getY() > getPaddingTop() + 55 + i * trackHeight) {
                                                if (event.getY() < getPaddingTop() + 55 + (trackHeight - 5) + i * trackHeight) {
                                                    touchState = 2;
                                                    touchObjectId = i;
                                                    audioData.get(i).setDelay(audioData.get(i).getDelay()
                                                            - (int) ((mouseX - event.getX()) * 60000 / zoom));
                                                    mustSaveAudios = true;
                                                    mustBeChangedButOutOfRectangle = false;
                                                }
                                            }
                                        }
                                    }
                                    if(mustBeChangedButOutOfRectangle && touchObjectId == i){
                                        audioData.get(i).setDelay(audioData.get(i).getDelay() - (int) ((mouseX - event.getX()) * 60000 / zoom));
                                        mustSaveAudios = true;
                                    }
                                }
                            }
                            if(event.getY()> getPaddingTop() - 55 + getHeight() - chordRectHeight
                                    || touchState == 3
                                    || touchState == 4
                                    || touchState == 5){
                                for(int i = 0; i< chordData.size(); i++){
                                    if(event.getX()>(getWidth() / 2) + chordData.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize){
                                        if((touchState == 0 || touchState == 3 || touchState == 5)
                                                && ((touchObjectId == i && (touchState == 3 || touchState == 5))
                                                || (touchState == 0 && event.getX()<(getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize))){
                                            if(Math.abs(mouseY - event.getY())>Math.abs(mouseX - event.getX())){
                                                touchState=5;
                                                touchObjectId = i;
                                                touchSwitchChord -= mouseY - event.getY();
                                                if(touchSwitchChord > chordRectHeight / 2){
                                                    chordData.get(i).upType();
                                                    touchSwitchChord = touchSwitchChord - chordRectHeight;
                                                    mustSaveChords = true;
                                                } else if(touchSwitchChord < -chordRectHeight / 2){
                                                    chordData.get(i).downType();
                                                    touchSwitchChord = touchSwitchChord + chordRectHeight;
                                                    mustSaveChords = true;
                                                }
                                            } else {
                                                touchState=3;
                                                touchObjectId = i;
                                                chordData.get(i).displace(-(int)((mouseX - event.getX()) * 60000 / zoom));
                                                mustSaveChords = true;
                                            }

                                        }
                                        else if((touchState == 0 || touchState == 4)
                                                && ((touchObjectId == i * 2 && touchState == 4)
                                                || (touchState == 0 && event.getX()<(getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f)))){
                                            mustSaveChords = true;
                                            touchState=4;
                                            touchObjectId = i * 2;
                                            chordData.get(i).setEnd(-(int)((mouseX - event.getX()) * 60000 / zoom));
                                        }
                                    } else if((touchState == 0 || touchState == 4)
                                            && ((touchObjectId == i * 2 + 1 && touchState == 4)
                                            || (touchState == 0 && event.getX()>(getWidth() / 2) + chordData.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f)))){
                                        touchState=4;
                                        touchObjectId = i * 2 + 1;
                                        chordData.get(i).setBegin(-(int)((mouseX - event.getX()) * 60000 / zoom));
                                        mustSaveChords = true;
                                    }
                                }
                            }
                            if(touchState == 1 || touchState == 0){
                                touchState = 1;
                                cursorPosition += (mouseX - event.getX()) * 60000 / zoom;
                            }
                            v.invalidate();
                        }
                        mouseX = event.getX();
                        mouseY = event.getY();
                        break;
                }
                return true;

            }
        });
    }
    public void doubleTap(int mouseX){
        if(isDeletingChord == -1){
            chordData.add(new ChordData((mouseX - getWidth() / 2) * 60000 / zoom + (int)(cursorPosition) - 500, (mouseX - getWidth() / 2) * 60000 / zoom + (int)(cursorPosition) + 500, 0));
            mustSaveChords = true;
        } else if(isDeletingChord > -1){
            chordData.remove(isDeletingChord);
            mustSaveChords = true;
        } else if(isDeletingChord < -2){

            deleteAudioFile(audioData.get(- isDeletingChord - 3).getId());
            audioData.remove(- isDeletingChord - 3);
            mustSaveAudios = true;
        }
        postInvalidate();
    }
    public void openSongData(String path) {
        try {
            /*
             * Reading the meta informations from the .meta file
             */
            FileInputStream fis = new FileInputStream(path + "/.meta");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                String infos[] = line.split(";");
                this.tempo = Integer.parseInt(infos[0]);
                this.signatureNb = Integer.parseInt(infos[1]);
                this.signatureNote = Integer.parseInt(infos[2]);
            }
            System.out.println("Song info : tempo = " + this.tempo + " , sign = " + this.signatureNb + "/" + this.signatureNote);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        printTempo(canvas, signatureNb, signatureNote, tempo, zoom);
        myPaint.setColor(Color.rgb(20,20,90));
        canvas.drawRect((getWidth() / 2) - (cursorPosition * zoom / 60000f) - 2, 0, (getWidth() / 2) - (cursorPosition * zoom / 60000f) + 2, getHeight(), myPaint);
        myPaint.setColor(Color.rgb(200,200,200));
        printAudios(canvas);
        printChords(canvas);
        printTonalityUI(canvas);
        myPaint.setColor(Color.argb(200,30,255,0));
        canvas.drawRect((getWidth() / 2) - 25, getPaddingTop(), (getWidth() / 2) + 25, getPaddingTop() + 50, myPaint);
        canvas.drawRect((getWidth() / 2) - 5, 0, (getWidth() / 2) + 5, getHeight(), myPaint);
        myPaint.setColor(Color.GREEN);

        printMetronome(canvas);


    }
    private void printTonalityUI(Canvas canvas){
        myPaint.setColor(Color.rgb(130,130,130));
        canvas.drawRect(getPaddingLeft() + 5, getPaddingTop() + 5, getPaddingLeft() + 5 + sizeButtonTonalityX, getPaddingTop() + 5 + sizeButtonTonalityY, myPaint);
        canvas.drawRect(getPaddingLeft() + 5, getPaddingTop() + 5 + 2 * sizeButtonTonalityY, getPaddingLeft() + 5 + sizeButtonTonalityX, getPaddingTop() + 5 + 3 * sizeButtonTonalityY, myPaint);
        myPaint.setColor(Color.rgb(220,220,220));
        canvas.drawRect(getPaddingLeft() + 5, getPaddingTop() + 5 + 1 * sizeButtonTonalityY, getPaddingLeft() + 5 + sizeButtonTonalityX, getPaddingTop() + 5 + 2 * sizeButtonTonalityY, myPaint);
        myPaint.setTextSize(48);
        myPaint.setColor(Color.BLACK);
        canvas.drawText(tonalityToString(), getPaddingLeft() + (int)(0.5 * sizeButtonTonalityX) - 20, getPaddingTop() - 30 + 2 * sizeButtonTonalityY, myPaint);
        canvas.drawText("+", getPaddingLeft() + (int)(0.5 * sizeButtonTonalityX) - 8, getPaddingTop() - 30 + 1 * sizeButtonTonalityY, myPaint);
        canvas.drawText("-", getPaddingLeft() + (int)(0.5 * sizeButtonTonalityX) - 8, getPaddingTop() - 30 + 3 * sizeButtonTonalityY, myPaint);
    }
    private String tonalityToString(){
        String[] str = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        return str[tonality % 12];
    }
    private void printChords(Canvas canvas){
        String[] chordNames = {"I", "ii", "iii", "IV", "V", "vi"};
        for(int i = 0; i< chordData.size(); i++) {
            myPaint.setColor(Color.rgb(chordColour[chordData.get(i).getType()][0], chordColour[chordData.get(i).getType()][1], chordColour[chordData.get(i).getType()][2]));
            canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), myPaint);
            myPaint.setColor(Color.rgb(chordColourWhithed[chordData.get(i).getType()][0], chordColourWhithed[chordData.get(i).getType()][1], chordColourWhithed[chordData.get(i).getType()][2]));
            canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), myPaint);
            canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight(), myPaint);
            if (touchState == 5 && touchObjectId == i) {
                if (touchSwitchChord > 0) {
                    myPaint.setColor(Color.rgb(chordColour[chordData.get(i).getNextType()][0], chordColour[chordData.get(i).getNextType()][1], chordColour[chordData.get(i).getNextType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight + touchSwitchChord, myPaint);
                    myPaint.setColor(Color.rgb(chordColourWhithed[chordData.get(i).getNextType()][0], chordColourWhithed[chordData.get(i).getNextType()][1], chordColourWhithed[chordData.get(i).getNextType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight + touchSwitchChord, myPaint);
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - chordRectHeight, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight() - chordRectHeight + touchSwitchChord, myPaint);
                } else {
                    myPaint.setColor(Color.rgb(chordColour[chordData.get(i).getPreviousType()][0], chordColour[chordData.get(i).getPreviousType()][1], chordColour[chordData.get(i).getPreviousType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() + touchSwitchChord, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), myPaint);
                    myPaint.setColor(Color.rgb(chordColourWhithed[chordData.get(i).getPreviousType()][0], chordColourWhithed[chordData.get(i).getPreviousType()][1], chordColourWhithed[chordData.get(i).getPreviousType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() + touchSwitchChord, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f + chordData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), myPaint);
                    canvas.drawRect((getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() + touchSwitchChord, (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight(), myPaint);
                }
            }
            myPaint.setColor(Color.rgb(0, 0, 0));
            myPaint.setTextSize(32);
            canvas.drawText(chordNames[chordData.get(i).getType()], (getWidth() / 2) + chordData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f) + chordData.get(i).getLength() * zoom / 120000f, getHeight() - chordRectHeight / 2, myPaint);
        }
    }
    private void printAudios(Canvas canvas){
        myPaint.setColor(Color.rgb(10,70,255));
        for(int i = 0; i<audioData.size(); i++){
            canvas.drawRect((getWidth() / 2) + audioData.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + i * trackHeight, (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f + audioData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (trackHeight-5) + i * trackHeight, myPaint);
        }
        if(isRecoding){
            myPaint.setColor(Color.rgb(230,30,30));
            canvas.drawRect((getWidth() / 2) + audioData.get(audioData.size()-1).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (audioData.size()-1) * trackHeight, (getWidth() / 2), getPaddingTop() + 55 + (trackHeight - 5) + (audioData.size()-1) * trackHeight, myPaint);
        } else {
            if(audioData.size()>0){
                canvas.drawRect((getWidth() / 2) + audioData.get(audioData.size()-1).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (audioData.size()-1) * trackHeight, (getWidth() / 2) + audioData.get(audioData.size()-1).getDelay() * zoom / 60000f + audioData.get(audioData.size()-1).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (trackHeight-5) + (audioData.size()-1) * trackHeight, myPaint);

            }
        }
        myPaint.setColor(Color.rgb(10,70,255));
    }
    public void addNewAudio(){
        isRecoding = true;
        int newID = getNextAudioID();
        audioData.add(new AudioNoteData((int)cursorPosition));
        if(getHeight() < trackHeight * audioData.size() + 55 + getPaddingTop() + getPaddingBottom() + chordRectHeight){
            trackHeight = (getHeight() - chordRectHeight - 55 - getPaddingTop() - getPaddingBottom()) / audioData.size();
        }
        audioData.get(audioData.size()-1).setId(newID);
    }
    public void finishAddingNewAudio(){
        if(isRecoding){
            audioData.get(audioData.size()-1).setLength((int)cursorPosition - audioData.get(audioData.size()-1).getDelay());
            audioData.get(audioData.size()-1).stopRecording();
        }
        isRecoding = false;
    }
    public int getNextAudioID(){
        int id = 1;
        if(audioData.size()>0){
            id = audioData.get(audioData.size() - 1).getId() + 1;
        }
        return id;
    }
    public void play(String path, boolean isRec){
        midiDriver.start();

        int maxValueOfAudio;
        if(isRec){
            maxValueOfAudio = audioData.size() - 1;
        }
        else {
            maxValueOfAudio = audioData.size();
        }
        for(int i = 0; i<maxValueOfAudio; i++){
            audioData.get(i).play((int)cursorPosition, path);
        }
        for(int i = 0; i< chordData.size(); i++){
            chordData.get(i).play((int)cursorPosition, tonality, midiDriver);
        }
        isPlaying = true;
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cursorPosition += 20;
                postInvalidate();
            }
        }, 20, 20);
    }
    public void stop(){
        for(int i = 0; i<audioData.size(); i++){
            audioData.get(i).stop();
        }
        isPlaying = false;
        timer.cancel();
        timer.purge();


        midiDriver.stop();
    }
    private float getStepBlackNote(int zoom, int tempo, int signNote){
        return (zoom/(float)tempo)/* / (float)signNote*/;
    }
    private void printTempo(Canvas canvas, int signNb, int signNote, int tempo, int zoom){
        float step = getStepBlackNote(zoom, tempo, signNote);
        float tpos = 0;
        int counter = 0;
        myPaint.setColor(Color.rgb(200,200,200));
        float dec = step * (-cursorPosition * tempo/1000f)/60f + getWidth() / 2;
        for(tpos = dec; tpos<getWidth(); tpos += step){
            canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), myPaint);
            if(counter % signNb == 0){
                myPaint.setColor(Color.rgb(130,130,130));
                canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), myPaint);
                myPaint.setColor(Color.rgb(200,200,200));
            }
            counter += 1;
        }
        counter = 0;
        for(tpos = dec; tpos>0; tpos -= step){
            canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), myPaint);
            if(counter % signNb == 0){
                myPaint.setColor(Color.rgb(130,130,130));
                canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), myPaint);
                myPaint.setColor(Color.rgb(200,200,200));
            }
            counter += 1;
        }

    }
    private void printMetronome(Canvas canvas){
        float moduloOnTemps = (Math.abs(cursorPosition) % (int)(60000.0/tempo));
        if(moduloOnTemps < 100 && isRecoding){
            int alpha = (int)(Math.cos(moduloOnTemps / 20 / Math.PI) * 255);
            myPaint.setColor(Color.argb(alpha,10,70,255));
            canvas.drawRect(0,0,getWidth(),getHeight(), myPaint);
            myPaint.setColor(Color.argb(alpha,250,50,10));
        }
    }
    public void setAudioData(ArrayList<AudioNoteData> input){
        this.audioData = input;
        postInvalidate();
    }
    public void setChordData(ArrayList<ChordData> input){
        this.chordData = input;
        postInvalidate();
    }
    public void setPath(String path){
        this.path = path;
        int ton = 60;
        try {
            FileInputStream fin = new FileInputStream(path + "/.tonality");
            DataInputStream din = new DataInputStream(fin);
            ton = din.readInt();
            fin.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
        tonality = ton;
    }
    public void deleteAudioFile(int fileId){
        File fToDelete = new File(path + "/" + fileId + ".3gp");
        fToDelete.delete();
    }
    public void saveTonality() throws IOException{
        try{
            FileOutputStream fos =  new FileOutputStream(path + "/.tonality");
            DataOutputStream out = new DataOutputStream(fos);
            out.writeInt(tonality);
            out.close();
            fos.close();
        }
        catch(FileNotFoundException fnfe){
            throw(fnfe);
        }
        catch(IOException ioe){
            throw(ioe);
        }
    }
    public void saveAudioData() throws IOException {
        try{
            FileOutputStream fos =  new FileOutputStream(path + "/.notes");
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(audioData);
            out.close();
            fos.close();
        }
        catch(FileNotFoundException fnfe){
            throw(fnfe);
        }
        catch(IOException ioe){
            throw(ioe);
        }
    }
    public void saveChordData() throws IOException {
        try{
            FileOutputStream fos =  new FileOutputStream(path + "/.chords");
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(chordData);
            out.close();
            fos.close();
        }
        catch(FileNotFoundException fnfe){
            throw(fnfe);
        }
        catch(IOException ioe){
            throw(ioe);
        }
    }
}
