package com.netbyte.vtunnel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.App;
import com.netbyte.vtunnel.model.AppConst;

import java.util.ArrayList;
import java.util.List;

public class BypassTab extends Fragment {
    ListView listView;
    Button btnSave;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;
    OnFragmentInteractionListener mListener;

    public BypassTab() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_bypass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();
        listView = (ListView) getView().findViewById(R.id.listView);
        btnSave = getView().findViewById(R.id.saveBypassBtn);
        this.listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        this.listView.setOnItemClickListener((parent, view1, position, id) -> {
            CheckedTextView v = (CheckedTextView) view1;
            App app = (App) listView.getItemAtPosition(position);
            app.setBypass(v.isChecked());
        });
        btnSave.setOnClickListener(v -> {
            SparseBooleanArray sparseBooleanArray = listView.getCheckedItemPositions();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < sparseBooleanArray.size(); i++) {
                if (sparseBooleanArray.valueAt(i)) {
                    App app = (App) listView.getItemAtPosition(i);
                    if (app != null && app.isBypass()) {
                        if (sb.length() == 0) {
                            sb.append(app.getPackageName());
                        } else {
                            sb.append(",").append(app.getPackageName());
                        }
                    }
                }
            }
            preEditor.putString("bypass_apps", sb.toString());
            preEditor.commit();
            Toast.makeText(activity, "SAVED !", Toast.LENGTH_LONG).show();
        });

        this.initListViewData(this.preferences);
    }

    private void initListViewData(SharedPreferences preferences) {
        List<App> appList = new ArrayList<>();
        String bypassApps = preferences.getString("bypass_apps", "");
        PackageManager packageManager = this.getActivity().getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo info : packageInfoList) {
            if (!isUserApp(info) || AppConst.APP_PACKAGE_NAME.equals(info.packageName)) {
                continue;
            }
            ApplicationInfo applicationInfo = null;
            try {
                applicationInfo = packageManager.getApplicationInfo(info.packageName, 0);
            } catch (final PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "unknown");
            App app = new App(name, info.packageName, bypassApps.contains(info.packageName));
            appList.add(app);
        }
        ArrayAdapter<App> arrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_list_item_checked, appList);
        this.listView.setAdapter(arrayAdapter);
        for (int i = 0; i < appList.size(); i++) {
            this.listView.setItemChecked(i, appList.get(i).isBypass());
        }
    }

    public boolean isSystemApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public boolean isSystemUpdateApp(PackageInfo pInfo) {
        return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
    }

    public boolean isUserApp(PackageInfo pInfo) {
        return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
