package com.netbyte.vtunnel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.model.Global;
import com.netbyte.vtunnel.model.Stats;
import com.netbyte.vtunnel.service.MyVPNService;
import com.netbyte.vtunnel.utils.FormatUtil;

import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    ImageButton imageButton;
    TextView statusTextView;
    TextView runningTimeTextView;
    TextView statTextView;
    Thread runningTimeThread;
    Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                showView();
            } else if (msg.what == 1 && Global.START_TIME > 0) {
                runningTimeTextView.setText(FormatUtil.formatTime((System.currentTimeMillis() - Global.START_TIME) / 1000));
                if (Stats.TOTAL_BYTES.get() > 0) {
                    statTextView.setText(FormatUtil.formatByte(Stats.TOTAL_BYTES.get()));
                }
            }
        }
    };
    ActivityResultLauncher<Intent> vpnLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    startVPNService();
                } else {
                    stopVPNService();
                }
            });

    public HomeFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runningTimeThread = new Thread(() -> {
            while (true) {
                if (Global.IS_CONNECTED) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        runningTimeThread.start();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.showView();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.showView();
    }

    private void showView() {
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        View thisView = getView();
        assert thisView != null;
        statusTextView = thisView.findViewById(R.id.textStatus);
        statusTextView.setText(Global.IS_CONNECTED ? R.string.msg_vpn_connect_yes : R.string.msg_vpn_connect_no);
        runningTimeTextView = thisView.findViewById(R.id.textRunningTime);
        runningTimeTextView.setVisibility(Global.IS_CONNECTED ? View.VISIBLE : View.GONE);
        statTextView = thisView.findViewById(R.id.textStat);
        statTextView.setVisibility(Global.IS_CONNECTED ? View.VISIBLE : View.GONE);
        imageButton = thisView.findViewById(R.id.connectBtn);
        imageButton.setImageResource(Global.IS_CONNECTED ? R.drawable.power_stop : R.drawable.power_off);
        imageButton.setOnClickListener(v -> clickHandler());
    }

    private void showResultView() {
        Activity activity = getActivity();
        assert activity != null;
        imageButton.setImageResource(Global.IS_CONNECTED ? R.drawable.power_stop : R.drawable.power_off);
        statusTextView.setText(Global.IS_CONNECTED ? R.string.msg_vpn_connect_yes : R.string.msg_vpn_connect_no);
        runningTimeTextView.setText((Global.IS_CONNECTED && Global.START_TIME > 0) ? FormatUtil.formatTime((System.currentTimeMillis() - Global.START_TIME) / 1000) : "00:00:00");
        runningTimeTextView.setVisibility(Global.IS_CONNECTED ? View.VISIBLE : View.GONE);
        statTextView.setText(Global.IS_CONNECTED ? FormatUtil.formatByte(Stats.TOTAL_BYTES.get()) : "");
        statTextView.setVisibility(Global.IS_CONNECTED ? View.VISIBLE : View.GONE);
        Toast.makeText(activity, Global.IS_CONNECTED ? R.string.msg_vpn_start : R.string.msg_vpn_stop, Toast.LENGTH_LONG).show();
    }

    private void clickHandler() {
        if (Global.IS_CONNECTED) {
            Global.IS_CONNECTED = false;
        } else {
            Global.IS_CONNECTED = true;
        }
        Activity activity = this.getActivity();
        assert activity != null;
        SharedPreferences preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        String server = preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS);
        if (TextUtils.isEmpty(server)) {
            Toast.makeText(activity, R.string.msg_error_server_none, Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = VpnService.prepare(this.getContext());
        if (intent != null) {
            Log.w(AppConst.DEFAULT_TAG, "VPN requires the authorization from the user, requesting...");
            vpnLauncher.launch(intent);
        } else {
            Log.d(AppConst.DEFAULT_TAG, "VPN was already authorized");
            startVPNService();
        }
    }

    private void startVPNService() {
        Activity activity = getActivity();
        assert activity != null;
        Intent vpnIntent = new Intent(activity, MyVPNService.class);
        vpnIntent.setAction(Global.IS_CONNECTED ? AppConst.BTN_ACTION_CONNECT : AppConst.BTN_ACTION_DISCONNECT);
        activity.startService(vpnIntent);
        showResultView();
    }

    private void stopVPNService() {
        Global.IS_CONNECTED = false;
        showResultView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            runningTimeThread.interrupt();
            runningTimeThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
