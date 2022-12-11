package com.netbyte.vtunnel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.adapter.StrArrayAdapter;
import com.netbyte.vtunnel.model.Const;
import com.netbyte.vtunnel.utils.NetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsFragment extends Fragment {
    Button btnSave;
    EditText editServer, editDNS, editKey;
    AutoCompleteTextView editObfs, editProtocol;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        if (Objects.isNull(activity)) {
            return;
        }
        preferences = activity.getSharedPreferences(Const.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();
        View thisView = getView();
        assert thisView != null;
        btnSave = thisView.findViewById(R.id.saveConfigBtn);
        editServer = thisView.findViewById(R.id.editServer);
        editKey = thisView.findViewById(R.id.editKey);
        editDNS = thisView.findViewById(R.id.editDNS);
        editProtocol = thisView.findViewById(R.id.editProtocol);
        List<String> protocolItems = new ArrayList<>();
        protocolItems.add("ws");
        protocolItems.add("wss");
        StrArrayAdapter protocolAdapter = new StrArrayAdapter(this.getActivity(), R.layout.str_item, protocolItems);
        editProtocol.setAdapter(protocolAdapter);
        editProtocol.setText(preferences.getString("proto", "wss"), false);
        editObfs = thisView.findViewById(R.id.editObfs);
        List<String> obfsItems = new ArrayList<>();
        obfsItems.add("on");
        obfsItems.add("off");
        StrArrayAdapter obfsAdapter = new StrArrayAdapter(this.getActivity(), R.layout.str_item, obfsItems);
        editObfs.setAdapter(obfsAdapter);
        editObfs.setText(preferences.getString("obfuscation", "off"), false);
        editServer.setText(preferences.getString("server", Const.DEFAULT_SERVER_ADDRESS));
        editDNS.setText(preferences.getString("dns", Const.DEFAULT_DNS));
        editKey.setText(preferences.getString("key", Const.DEFAULT_KEY));
        btnSave.setOnClickListener(v -> {
            String server = editServer.getText().toString().trim();
            String key = editKey.getText().toString().trim();
            String obfs = editObfs.getText().toString().trim();
            String proto = editProtocol.getText().toString().trim();
            if (!NetUtil.checkServer(server, Const.DEFAULT_PATH, key, Objects.equals(proto, "wss"))) {
                Toast.makeText(activity, R.string.msg_error_server, Toast.LENGTH_LONG).show();
                return;
            }
            String dns = editDNS.getText().toString().trim();
            if (!NetUtil.checkDNS(dns)) {
                Toast.makeText(activity, R.string.msg_error_dns, Toast.LENGTH_LONG).show();
                return;
            }
            preEditor.putString("server", server);
            preEditor.putString("dns", dns);
            preEditor.putString("key", key);
            preEditor.putString("obfuscation", obfs);
            preEditor.putString("proto", proto);
            preEditor.apply();
            Toast.makeText(activity, R.string.msg_success_save, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
