package com.netbyte.vtunnel.adapter;

import android.app.Activity;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;


public class StrArrayAdapter extends ArrayAdapter<String> {
    private final List<String> items;
    private final Activity context;

    public StrArrayAdapter(Activity context, int resource, List<String> items) {
        super(context, resource, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}