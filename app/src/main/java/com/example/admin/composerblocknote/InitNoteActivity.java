package com.example.admin.composerblocknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InitNoteActivity extends AppCompatActivity {

    final List<String> tempoList = new ArrayList<String>();
    Spinner spTemp;
    Spinner spSign;
    ArrayAdapter<String> adp1;
    boolean isNewSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_note);

        Intent intent = getIntent();
        String str = intent.getStringExtra("isNewSong");
        isNewSong = true;
        spTemp = (Spinner) findViewById(R.id.spnTempo);
        spSign = (Spinner) findViewById(R.id.spnSignature);
        final Spinner spSign2 = (Spinner) findViewById(R.id.spnSign1);
        initLists();
        adp1 = new ArrayAdapter<String>(
                InitNoteActivity.this,
                android.R.layout.simple_list_item_1,
                tempoList);

        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTemp.setAdapter(adp1);
        if(str.equals("yes")){
            spTemp.setSelection(100);
            spSign.setSelection(2);
            spSign2.setSelection(3);
        }
        else {
            spTemp.setVisibility(View.INVISIBLE);
            spSign.setVisibility(View.INVISIBLE);
            spSign2.setVisibility(View.INVISIBLE);
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
            findViewById(R.id.tbxSongName).setVisibility(View.INVISIBLE);
        }
    }

    private void initLists(){
        for(int i = 20; i<=240; i++){
            tempoList.add(i + "");
        }
    }
}
