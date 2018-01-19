package com.example.admin.composerblocknote;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by narval on 12/01/2018.
 */

class ListViewCB extends ListView {
    public ListViewCB(Context context) {
        super(context);
    }

    public ListViewCB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public ListViewCB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private CheckBox getCheckboxByPosition(int pos){
        View v = getCheckboxByPosition(pos);
        return (CheckBox) v.findViewById(R.id.listItemDeleteCheckbox);
    }

    @Override
    public void deferNotifyDataSetChanged() {
        super.deferNotifyDataSetChanged();
        //todo
    }

    public void showCheckboxes(boolean b){
        for (int i = 0; i < this.getChildCount(); i++){
            getChildAt(i).findViewById(R.id.listItemDeleteCheckbox).setVisibility(b ? VISIBLE : GONE);
        }
    }
    public void setCheckbox(boolean b, int id){
        CheckBox cb = (CheckBox)getChildAt(id).findViewById(R.id.listItemDeleteCheckbox);
        if (cb == null){
            System.out.println("cb null");
        }
        cb.setChecked(b);
    }

    public ArrayList<Integer> getSelectedIndexes(){
        ArrayList<Integer> ary = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++){
            CheckBox cb = (CheckBox)getChildAt(i).findViewById(R.id.listItemDeleteCheckbox);
            if (cb.isChecked()){
                ary.add(i);
            }
        }
        return ary;
    }

    //https://stackoverflow.com/questions/24811536/android-listview-get-item-view-by-position
    private View getViewByPosition(int pos) {
        final int firstListItemPosition = this.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + this.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return this.getAdapter().getView(pos, null, this);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return this.getChildAt(childIndex);
        }
    }

}
