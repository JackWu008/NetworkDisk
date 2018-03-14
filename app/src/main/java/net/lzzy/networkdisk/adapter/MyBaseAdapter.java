package net.lzzy.networkdisk.adapter;


import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public abstract class MyBaseAdapter<T> extends BaseAdapter {
    private List<T> ts;
    private Context context;
    private int layout;
    private SparseBooleanArray sparse = new SparseBooleanArray();
    private boolean isMyMultipleChoice;

    MyBaseAdapter(Context context, int layout, List<T> ts, boolean isMyMultipleChoice) {
        this.ts = ts;
        this.context = context;
        this.layout = layout;
        this.isMyMultipleChoice = isMyMultipleChoice;
    }

    @Override
    public int getCount() {
        return ts.size();
    }

    @Override
    public T getItem(int position) {
        return ts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    List<T> getItems() {
        return ts;
    }

    public void setIsSelected(int position, boolean isSelected) {
        if (isMyMultipleChoice)
            this.sparse.put(position, isSelected);
    }

    SparseBooleanArray getSparse() {
        if (isMyMultipleChoice)
            return sparse;
        else
            return null;
    }

    public int getSelectedNum() {
        int i = 0;
        for (int j = 0; j < sparse.size(); j++) {
            int pos = sparse.keyAt(j);
            if (sparse.get(pos))
                i += 1;
        }
        return i;
    }

    public int[] getSelectedPosition() {
        int[] ints = new int[getSelectedNum()];
        int index = 0;
        for (int j = 0; j < sparse.size(); j++) {
            int pos = sparse.keyAt(j);
            if (isSelected(pos)) {
                ints[index] = pos;
                index++;
            }
        }
        return ints;
    }

    public void setIsSelected(SparseBooleanArray isSelected) {
        this.sparse = isSelected;
    }

    public void delete(int key) {
        this.sparse.delete(key);
    }


    public boolean isSelected(int position) {
        return sparse.get(position);
    }

    void clearSelected() {
        this.sparse.clear();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.getInstance(context, null, layout, convertView);
        populate(holder, getItem(position), convertView, position);
        return holder.getConvertView();
    }


    public abstract void populate(ViewHolder holder, T t, View convertView, int position);

}
