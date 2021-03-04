package com.example.notification;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{

    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;

    //Fragment Object
    private MyFragment main,locate;
    private FragmentManager fManager;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fManager = getSupportFragmentManager();
        rg_tab_bar = (RadioGroup) findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        //獲取第一個選單按鈕，設置為選取狀態
        rb_main = (RadioButton) findViewById(R.id.rb_main);
        rb_main.setChecked(true);

        if (!isPurview(this))
        { // 檢查權限是否開啟，未開啟則開啟對話框
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
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (checkedId){
            case R.id.rb_main:
                if(main == null)
                {
                    main = new MyFragment(1);
                    fTransaction.add(R.id.ly_content,main);
                }
                else
                {
                    fTransaction.show(main);
                }
                break;
            case R.id.rb_locate:
                if(locate == null)
                {
                    locate = new MyFragment(2);
                    fTransaction.add(R.id.ly_content,locate);
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
    private void hideAllFragment(FragmentTransaction fragmentTransaction){
        if(main != null)fragmentTransaction.hide(main);
        if(locate != null)fragmentTransaction.hide(locate);
    }

    private boolean isPurview(Context context)
    { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName()))
        {
            return true;
        }
        return false;
    }
}