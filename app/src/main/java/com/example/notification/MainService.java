package com.example.notification;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import tool.ConnectThread;
import tool.MyBluetoothService;

public class MainService extends Service
{
    public static final int connect=1;
    public static final int disconnect=0;
    private MediaPlayer mp;
    private static ConnectThread ii;
    private BluetoothSocket mmSocket=null;
    private int count=0;
    private Bundle message=new Bundle();
    private Intent intent=new Intent("MainService");
    private Timer timer=new Timer();
    private TimerTask task;


    public void onCreate()
    {
        super.onCreate();

        Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

        if (pairedDevices.size() > 0)
        {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if("raspberrypi".equals(deviceName))
                {
                    ii =new ConnectThread( BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceHardwareAddress));
                    mmSocket=ii.getMmSocket();
                    ii.start();
                    while (true)
                    {
                        if (mmSocket.isConnected())
                        {
                            NotificationMonitorService.sentStatus = true;
                            message.putInt("connectStatus",connect);
                            break;
                        }
                        else if(count>10) {
                            message.putInt("connectStatus", disconnect);
                            break;
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        count++;
                    }
                }
                else
                {
                    //Toast.makeText(getApplicationContext(), "請先執行綁定", Toast.LENGTH_SHORT).show();
                }
            }
        }


        intent.putExtras(message);
        sendBroadcast(intent);
        MyBluetoothService myBluetoothService=new MyBluetoothService(ii);
        myBluetoothService.enableReadData();

        task=new TimerTask() {
            @Override
            public void run() {
                Boolean temp=mmSocket.isConnected();
                Log.d("run",temp.toString());
                if(!myBluetoothService.isConnected())
                {
                    NotificationMonitorService.sentStatus=false;
                    message.putInt("connectStatus",disconnect);
                    intent.putExtras(message);
                    sendBroadcast(intent);
                    ii.cancel();
                    stopSelf();
                    timer.cancel();
                    task.cancel();
                }
                message.putInt("connectStatus",connect);
                intent.putExtras(message);
                sendBroadcast(intent);
                Log.e("Service", "Tim" + count++);
                isNotificationMonitorService();
            }
        };
        timer.schedule(task, 1000,1000);

        mp=MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
        mp.setLooping(true);
        mp.start();
        Log.e("Service", "onCreate");
        //stopSelf();
    }


    private void isNotificationMonitorService(){
        // 檢查通知欄擷取是否失效，
        // 如果失效 將呼叫 restartNotificationMonitorService() 重新啟動 通知欄擷取
        // 如果未失效 將不動作
        ComponentName componentName = new ComponentName(this,NotificationMonitorService.class);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean isRunning = false;
        List<ActivityManager.RunningServiceInfo> runningServiceInfo = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (runningServiceInfo == null )
        {
            return;
        }
        for (ActivityManager.RunningServiceInfo service : runningServiceInfo) {
            if(service.service.equals(componentName)){
                    isRunning = true;
            }
        }
        if (isRunning) {
            return;
        }
        restartNotificationMonitorService();
    }
    private void restartNotificationMonitorService() { // 重新啟動 通知欄擷取
        ComponentName componentName = new ComponentName(this, NotificationMonitorService.class);
        PackageManager packageManager = getPackageManager();
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service", "onStartCommand");
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Service", "onBind");return null;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        NotificationMonitorService.sentStatus=false;
        timer.cancel();
        task.cancel();
        ii.cancel();
        mp.stop();
        message.putInt("connectStatus",disconnect);
        intent.putExtras(message);
        sendBroadcast(intent);
        Log.e("Service", "STOP");
    }
    public static ConnectThread getConnectThread()
    {
        return ii;
    }
}
