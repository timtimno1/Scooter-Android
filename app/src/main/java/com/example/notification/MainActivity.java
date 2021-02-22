package com.example.notification;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.wifi.aware.DiscoverySession;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
{
    private static TextView textView;
    private static ImageView smallIcon, largeIcon;
    private static Icon bitmapIcon;//儲存通知訊息大圖示
    private static String string;//儲存包名、標題、內容文字
    private static final byte left=33;
    private static final byte regiht=32;
    private static final byte[] resolution={48,72,90,95,113,120,126};
    private static final byte[] value={0,0,0,0,113,0,0};
    private static final byte[][] feature={{30,30},{28,29},{28,31},{32,33},{6,9},{33,30},{28,39}};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        largeIcon = (ImageView) findViewById(R.id.largeIcon);


        if (!isPurview(this))
        { // 檢查權限是否開啟，未開啟則開啟對話框
            new AlertDialog.Builder(this)
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

    private boolean isPurview(Context context)
    { // 檢查權限是否開啟 true = 開啟 ，false = 未開啟
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName()))
        {
            return true;
        }
        return false;
    }

    //接收資料
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void show(String packageName, String title, String text, Icon large, Context ct)
    {

        int Cont=0;
        String direction="無";
        bitmapIcon = large;
        Bitmap bi;
        int index;
        byte[] biArray;
        byte bitmapW=0;
        try {
            bi=drawableToBitmap (bitmapIcon.loadDrawable(ct));
            bitmapW=(byte)bi.getWidth();
            index=find(bitmapW);
            biArray=getBytesByBitmap(bi);
            for(byte i:biArray)
                if(i==value[index])Cont++;
            if(Cont==feature[index][0])
                direction="右";
            else if (Cont==feature[index][1])
                direction="左";
        }
        catch (Exception e)
        {
            System.out.println("wait");
        }


        string = "\n\n" +
                "距離:" + title.replaceAll("-.*", "") + "\n\n" +
                "下個轉彎方向:" + direction + "轉" + "Cont:" + Cont + " Resolution:" + bitmapW + "\n\n" +
                "到達時間:" + text + "\n\n";


        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                handler.sendMessage(msg);
            }
        }).start();
    }

    private static Handler handler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                //將資料顯示，更新至畫面
                textView.setText(string);
                largeIcon.setImageIcon(bitmapIcon);
            } catch (Exception e) {
            }
        }
    };

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0)
        {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        }
        else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static byte[] getBytesByBitmap(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bitmap.getAllocationByteCount());
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }
    public static byte find(byte i)
    {
        byte j;
        for(j=0;j<resolution.length;j++)
            if(i==resolution[j])
                break;
        return j;
    }

}