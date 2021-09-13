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

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.config.AppConst;

public class SettingsTab extends Fragment {
    ToggleButton obfuscateBtn;
    Button btnSave;
    EditText editServer, editDNS, editKey, editBypass;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;
    OnFragmentInteractionListener mListener;

    public SettingsTab() {

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

        btnSave = getView().findViewById(R.id.saveButton);
        editServer = getView().findViewById(R.id.serverAddressEdit);
        editKey = getView().findViewById(R.id.keyEdit);
        editBypass = getView().findViewById(R.id.bypassUrlEdit);
        editDNS = getView().findViewById(R.id.dnsEdit);
        obfuscateBtn = getView().findViewById(R.id.obfuscateBtn);

        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS));
        editBypass.setText(preferences.getString("bypassUrl", ""));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        editKey.setText(preferences.getString("key", AppConst.DEFAULT_KEY));
        obfuscateBtn.setChecked(preferences.getBoolean("obfuscate", true));

        btnSave.setOnClickListener(v -> {
            String server = editServer.getText().toString().trim();
            String dns = editDNS.getText().toString().trim();
            String key = editKey.getText().toString().trim();
            String bypassUrl = editBypass.getText().toString().trim();
            boolean obfuscate = obfuscateBtn.isChecked();
            preEditor.putString("server", server);
            preEditor.putString("dns", dns);
            preEditor.putString("key", key);
            preEditor.putString("bypassUrl", bypassUrl);
            preEditor.putBoolean("obfuscate", obfuscate);
            preEditor.apply();
            Toast.makeText(activity, "Saved！", Toast.LENGTH_LONG).show();
        });
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
