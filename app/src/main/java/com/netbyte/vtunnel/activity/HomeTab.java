package com.netbyte.vtunnel.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.service.TunnelService;


public class HomeTab extends Fragment {
    SharedPreferences preferences;
    SwitchMaterial switchMaterial;
    private OnFragmentInteractionListener mListener;

    public HomeTab() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        switchMaterial = getView().findViewById(R.id.connButton);
        switchMaterial.setActivated(activity.getSharedPreferences(AppConst.APP_NAME, Context.MODE_PRIVATE).getBoolean("connected", false));
        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor preEditor = activity.getSharedPreferences(AppConst.APP_NAME, Context.MODE_PRIVATE).edit();
            preEditor.putBoolean("connected", isChecked);
            preEditor.apply();
            String server = preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS);
            String dns = preferences.getString("dns", AppConst.DEFAULT_DNS);
            String key = preferences.getString("key", AppConst.DEFAULT_KEY);
            String bypassUrl = preferences.getString("bypassUrl", "");
            Intent intent = new Intent();
            intent.setClass(activity, TunnelService.class);
            intent.setAction(isChecked ? AppConst.BTN_ACTION_CONNECT : AppConst.BTN_ACTION_DISCONNECT);
            intent.putExtra("server", server);
            intent.putExtra("dns", dns);
            intent.putExtra("key", key);
            intent.putExtra("bypassUrl", bypassUrl);
            this.startActivity(intent);
            Toast.makeText(activity, isChecked ? "connected！" : "disconnected!", Toast.LENGTH_LONG).show();
        });
        {

        }
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
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
