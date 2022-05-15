package com.netbyte.vtunnel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.adapter.AppArrayAdapter;
import com.netbyte.vtunnel.model.App;
import com.netbyte.vtunnel.model.AppConst;

import java.util.ArrayList;
import java.util.List;

public class AppsFragment extends Fragment {
    ListView listView;
    Button btnSave;
    ProgressBar progressBar;

    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;
    ArrayAdapter<App> arrayAdapter;

    private final int NOTIFY_ARRAY_ADAPTER_CAN_UPDATE = 1;
    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == NOTIFY_ARRAY_ADAPTER_CAN_UPDATE) {
                arrayAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    public AppsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_apps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();
        View thisView = getView();
        assert thisView != null;
        listView = thisView.findViewById(R.id.listView);
        btnSave = thisView.findViewById(R.id.saveBypassBtn);
        progressBar = thisView.findViewById(R.id.loadingProgressBar);
        btnSave.setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                App app = (App) listView.getAdapter().getItem(i);
                if (app != null && app.isBypass()) {
                    if (sb.length() == 0) {
                        sb.append(app.getPackageName());
                    } else {
                        sb.append(",").append(app.getPackageName());
                    }
                }
            }
            preEditor.putString("bypass_apps", sb.toString());
            preEditor.commit();
            Toast.makeText(activity, R.string.msg_success_save, Toast.LENGTH_LONG).show();
        });

        this.initListViewData(this.preferences);
    }

    private void initListViewData(SharedPreferences preferences) {
        List<App> appList = new ArrayList<>();
        arrayAdapter = new AppArrayAdapter(this.getActivity(), appList);
        this.listView.setAdapter(arrayAdapter);
        String bypassApps = preferences.getString("bypass_apps", "");
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        PackageManager packageManager = activity.getPackageManager();
        progressBar.setVisibility(View.VISIBLE);
        new Thread(() -> {
            List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
            for (PackageInfo info : packageInfoList) {
                if (!isUserApp(info) || AppConst.APP_PACKAGE_NAME.equals(info.packageName)) {
                    continue;
                }
                ApplicationInfo applicationInfo = null;
                Drawable icon = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(info.packageName, 0);
                    icon = applicationInfo.loadIcon(packageManager);
                } catch (final PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                String name = (String) ((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "unknown");
                App app = new App(icon, name, info.packageName, bypassApps.contains(info.packageName));
                appList.add(app);
            }
            mHandler.sendEmptyMessage(NOTIFY_ARRAY_ADAPTER_CAN_UPDATE);
        }).start();
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
