package com.example.admin.composerblocknote;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by guill on 22/12/2017.
 */

public class MainActivityListAdapter<T> extends ArrayAdapter {
    private List<T> array;
    private int layoutResourceId;
    private Context context;
    Button deleteButton;

    public MainActivityListAdapter(@NonNull Context context, @LayoutRes int resource, List<T> a) {
        super(context, resource, a);
        this.context = context;
        this.layoutResourceId = resource;
        this.array = a;
    }
    public static class SongHolder {
        TextView name;
        TextView partNumber;
        ImageButton removeButton;
    }
}
