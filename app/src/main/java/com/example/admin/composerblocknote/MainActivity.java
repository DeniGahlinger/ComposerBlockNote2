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
import java.util.Iterator;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;
import static com.example.admin.composerblocknote.R.id.listItemString;
import static java.lang.System.in;

public class MainActivity extends AppCompatActivity {

    //ListView lvwSongs;
    ListViewCB lvwSongs;
    Button btnAdd;
    Button btnDelete;
    Button btnCancel;
    private List<String> songName = new ArrayList<String>();
    private String mainDirName = "ComposerBlockNote";
    private File baseFolder;
    private boolean checkBoxesVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lvwSongs = (ListViewCB) findViewById(R.id.lvwSongs);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnDelete = (Button) findViewById(R.id.deleteButton);
        btnCancel = (Button) findViewById(R.id.cancelDeleteButton);

        final ArrayAdapter<String> adapterlst = new ArrayAdapter<String>(
                MainActivity.this,
                //android.R.layout.simple_list_item_1,
                //songName
                R.layout.list_element, R.id.listItemString, songName
        );

        lvwSongs.setAdapter(adapterlst);
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
                if (checkBoxesVisible){
                    lvwSongs.showCheckboxes(false);
                }
                else{
                    lvwSongs.showCheckboxes(true);
                    lvwSongs.setCheckbox(true, pos);
                }
                checkBoxesVisible = !checkBoxesVisible;
                findViewById(R.id.deleteRow).setVisibility(checkBoxesVisible ? View.VISIBLE : View.GONE);
                findViewById(R.id.newRow).setVisibility(checkBoxesVisible ? View.GONE : View.VISIBLE);
                return true;
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            //y'aura sans doute des soucis d'itérateurs comme d'hab avec cette purge de java de mes fesses
            @Override
            public void onClick(View view) {
                ArrayList<Integer> indexes = lvwSongs.getSelectedIndexes();
                ArrayList<String> toRemove = new ArrayList<String>();

                for (int e : indexes){
                    toRemove.add(songName.get(e));
                }

                for (File f : baseFolder.listFiles()){
                    if (toRemove.contains(f.getName())){
                        songName.remove(f.getName());
                        delR(f);
                        ((BaseAdapter)lvwSongs.getAdapter()).notifyDataSetChanged();
                    }
                }

                lvwSongs.showCheckboxes(false);
                checkBoxesVisible = false;
                findViewById(R.id.newRow).setVisibility(View.VISIBLE);
                findViewById(R.id.deleteRow).setVisibility(View.GONE);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                for (int i = 0; i < lvwSongs.getChildCount(); i++){
                    lvwSongs.setCheckbox(false, i);
                }
                checkBoxesVisible = false;
                lvwSongs.showCheckboxes(checkBoxesVisible);
                findViewById(R.id.newRow).setVisibility(View.VISIBLE);
                findViewById(R.id.deleteRow).setVisibility(View.GONE);
            }
        });


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InitNoteActivity.class);
                intent.putExtra("mainDir", baseFolder);
                intent.putExtra("isNewSong", "yes");
                //todo : change startActivity() to startActivityWithResult() to keep track of the back button returns,
                startActivity(intent);

                //startActivityForResult(intent, 0);
                //songName.add("a");
                //adapterlst.notifyDataSetChanged();
                //Intent newSong = new Intent(this, );
            }
        });
        ((BaseAdapter)lvwSongs.getAdapter()).notifyDataSetChanged();
    }


    // Recursively delete a directory and its childrens
    void delR(File f) {
        if (f.isDirectory())
            for (File child : f.listFiles())
                delR(child);
        f.delete();
    }

    public BaseAdapter getSongsAdapter(){
        return (BaseAdapter)lvwSongs.getAdapter();
    }
}
