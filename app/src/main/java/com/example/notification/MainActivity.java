package com.example.notification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import android.bluetooth.BluetoothAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import Fragment.MainFragment;
import Fragment.MyFragment;
import Fragment.SettingFragment;
import internet.Internet;
import static android.content.ContentValues.TAG;


public class MainActivity extends AppCompatActivity  implements NavigationView.OnNavigationItemSelectedListener
{

    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;
    //Fragment Object
    private MyFragment main, locate;
    private FragmentManager fManager;

    Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Start service
        Log.e("MainActivity", "000");

        init();
        if (pairedDevices.size() > 0)
        {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println("Name: " + deviceName +  " Address: " + deviceHardwareAddress);
            }
        }
        //initListener();
        //bluetoothPair();
        if (!isPurview(this))// 檢查權限是否開啟，未開啟則開啟對話框
        {
            new AlertDialog.Builder(MainActivity.this)// 跳轉自開啟權限畫面，權限開啟後通知欄擷取服務將自動啟動。
                    .setTitle("啟用通知欄權限")
                    .setMessage("請啟用通知欄擷取權限")
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setCancelable(false)
                    .setPositiveButton("開啟", (d,w)-> super.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))// 對話框按鈕事件
                    .show();
        }



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


    private boolean isPurview(Context context) // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
	{ 

        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName()))
        {
            return true;
        }
        return false;
    }

    private void init() {
        //Bluetooth = (Button) findViewById(R.id.button5);

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