package com.netbyte.vtunnel.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.utils.NetUtil;

public class ConfigTab extends Fragment {
    ToggleButton toggleButton;
    Button btnSave;
    EditText editServer, editDNS, editKey;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;
    OnFragmentInteractionListener mListener;

    public ConfigTab() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        btnSave = getView().findViewById(R.id.saveConfigBtn);
        editServer = getView().findViewById(R.id.editServer);
        editKey = getView().findViewById(R.id.editKey);
        editDNS = getView().findViewById(R.id.editDNS);
        toggleButton = getView().findViewById(R.id.toggleButton);
        assert activity != null;
        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        editKey.setText(preferences.getString("key", AppConst.DEFAULT_KEY));
        toggleButton.setChecked(preferences.getBoolean("obfuscate", false));
        btnSave.setOnClickListener(v -> {
            String server = editServer.getText().toString().trim();
            if (!NetUtil.checkServer(server)) {
                Toast.makeText(activity, "Server can't connect", Toast.LENGTH_LONG).show();
                return;
            }
            String dns = editDNS.getText().toString().trim();
            if (!NetUtil.checkDNS(dns)) {
                Toast.makeText(activity, "DNS is not reachable", Toast.LENGTH_LONG).show();
                return;
            }
            String key = editKey.getText().toString().trim();
            boolean obfuscate = toggleButton.isChecked();
            preEditor.putString("server", server);
            preEditor.putString("dns", dns);
            preEditor.putString("key", key);
            preEditor.putBoolean("obfuscate", obfuscate);
            preEditor.apply();
            Toast.makeText(activity, "Saved", Toast.LENGTH_LONG).show();
        });
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
