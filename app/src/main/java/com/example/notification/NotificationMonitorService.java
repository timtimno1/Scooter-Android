package com.example.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class NotificationMonitorService extends NotificationListenerService {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) { //通知出現將觸發
        Bundle extras = sbn.getNotification().extras;
        String packageName = sbn.getPackageName(); // 取得應用程式包名

        if (packageName.equals("com.google.android.apps.maps")) {//判斷只接收google map通知{
            String title = extras.getString(Notification.EXTRA_TITLE); // 取得通知欄標題
            String text = extras.getString(Notification.EXTRA_TEXT); // 取得通知欄文字
            Icon largeIcon = sbn.getNotification().getLargeIcon();
           /* try { // 取得通知欄的小圖示
                PackageManager manager = getPackageManager();
                Resources resources = manager.getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }*/
            MainActivity.show(packageName, title, text, largeIcon,this);//傳送資料
        }
    }
}
