package com.example.notification;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Set;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;

    //Fragment Object
    private MyFragment main, locate;
    private FragmentManager fManager;
    //Bluetooth
    private Button Bluetooth;
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothDeviceFilter deviceFilter;

    private ProgressDialog dialog;

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
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (checkedId) {
            case R.id.rb_main:
                if (main == null) {
                    main = new MyFragment(1);
                    fTransaction.add(R.id.ly_content, main);
                } else {
                    fTransaction.show(main);
                }
                break;
            case R.id.rb_locate:
                if (locate == null) {
                    locate = new MyFragment(2);
                    fTransaction.add(R.id.ly_content, locate);
                } else {
                    fTransaction.show(locate);
                }
                break;
        }
        fTransaction.commit();
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (main != null) fragmentTransaction.hide(main);
        if (locate != null) fragmentTransaction.hide(locate);
    }

    private boolean isPurview(Context context) { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    private void init() {
        fManager = getSupportFragmentManager();
        rg_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        //獲取第一個選單按鈕，設置為選取狀態
        rb_main = (RadioButton) findViewById(R.id.rb_main);
        rb_main.setChecked(true);
        Bluetooth = (Button) findViewById(R.id.button5);
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
}