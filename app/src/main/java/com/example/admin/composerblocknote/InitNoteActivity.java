package com.example.admin.composerblocknote;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;

public class InitNoteActivity extends AppCompatActivity
{
    final List<String> tempoList = new ArrayList<String>();
    Spinner spTemp;
    Spinner spSign;
    EditText textSongName;
    EditText textPartName;
    private String songName;

    ArrayAdapter<String> adp1;
    boolean isNewSong;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_note);

        Intent intent = getIntent();
        String str = intent.getStringExtra("isNewSong");
        isNewSong = intent.getStringExtra("isNewSong").equals("yes");

        spTemp = (Spinner) findViewById(R.id.spnTempo);
        spSign = (Spinner) findViewById(R.id.spnSignature);
        textSongName = (EditText) findViewById((R.id.tbxSongName));
        textPartName = (EditText) findViewById(R.id.tbxPartName);

        final Spinner spSign2 = (Spinner) findViewById(R.id.spnSign1);
        initLists();
        adp1 = new ArrayAdapter<String>(
                InitNoteActivity.this,
                android.R.layout.simple_list_item_1,
                tempoList);

        adp1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTemp.setAdapter(adp1);

        if (str.equals("yes"))
        {
            songName = this.getIntent().getExtras().getString("songName");
            spTemp.setSelection(100);
            spSign.setSelection(2);
            spSign2.setSelection(3);
        } else
        {
            spTemp.setVisibility(View.INVISIBLE);
            spSign.setVisibility(View.INVISIBLE);
            spSign2.setVisibility(View.INVISIBLE);
            findViewById(R.id.textView).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView2).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView3).setVisibility(View.INVISIBLE);
            findViewById(R.id.tbxSongName).setVisibility(View.INVISIBLE);
        }
        findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener()
        {
            //creating folder and writing metadata
            @Override
            public void onClick(View v)
            {

                File baseFolder = (File) getIntent().getExtras().get("mainDir");
                File songFolder = null;
                File partFolder = null;
                if (isNewSong)
                {
                    songFolder = new File(baseFolder.getAbsolutePath() + "/" + textSongName.getText());
                    partFolder = new File(songFolder.getAbsolutePath() + "/" + textPartName.getText());

                    if (songFolder.mkdir())
                    {
                        partFolder.mkdir();
                        String content = spTemp.getSelectedItem().toString() + ";" + spSign.getSelectedItem().toString() + ";" + spSign2.getSelectedItem().toString();
                        System.out.println("NRV - " + content);
                        try
                        {
                            FileOutputStream fos = new FileOutputStream(songFolder.getAbsoluteFile() + "/.meta");

                            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                            bw.write(content);
                            bw.flush();
                            bw.close();
                            fos.close();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    } else
                    {
                        Log.d("NRV", "Wasn't able to create folder .. ");
                    }
                    Intent intent = new Intent(InitNoteActivity.this, StudioActivity.class);
                    intent.putExtra("currentPath", partFolder.getAbsolutePath());
                    intent.putExtra("newPart", true);
                    songName = textSongName.getText().toString();
                    intent.putExtra("songSongName", songName);
                    startActivity(intent);
                } else // Is NOT new song.
                {
                    songFolder = (File) getIntent().getExtras().get("songFolder");
                    partFolder = new File(songFolder.getAbsolutePath() + "/" + textPartName.getText());
                    if (partFolder.exists())
                    {
                    } else
                    {
                        partFolder.mkdir();
                        File metadataP = new File(partFolder.getAbsolutePath() + "/.meta");
                        metadataP.delete();
                        String str = "";
                        try
                        {
                            metadataP.createNewFile();
                            metadataP.setWritable(true);
                            FileOutputStream fos = new FileOutputStream(metadataP);
                            fos.write(str.getBytes());
                            fos.flush();
                            fos.close();
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(InitNoteActivity.this, StudioActivity.class);
                        intent.putExtra("currentPath", partFolder.getAbsolutePath());
                        intent.putExtra("newPart", true);
                        songName = getIntent().getExtras().getString("songName");
                        intent.putExtra("songSongName", songName);

                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void initLists()
    {
        for (int i = 20; i <= 240; i++)
        {
            tempoList.add(i + "");
        }
    }
}
