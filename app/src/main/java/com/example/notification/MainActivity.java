package com.example.notification;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import internet.Internet;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener
{

    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;
    //Fragment Object
    private MyFragment main, locate;
    private FragmentManager fManager;

    Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();



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

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (checkedId)
        {
            case R.id.rb_main:
                if (main == null)
                {
                    main = new MyFragment(1);
                    fTransaction.add(R.id.ly_content, main);
                } else {
                    fTransaction.show(main);
                }
                break;
            case R.id.rb_locate:
                if (locate == null)
                {
                    locate = new MyFragment(2);
                    fTransaction.add(R.id.ly_content, locate);
                }
                else
                {
                    fTransaction.show(locate);
                }
                break;
        }
        fTransaction.commit();
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction)
    {
        if (main != null) fragmentTransaction.hide(main);
        if (locate != null) fragmentTransaction.hide(locate);
    }

    private boolean isPurview(Context context)  // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
    {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName()))
        {
            return true;
        }
        return false;
    }

    private void init()
    {
        fManager = getSupportFragmentManager();
        rg_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        //獲取第一個選單按鈕，設置為選取狀態
        rb_main = (RadioButton) findViewById(R.id.rb_main);
        rb_main.setChecked(true);
    }
}