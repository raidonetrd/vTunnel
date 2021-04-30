package com.netbyte.vtun;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.netbyte.vtun.config.AppConst;
import com.netbyte.vtun.service.VTunnelService;

public class MainActivity extends AppCompatActivity {
    private Button btnConn, btnDisConn;
    private ToggleButton protocolBtn;
    private EditText editServer, editServerPort, editLocal, editDNS, tokenEdit;
    private TextView viewInfo;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConn = findViewById(R.id.connButton);
        btnDisConn = findViewById(R.id.disConnButton);
        protocolBtn = findViewById(R.id.protocolButton);
        editServer = findViewById(R.id.serverAddrEdit);
        editServerPort = findViewById(R.id.serverPortEdit);
        editLocal = findViewById(R.id.localAddrEdit);
        viewInfo = findViewById(R.id.infoTextView);
        tokenEdit = findViewById(R.id.tokenText);
        editDNS = findViewById(R.id.dnsEdit);

        preferences = getPreferences(Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("serverIP", AppConst.DEFAULT_SERVER_IP));
        editServerPort.setText(preferences.getString("serverPort", AppConst.DEFAULT_SERVER_PORT));
        editLocal.setText(preferences.getString("localIP", AppConst.DEFAULT_LOCAL_IP));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        tokenEdit.setText(preferences.getString("token", AppConst.DEFAULT_TOKEN));
        String preProtocol = preferences.getString("protocol", AppConst.DEFAULT_PROTOCOL);
        protocolBtn.setChecked(preProtocol.equals(AppConst.DEFAULT_PROTOCOL));

        btnDisConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewInfo.setText("Disconnected");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, VTunnelService.class);
                intent.setAction(AppConst.BTN_ACTION_DISCONNECT);
                startService(intent);
            }
        });

        btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewInfo.setText("Connected");
                Intent intent = VpnService.prepare(MainActivity.this);
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result != RESULT_OK) {
            return;
        }
        Intent intent = new Intent(this, VTunnelService.class);
        String serverIP = editServer.getText().toString();
        String serverPort = editServerPort.getText().toString();
        String localIp = editLocal.getText().toString();
        String dns = editDNS.getText().toString();
        String token = tokenEdit.getText().toString();
        String protocol = this.protocolBtn.isChecked() ? "ws" : "udp";
        intent.setAction(AppConst.BTN_ACTION_CONNECT);
        intent.putExtra("serverIP", serverIP);
        intent.putExtra("serverPort", Integer.parseInt(serverPort));
        intent.putExtra("localIP", localIp);
        intent.putExtra("dns", dns);
        intent.putExtra("token", token);
        intent.putExtra("protocol", protocol);

        startService(intent);

        preEditor.putString("serverIP", serverIP);
        preEditor.putString("serverPort", serverPort);
        preEditor.putString("localIP", localIp);
        preEditor.putString("dns", dns);
        preEditor.putString("token", token);
        preEditor.putString("protocol", protocol);
        preEditor.commit();
    }

}
