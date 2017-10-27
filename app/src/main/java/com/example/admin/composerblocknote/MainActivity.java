package com.example.admin.composerblocknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lvwSongs;
    Button btnAdd;
    private List<String> songName = new ArrayList<String>();

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

        lvwSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, PartNavigatorActivity.class);
                intent.putExtra("songName", songName.get(position));
                startActivity(intent);
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InitNoteActivity.class);
                intent.putExtra("isNewSong", "yess");
                startActivity(intent);
                //songName.add("a");
                //adapterlst.notifyDataSetChanged();
                //Intent newSong = new Intent(this, );
            }
        });
    }
}
