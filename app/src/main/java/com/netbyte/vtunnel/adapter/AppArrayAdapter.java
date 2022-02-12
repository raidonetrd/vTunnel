package com.netbyte.vtunnel.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.App;

import java.util.List;

public class AppArrayAdapter extends ArrayAdapter<App> {
    private final List<App> apps;
    private final Activity context;

    public AppArrayAdapter(Activity context, List<App> apps) {
        super(context, R.layout.bypass_item, apps);
        this.context = context;
        this.apps = apps;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null) {
            row = inflater.inflate(R.layout.bypass_item, null, true);
        }
        ImageView icon = row.findViewById(R.id.appIcon);
        TextView name = row.findViewById(R.id.appName);
        CheckBox checkBox = row.findViewById(R.id.checkApp);
        icon.setImageDrawable(apps.get(position).getIcon());
        name.setText(apps.get(position).getName());
        checkBox.setChecked(apps.get(position).isBypass());
        checkBox.setTag(position);
        checkBox.setOnClickListener(v -> {
            Integer pos = (Integer) checkBox.getTag();
            if (apps.get(pos).isBypass()) {
                apps.get(pos).setBypass(false);
            } else {
                apps.get(pos).setBypass(true);
            }
        });
        return row;
    }
}

