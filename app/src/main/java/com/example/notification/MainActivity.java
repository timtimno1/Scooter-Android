package com.example.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationRequest;

import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.Set;
import java.util.regex.Pattern;

import Fragment.MainFragment;
import Fragment.SettingFragment;

public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener{

    //Bluetooth
    private Button Bluetooth;
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothDeviceFilter deviceFilter;

    private ProgressDialog dialog;

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        //initListener();
        bluetoothPair();
        if (!isPurview(this)) { // 檢查權限是否開啟，未開啟則開啟對話框
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("啟用通知欄擷取權限")
                    .setMessage("請啟用通知欄擷取權限")
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setCancelable(false)
                    .setPositiveButton("開啟", new DialogInterface.OnClickListener() { // 對話框按鈕事件
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 跳轉自開啟權限畫面，權限開啟後通知欄擷取服務將自動啟動。
                                    startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                                }
                            }
                    ).show();
        }
        startService(new Intent(this, MainService.class));

        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.nav_view);
        setSupportActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_homic);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.navi_fragment,new MainFragment());
        ft.commit();

        navigationView.setCheckedItem(R.id.nav_home);
        /*ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        actionBarDrawerToggle.syncState();
        drawer.addDrawerListener(actionBarDrawerToggle);*/

    }
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }


    private boolean isPurview(Context context) { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    private void init() {
        //Bluetooth = (Button) findViewById(R.id.button5);

    }

    private void initListener()
    {
        Bluetooth.setOnClickListener(v -> bluetoothPair());
    }

    private void bluetoothPair() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            dialog=new ProgressDialog(this);
            dialog.setTitle("掃描中");
            dialog.show();
            deviceManager = getSystemService(CompanionDeviceManager.class);

            deviceFilter = new BluetoothDeviceFilter.Builder()
                    .setNamePattern(Pattern.compile("MI Portable Bluetooth Speaker"))
                    .build();

            pairingRequest = new AssociationRequest.Builder()
                    .addDeviceFilter(deviceFilter)
                    .setSingleDevice(true)
                    .build();

            deviceManager.associate(pairingRequest,
                    new CompanionDeviceManager.Callback()
                    {
                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher)
                        {
                            try
                            {
                                startIntentSenderForResult(chooserLauncher,
                                        42, null, 0, 0, 0);
                            }
                            catch (IntentSender.SendIntentException e)
                            {
                                e.printStackTrace();
                            }
                            finally
                            {
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(CharSequence error)
                        {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "請確認裝置已開機", Toast.LENGTH_SHORT).show();
                        }
                    },
                    null);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Not supper Android version", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == Activity.RESULT_OK)
        {
            // User has chosen to pair with the Bluetooth device.
            BluetoothDevice deviceToPair = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            if(deviceToPair.getBondState()==BluetoothDevice.BOND_BONDED)
                deviceToPair.createBond();
            else
                Toast.makeText(getApplicationContext(), "裝置已經榜定", Toast.LENGTH_SHORT).show();

            // ... Continue interacting with the paired device.
        }
        else
        {
            Toast.makeText(getApplicationContext(), "請確認裝置已開機", Toast.LENGTH_SHORT).show();
        }
    }
    /*@Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }*/

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override//側邊選單欄按下動作
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch(item.getItemId()){
            case R.id.nav_home:
                ft.replace(R.id.navi_fragment,new MainFragment());
                ft.commit();
                break;
            case R.id.nav_setting:
                ft.replace(R.id.navi_fragment,new SettingFragment());
                ft.commit();
                Toast.makeText(MainActivity.this,"This is setting",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to Logout?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent3 = new Intent();
                                intent3.setClass(MainActivity.this, LoginMain.class);
                                startActivity(intent3);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override//按下返回建關閉側邊選單欄
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }
}