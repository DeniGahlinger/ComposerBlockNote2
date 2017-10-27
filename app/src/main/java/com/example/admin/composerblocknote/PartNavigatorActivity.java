package com.example.admin.composerblocknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class PartNavigatorActivity extends AppCompatActivity {

    ListView lvwParts;
    Button btnAdd;
    private List<String> partName = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_part_navigator);
        lvwParts = (ListView) findViewById(R.id.lvwPart);
        btnAdd = (Button) findViewById(R.id.btnAddPart);

        final ArrayAdapter<String> adapterlst = new ArrayAdapter<String>(
                PartNavigatorActivity.this,
                android.R.layout.simple_list_item_1,
                partName
        );
        lvwParts.setAdapter(adapterlst);
        lvwParts.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PartNavigatorActivity.this, PartNavigatorActivity.class);
                intent.putExtra("songName", partName.get(position));
                startActivity(intent);
            }
        });
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                partName.add("a");
                adapterlst.notifyDataSetChanged();
                //Intent newSong = new Intent(this, );
            }
        });
    }
}
