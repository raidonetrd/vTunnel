package com.netbyte.vtunnel.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.fragment.AppsFragment;
import com.netbyte.vtunnel.fragment.SettingsFragment;
import com.netbyte.vtunnel.fragment.HomeFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        setContentView(R.layout.activity_main);

        NavigationBarView navigationView = findViewById(R.id.navigation_menu);
        navigationView.setOnItemSelectedListener(selectedListener);

        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, "");
        fragmentTransaction.commit();
    }

    @SuppressLint("NonConstantResourceId")
    private final NavigationBarView.OnItemSelectedListener selectedListener = menuItem -> {
        switch (menuItem.getItemId()) {
            case R.id.bottomNavigationHomeMenuId:
                HomeFragment homeFragment = new HomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, homeFragment, "");
                fragmentTransaction.commit();
                return true;

            case R.id.bottomNavigationSettingsMenuId:
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction fragmentTransaction1 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction1.replace(R.id.content, settingsFragment);
                fragmentTransaction1.commit();
                return true;

            case R.id.bottomNavigationAppsMenuId:
                AppsFragment appsFragment = new AppsFragment();
                FragmentTransaction fragmentTransaction2 = getSupportFragmentManager().beginTransaction();
                fragmentTransaction2.replace(R.id.content, appsFragment, "");
                fragmentTransaction2.commit();
                return true;
        }
        return false;
    };

}
