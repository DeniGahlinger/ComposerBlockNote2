package com.example.admin.composerblocknote;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.billthefarmer.mididriver.MidiDriver;

/**
 * TODO: document your custom view class.
 */
public class StudioManagerView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    List<RectShape> records = new ArrayList<RectShape>();

    private int MAX_DELAY_FOR_DOUBLE_TAP = 250;
    private long firstTapTimeOfDoubleTap = System.currentTimeMillis();

    private boolean isRecoding = false;

    public float mouseX = -1;
    public float mouseY = -1;

    private int[][] chordCoulours =  {{10,30,200},{170,80,240},{250,140,0},{10,100,25},{240,240,30},{200,30,20}};
    private int[][] chordCouloursWhithed =  {{15,45,250},{210,120,255},{255,190,60},{20,150,45},{255,255,80},{255,100,60}};

    private int trackHeight = 80;

    private Paint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private float cursorPosition = 0;
    private int tempo = 120;
    private int signatureNb = 4;
    private int signatureNote = 4;
    private int zoom = 6000;
    private boolean isPlaying = false;
    private Timer timer = new Timer();
    public ArrayList<AudioNoteData> audioData = new ArrayList<AudioNoteData>();
    private ArrayList<ChordData> chordDatas = new ArrayList<ChordData>();

    MidiDriver midiDriver = new MidiDriver();
    private int otherDatasSize = 125;
    private int rectChordModifierSize = 40;
    private int touchState = 0; // 0 : none, 1 : scroll, 2 : displace audio, 3 : displace midi, 4 : resizing midi, 5 switch chord
    private int touchObjectId = -1;
    private int touchSwitchChord = 0;
    private int isDeletingChord = -1;

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
        // Load attributes


        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.StudioManagerView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.StudioManagerView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.StudioManagerView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.StudioManagerView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.StudioManagerView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.StudioManagerView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new Paint();
        //mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        //mTextPaint.setTextAlign(Paint.Align.LEFT);

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
                        if(System.currentTimeMillis() - firstTapTimeOfDoubleTap < MAX_DELAY_FOR_DOUBLE_TAP){
                            if(event.getY()> getPaddingTop() - 55 + getHeight() - otherDatasSize + rectChordModifierSize){
                                for(int i = 0; i<chordDatas.size(); i++){
                                    if(event.getX()>(getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f)){
                                        if(event.getX()<(getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize){
                                            isDeletingChord = i;
                                        }
                                    }
                                }
                            }
                            doubleTap((int)event.getX());
                        }
                        else{
                            isDeletingChord = -1;
                        }
                        firstTapTimeOfDoubleTap = System.currentTimeMillis();
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
                        //cursorPosition += (mouseX - event.getX())*zoom / 1800f;

                        if(mouseX != -1 && isPlaying == false){
                            if(touchState == 0 || touchState == 2) {
                                for (int i = 0; i < audioData.size(); i++) {
                                    if (event.getX() > (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f - (cursorPosition * zoom / 60000f)) {
                                        if (event.getX() < (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f + audioData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f)) {
                                            if (event.getY() > getPaddingTop() + 55 + i * trackHeight) {
                                                if (event.getY() < getPaddingTop() + 55 + (trackHeight - 5) + i * trackHeight) {
                                                    touchState = 2;
                                                    touchObjectId = i;
                                                    audioData.get(i).setDelay(audioData.get(i).getDelay() - (int) ((mouseX - event.getX()) * 60000 / zoom));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if(event.getY()> getPaddingTop() - 55 + getHeight() - otherDatasSize || touchState == 3 || touchState == 4 || touchState == 5){
                                for(int i = 0; i<chordDatas.size(); i++){
                                    if(event.getX()>(getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize){
                                        if((touchState == 0 || touchState == 3 || touchState == 5) && ((touchObjectId == i && (touchState == 3 || touchState == 5)) || (touchState == 0 && event.getX()<(getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize))){
                                            if(Math.abs(mouseY - event.getY())>Math.abs(mouseX - event.getX())){
                                                touchState=5;
                                                touchObjectId = i;
                                                touchSwitchChord -= mouseY - event.getY();
                                                if(touchSwitchChord > otherDatasSize / 2){
                                                    chordDatas.get(i).upType();
                                                    touchSwitchChord = touchSwitchChord - otherDatasSize;
                                                } else if(touchSwitchChord < -otherDatasSize / 2){
                                                    chordDatas.get(i).downType();
                                                    touchSwitchChord = touchSwitchChord + otherDatasSize;
                                                }
                                            } else {
                                                touchState=3;
                                                touchObjectId = i;
                                                chordDatas.get(i).displace(-(int)((mouseX - event.getX()) * 60000 / zoom));
                                            }

                                        }
                                        else if((touchState == 0 || touchState == 4) && ((touchObjectId == i * 2 && touchState == 4) || (touchState == 0 && event.getX()<(getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f)))){

                                            touchState=4;
                                            touchObjectId = i * 2;
                                            chordDatas.get(i).setEnd(-(int)((mouseX - event.getX()) * 60000 / zoom));
                                        }
                                    } else if((touchState == 0 || touchState == 4) && ((touchObjectId == i * 2 + 1 && touchState == 4) || (touchState == 0 && event.getX()>(getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f)))){
                                        touchState=4;
                                        touchObjectId = i * 2 + 1;
                                        chordDatas.get(i).setBegin(-(int)((mouseX - event.getX()) * 60000 / zoom));
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
                //Log.d("TOUCHSTATE", "touchState : " + touchState + ", objID : " + touchObjectId);
                return true;

            }
        });
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }
    public void doubleTap(int mouseX){
        if(isDeletingChord == -1){
            chordDatas.add(new ChordData((mouseX - getWidth() / 2) * 60000 / zoom + (int)(cursorPosition) - 500, (mouseX - getWidth() / 2) * 60000 / zoom + (int)(cursorPosition) + 500, 1));
        } else {
            chordDatas.remove(isDeletingChord);
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
    public void openExistingPartData(String path) {
        try {
            //Je sais pas si c'est nécessaire mais je sais plus ce qu'on avait dit .. En totu cas là ça fait une exception surtout

            FileInputStream fis = new FileInputStream(path + "/.meta");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void invalidateTextPaintAndMeasurements() {
        //mTextPaint.setTextSize(mExampleDimension);
        //mTextPaint.setColor(mExampleColor);
        //mTextWidth = mTextPaint.measureText(mExampleString);

        //Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        /*canvas.drawText(mExampleString,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);*/

        // Draw the example drawable on top of the text.
        /*if (mExampleDrawable != null) {
            mExampleDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mExampleDrawable.draw(canvas);
        }*/
        printTempo(canvas, signatureNb, signatureNote, tempo, zoom);
        mTextPaint.setColor(Color.rgb(20,20,90));
        canvas.drawRect((getWidth() / 2) - (cursorPosition * zoom / 60000f) - 2, 0, (getWidth() / 2) - (cursorPosition * zoom / 60000f) + 2, getHeight(), mTextPaint);
        mTextPaint.setColor(Color.rgb(200,200,200));
        printAudios(canvas);
        printChords(canvas);
        mTextPaint.setColor(Color.argb(200,30,255,0));
        canvas.drawRect((getWidth() / 2) - 25, paddingTop, (getWidth() / 2) + 25, paddingTop + 50, mTextPaint);
        canvas.drawRect((getWidth() / 2) - 5, 0, (getWidth() / 2) + 5, getHeight(), mTextPaint);
        mTextPaint.setColor(Color.GREEN);

        printMetronome(canvas);


    }
    private void printChords(Canvas canvas){
        String[] chordNames = {"I", "ii", "iii", "IV", "V", "vi"};
        for(int i = 0; i<chordDatas.size(); i++){
            mTextPaint.setColor(Color.rgb(chordCoulours[chordDatas.get(i).getType()][0],chordCoulours[chordDatas.get(i).getType()][1],chordCoulours[chordDatas.get(i).getType()][2]));
            canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), mTextPaint);
            mTextPaint.setColor(Color.rgb(chordCouloursWhithed[chordDatas.get(i).getType()][0],chordCouloursWhithed[chordDatas.get(i).getType()][1],chordCouloursWhithed[chordDatas.get(i).getType()][2]));
            canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), mTextPaint);
            canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight(), mTextPaint);
            if(touchState == 5 && touchObjectId == i){
                if(touchSwitchChord > 0){
                    mTextPaint.setColor(Color.rgb(chordCoulours[chordDatas.get(i).getNextType()][0],chordCoulours[chordDatas.get(i).getNextType()][1],chordCoulours[chordDatas.get(i).getNextType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize + touchSwitchChord, mTextPaint);
                    mTextPaint.setColor(Color.rgb(chordCouloursWhithed[chordDatas.get(i).getNextType()][0],chordCouloursWhithed[chordDatas.get(i).getNextType()][1],chordCouloursWhithed[chordDatas.get(i).getNextType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize + touchSwitchChord, mTextPaint);
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() - otherDatasSize, (getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight() - otherDatasSize + touchSwitchChord, mTextPaint);
                } else {
                    mTextPaint.setColor(Color.rgb(chordCoulours[chordDatas.get(i).getPreviousType()][0],chordCoulours[chordDatas.get(i).getPreviousType()][1],chordCoulours[chordDatas.get(i).getPreviousType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() + touchSwitchChord, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), mTextPaint);
                    mTextPaint.setColor(Color.rgb(chordCouloursWhithed[chordDatas.get(i).getPreviousType()][0],chordCouloursWhithed[chordDatas.get(i).getPreviousType()][1],chordCouloursWhithed[chordDatas.get(i).getPreviousType()][2]));
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f) - rectChordModifierSize, getHeight() + touchSwitchChord, (getWidth() / 2) + chordDatas.get(i).getDelay() * zoom / 60000f + chordDatas.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight(), mTextPaint);
                    canvas.drawRect((getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getHeight() + touchSwitchChord, (getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + rectChordModifierSize, getHeight(), mTextPaint);
                }
            }
            mTextPaint.setColor(Color.rgb(0,0,0));
            
            canvas.drawText(chordNames[chordDatas.get(i).getType()], (getWidth() / 2) + chordDatas.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f) + chordDatas.get(i).getLength() * zoom / 120000f, getHeight() - otherDatasSize / 2, mTextPaint);
        }
    }
    private void printAudios(Canvas canvas){
        mTextPaint.setColor(Color.rgb(10,70,255));
        for(int i = 0; i<audioData.size(); i++){
            canvas.drawRect((getWidth() / 2) + audioData.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + i * trackHeight, (getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f + audioData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (trackHeight-5) + i * trackHeight, mTextPaint);
        }
        if(isRecoding){
            mTextPaint.setColor(Color.rgb(230,30,30));
            canvas.drawRect((getWidth() / 2) + audioData.get(audioData.size()-1).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (audioData.size()-1) * trackHeight, (getWidth() / 2), getPaddingTop() + 55 + (trackHeight - 5) + (audioData.size()-1) * trackHeight, mTextPaint);
        } else {
            if(audioData.size()>0){
                canvas.drawRect((getWidth() / 2) + audioData.get(audioData.size()-1).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (audioData.size()-1) * trackHeight, (getWidth() / 2) + audioData.get(audioData.size()-1).getDelay() * zoom / 60000f + audioData.get(audioData.size()-1).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f), getPaddingTop() + 55 + (trackHeight-5) + (audioData.size()-1) * trackHeight, mTextPaint);

            }
        }
        mTextPaint.setColor(Color.rgb(10,70,255));
    }
    public void addNewAudio(){
        isRecoding = true;
        audioData.add(new AudioNoteData((int)cursorPosition));
        if(getHeight() < trackHeight * audioData.size() + 55 + getPaddingTop() + getPaddingBottom()){
            trackHeight = (getHeight() - otherDatasSize - 55 - getPaddingTop() - getPaddingBottom()) / audioData.size();
        }
    }
    public void finishAddingNewAudio(){
        if(isRecoding){
            audioData.get(audioData.size()-1).setLength((int)cursorPosition - audioData.get(audioData.size()-1).getDelay());
        }
        isRecoding = false;
    }
    public int getNextAudioID(){
        return audioData.size();
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
            audioData.get(i).play((int)cursorPosition, path, i);
        }
        for(int i = 0; i<chordDatas.size(); i++){
            chordDatas.get(i).play((int)cursorPosition, 60, midiDriver);
        }
        isPlaying = true;
        timer.cancel();
        timer.purge();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cursorPosition += 20;
                //invalidate();
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
        mTextPaint.setColor(Color.rgb(200,200,200));
        float dec = step * (-cursorPosition * tempo/1000f)/60f + getWidth() / 2;
        for(tpos = dec; tpos<getWidth(); tpos += step){
            canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), mTextPaint);
            if(counter % signNb == 0){
                mTextPaint.setColor(Color.rgb(130,130,130));
                canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), mTextPaint);
                mTextPaint.setColor(Color.rgb(200,200,200));
            }
            counter += 1;
        }
        counter = 0;
        for(tpos = dec; tpos>0; tpos -= step){
            canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), mTextPaint);
            if(counter % signNb == 0){
                mTextPaint.setColor(Color.rgb(130,130,130));
                canvas.drawRect(tpos - 1, 0, tpos + 1, getHeight(), mTextPaint);
                mTextPaint.setColor(Color.rgb(200,200,200));
            }
            counter += 1;
        }

    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    private void printMetronome(Canvas canvas){
        float moduloOnTemps = (Math.abs(cursorPosition) % (int)(60000.0/tempo));
        if(moduloOnTemps < 100 && isRecoding){
            int alpha = (int)(Math.cos(moduloOnTemps / 20 / Math.PI) * 255);
            mTextPaint.setColor(Color.argb(alpha,10,70,255));
            canvas.drawRect(0,0,getWidth(),getHeight(),mTextPaint);
            mTextPaint.setColor(Color.argb(alpha,250,50,10));
        }
    }
    public String getExampleString() {
        return mExampleString;
    }

    public ArrayList<AudioNoteData> getAudioData(){
        return audioData;
    }
    public void setAudioData(ArrayList<AudioNoteData> input){
        this.audioData = input;
        postInvalidate();
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */


    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
