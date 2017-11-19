package com.example.admin.composerblocknote;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.System.in;

public class MainActivity extends AppCompatActivity {

    ListView lvwSongs;
    Button btnAdd;
    private List<String> songName = new ArrayList<String>();
    private String mainDirName = "ComposerBlockNote";
    private File baseFolder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvwSongs = (ListView) findViewById(R.id.lvwSongs);
        btnAdd = (Button) findViewById(R.id.btnAdd);

        final ArrayAdapter<String> adapterlst = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.simple_list_item_1,
                songName
        );
        lvwSongs.setAdapter(adapterlst);

        // open a folder
        FileManager fm = new FileManager(this.getBaseContext(),this);
        baseFolder = fm.getMusicStorageDir(mainDirName);
        if (baseFolder == null){
            baseFolder = fm.createMusicStorageDir(mainDirName);
        }
        File[] files = baseFolder.listFiles();
        for (File f : files){
            songName.add(f.getName());
        }

        lvwSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PartNavigatorActivity.class);
                intent.putExtra("songName", songName.get(position));
                intent.putExtra("mainDir", mainDirName);
                intent.putExtra("initMainDir", baseFolder);
                startActivity(intent);
            }
        });


        lvwSongs.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                System.out.println("NRV - songname(pos) = " +  songName.get(pos));
                for (File f : baseFolder.listFiles()){
                    if (f.getName().equals(songName.get(pos))){
                        delR(f);
                        songName.remove(pos);
                        System.out.println("NRV - deleting " + f.getName());
                        ((BaseAdapter)lvwSongs.getAdapter()).notifyDataSetChanged();
                    }
                }
                return true;
            }
        });


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InitNoteActivity.class);
                intent.putExtra("mainDir", baseFolder);
                intent.putExtra("isNewSong", "yes");
                startActivity(intent);
                //songName.add("a");
                //adapterlst.notifyDataSetChanged();
                //Intent newSong = new Intent(this, );
            }
        });

    }

    //recursively delete a directory and its childrens
    void delR(File f) {
        if (f.isDirectory())
            for (File child : f.listFiles())
                delR(child);
        f.delete();
    }
}
