package com.example.admin.composerblocknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PartNavigatorActivity extends AppCompatActivity {

    ListView lvwParts;
    Button btnAdd;
    private List<String> partName = new ArrayList<String>();
    private List<String> partPath = new ArrayList<String>();
    private String songName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_navigator);
        lvwParts = (ListView) findViewById(R.id.lvwPart);
        btnAdd = (Button) findViewById(R.id.btnAddPart);
        String songDirName = getIntent().getStringExtra("mainDir");
        FileManager fm = new FileManager(this.getBaseContext(), this);
        File baseFolder = fm.getMusicStorageDir(songDirName);
        File[] files = baseFolder.listFiles();
        File[] parts = null;
        File songFolder = null;
        for (File f : files) {
            songName = getIntent().getStringExtra("songName");
            if (f.getName().equals(getIntent().getStringExtra("songName"))) {
                parts = f.listFiles();
                songFolder = f;
                break;
            }
        }
        for (File f : parts) {
            if (!f.getName().equals(".meta")) {
                partName.add(f.getName());
                partPath.add(f.getAbsolutePath());
            }
        }
        File partFolder = null;
        final ArrayAdapter<String> adapterlst = new ArrayAdapter<String>(
                PartNavigatorActivity.this,
                android.R.layout.simple_list_item_1,
                partName
        );

        lvwParts.setAdapter(adapterlst);
        final File[] finalParts = parts;
        lvwParts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PartNavigatorActivity.this, StudioActivity.class);
               intent.putExtra("songName", partName.get(position)); // Part name! Bad name.
                intent.putExtra("songSongName", songName); // Part name! Bad name.
                intent.putExtra("currentPath", partPath.get(position));
                intent.putExtra("newPart", false);
                startActivity(intent);
            }
        });

        final File finalSongFolder = songFolder;

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PartNavigatorActivity.this, InitNoteActivity.class);
                intent.putExtra("isNewSong", "no");
                File mainFolder = (File) intent.getExtras().get("InitMainDir");
                intent.putExtra("mainDir", mainFolder);
                intent.putExtra("songName", songName);
                intent.putExtra("songFolder", finalSongFolder);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {

        switch(keyCode) {
            case(KeyEvent.KEYCODE_BACK):
                Intent intent = new Intent(this, MainActivity.class);

                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }
}
