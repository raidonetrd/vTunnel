package com.netbyte.vtunnel.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.config.AppConst;
import com.netbyte.vtunnel.service.TunnelService;

public class MainActivity extends AppCompatActivity {
    private Button btnConn, btnDisConn;
    private EditText editServer, editDNS, editKey, editBypass;
    private TextView msgView;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConn = findViewById(R.id.connButton);
        btnDisConn = findViewById(R.id.disConnButton);
        editServer = findViewById(R.id.serverAddressEdit);

        editKey = findViewById(R.id.keyEdit);
        editBypass = findViewById(R.id.bypassUrlEdit);
        editDNS = findViewById(R.id.dnsEdit);

        msgView = findViewById(R.id.msgTextView);
        preferences = getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS));
        editBypass.setText(preferences.getString("bypassUrl", ""));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        editKey.setText(preferences.getString("key", AppConst.DEFAULT_KEY));
        btnDisConn.setEnabled(preferences.getBoolean("connected", false));
        btnDisConn.setOnClickListener(v -> {
            msgView.setText("Disconnected");
            btnDisConn.setEnabled(false);
            btnConn.setEnabled(true);
            preEditor.putBoolean("connected", false);
            preEditor.apply();

            Intent intent = new Intent();
            intent.setClass(MainActivity.this, TunnelService.class);
            intent.setAction(AppConst.BTN_ACTION_DISCONNECT);
            startService(intent);
        });
        btnConn.setEnabled(!preferences.getBoolean("connected", false));
        btnConn.setOnClickListener(v -> {
            msgView.setText("Connected");
            btnConn.setEnabled(false);
            btnDisConn.setEnabled(true);
            Intent intent = VpnService.prepare(MainActivity.this);
            if (intent != null) {
                startActivityForResult(intent, 0);
            } else {
                onActivityResult(0, RESULT_OK, null);
            }
        });

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK) {
            return;
        }
        String server = editServer.getText().toString().trim();
        String dns = editDNS.getText().toString().trim();
        String key = editKey.getText().toString().trim();
        String bypassUrl = editBypass.getText().toString().trim();
        // new intent
        Intent intent = new Intent(this, TunnelService.class);
        intent.setAction(AppConst.BTN_ACTION_CONNECT);
        intent.putExtra("server", server);
        intent.putExtra("dns", dns);
        intent.putExtra("key", key);
        intent.putExtra("bypassUrl", bypassUrl);
        // start service
        startService(intent);
        // save config
        preEditor.putString("server", server);
        preEditor.putString("dns", dns);
        preEditor.putString("key", key);
        preEditor.putString("bypassUrl", bypassUrl);
        preEditor.putBoolean("connected", true);
        preEditor.apply();
    }

}
