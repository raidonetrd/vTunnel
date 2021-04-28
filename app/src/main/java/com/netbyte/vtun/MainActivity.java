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

import java.util.concurrent.atomic.AtomicLong;

public class MainActivity extends AppCompatActivity {
    public static AtomicLong downByte = new AtomicLong(0);
    public static AtomicLong upByte = new AtomicLong(0);
    private Button btConn, btDisConn;
    private ToggleButton protocolButton;
    private EditText editServer, editServerPort, editLocal, editDNS, tokenEdit;
    private TextView viewInfo;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preEditor;
    private volatile boolean statThreadRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btConn = findViewById(R.id.connButton);
        btDisConn = findViewById(R.id.disConnButton);
        protocolButton = findViewById(R.id.protocolButton);
        editServer = findViewById(R.id.serverAddrEdit);
        editServerPort = findViewById(R.id.serverPortEdit);
        editLocal = findViewById(R.id.localAddrEdit);
        viewInfo = findViewById(R.id.infoTextView);
        tokenEdit = findViewById(R.id.tokenText);
        editDNS = findViewById(R.id.dnsEdit);

        preferences = getPreferences(Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("serverIP", "192.168.0.1"));
        editServerPort.setText(preferences.getString("serverPort", "443"));
        editLocal.setText(preferences.getString("localIP", "172.16.0.20/24"));
        editDNS.setText(preferences.getString("dns", "208.67.220.220"));
        tokenEdit.setText(preferences.getString("token", "6w9z$C&F)J@NcRfWjXn3r4u7x!A%D*G-"));
        String preProtocol = preferences.getString("protocol", "ws");
        protocolButton.setChecked(preProtocol.equals("ws"));

        btDisConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statThreadRunning = false;
                upByte.set(0);
                downByte.set(0);
                viewInfo.setText("Disconnected");
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, VTunService.class);
                intent.setAction("disconnect");
                startService(intent);
            }
        });

        btConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewInfo.setText("Connected");
                Intent intent = VpnService.prepare(MainActivity.this);
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, RESULT_OK, null);
                    statThreadRunning = true;
                    Thread t = new Thread(new StatThread(viewInfo));
                    t.start();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        if (result != RESULT_OK) {
            return;
        }
        Intent intent = new Intent(this, VTunService.class);
        String serverIP = editServer.getText().toString();
        String serverPort = editServerPort.getText().toString();
        String localIp = editLocal.getText().toString();
        String dns = editDNS.getText().toString();
        String token = tokenEdit.getText().toString();
        String protocol = this.protocolButton.isChecked() ? "ws" : "udp";
        intent.setAction("connect");
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


    class StatThread implements Runnable {
        TextView textView;

        StatThread(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void run() {
            while (statThreadRunning) {
                try {
                    textView.setText(String.format("Network: up %dKB down %dKB", MainActivity.upByte.get() / 1024, MainActivity.downByte.get() / 1024));
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
