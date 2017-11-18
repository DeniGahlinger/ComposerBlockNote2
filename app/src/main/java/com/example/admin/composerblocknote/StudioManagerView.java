package com.example.admin.composerblocknote;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: document your custom view class.
 */
public class StudioManagerView extends View {
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    List<RectShape> records = new ArrayList<RectShape>();

    private boolean isRecoding = false;

    public float mouseX = -1;
    public float mouseY = -1;

    private int trackHeight = 80;

    private Paint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private float cursorPosition = 0;
    private int tempo = 120;
    private int zoom = 6000;
    private boolean isPlaying = false;
    private Timer timer = new Timer();
    public ArrayList<AudioNoteData> audioData = new ArrayList<AudioNoteData>();

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
                        mouseX = -1;
                        mouseY = -1;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mouseX = -1;
                        mouseY = -1;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //cursorPosition += (mouseX - event.getX())*zoom / 1800f;

                        if(mouseX != -1 && isPlaying == false){
                            boolean scroll = true;
                            for(int i = 0; i<audioData.size(); i++){
                                if(event.getX()>(getWidth() / 2) + audioData.get(i).getDelay()* zoom / 60000f - (cursorPosition * zoom / 60000f)){
                                    if(event.getX()<(getWidth() / 2) + audioData.get(i).getDelay() * zoom / 60000f + audioData.get(i).getLength() * zoom / 60000f - (cursorPosition * zoom / 60000f)){
                                        if(event.getY()>getPaddingTop() + 55 + i * trackHeight){
                                            if(event.getY()<getPaddingTop() + 55 + (trackHeight-5) + i * trackHeight){
                                                scroll=false;
                                                audioData.get(i).setDelay(audioData.get(i).getDelay() - (int)((mouseX - event.getX()) * 60000 / zoom));
                                            }
                                        }
                                    }
                                }
                            }
                            if(scroll == true){
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
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
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
        printTempo(canvas, 4, 4, tempo, zoom);
        mTextPaint.setColor(Color.rgb(20,20,90));
        canvas.drawRect((getWidth() / 2) - (cursorPosition * zoom / 60000f) - 2, 0, (getWidth() / 2) - (cursorPosition * zoom / 60000f) + 2, getHeight(), mTextPaint);
        mTextPaint.setColor(Color.rgb(200,200,200));
        printAudios(canvas);
        mTextPaint.setColor(Color.argb(200,30,255,0));
        canvas.drawRect((getWidth() / 2) - 25, paddingTop, (getWidth() / 2) + 25, paddingTop + 50, mTextPaint);
        canvas.drawRect((getWidth() / 2) - 5, 0, (getWidth() / 2) + 5, getHeight(), mTextPaint);
        mTextPaint.setColor(Color.GREEN);



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
    }
    public void finishAddingNewAudio(){
        isRecoding = false;
        audioData.get(audioData.size()-1).setLength((int)cursorPosition - audioData.get(audioData.size()-1).getDelay());
    }
    public int getNextAudioID(){
        return audioData.size();
    }
    public void play(String path, boolean isRec){
        int maxValueOfAudio;
        if(isRec){
            maxValueOfAudio = audioData.size() - 1;
        }
        else {
            maxValueOfAudio = audioData.size();
        }
        for(int i = 0; i<maxValueOfAudio; i++){
            audioData.get(i).play((int)cursorPosition,path,i);
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
    public String getExampleString() {
        return mExampleString;
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
