package com.example.notification;

import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import internet.HttpConnect;
import internet.Internet;

public class getGpsService extends Service
{
    private TimerTask task;
    private Timer timer=new Timer();
    private  GoogleMap mMap;
    private Marker marker;
    private TextView message;
    private HttpConnect ruc;
    private String Tag="getGpsService";
        //服務創建
        @Override
        public void onCreate()
        {
            super.onCreate();
            ruc=new HttpConnect();
            task=new TimerTask()
            {
                @Override
                public void run()
                {
                    RegisterUser ru = new RegisterUser();/**傳送資料**/
                    ru.execute("1");
                }
            };

        }

        // 服務啟動
        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
        {
            return super.onStartCommand(intent, flags, startId);
        }

        //服務銷毀
        @Override
        public void onDestroy()
        {
            stopSelf(); //自殺服務
            timer.cancel();
            Log.d(Tag,"onDestroy");
            super.onDestroy();
        }

        //綁定服務
        @Nullable
        @Override
        public IBinder onBind(Intent intent)
        {
            return new MyBinder();
        }

        public void sync(GoogleMap googleMap,Marker marker,TextView textView)
        {
            mMap=googleMap;
            message=textView;
            this.marker=marker;
            timer.schedule(task,2000,500);
        }

        // IBinder是远程对象的基本接口，是为高性能而设计的轻量级远程调用机制的核心部分。但它不仅用于远程
        // 调用，也用于进程内调用。这个接口定义了与远程对象交互的协议。
        // 不要直接实现这个接口，而应该从Binder派生。
        // Binder类已实现了IBinder接口
        class MyBinder extends Binder
        {
            /** * 获取Service的方法 * @return 返回PlayerService */
            public getGpsService getService()
            {
                return getGpsService.this;
            }
        }

    private class RegisterUser extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
    {
        super.onPreExecute();/**當按下創見鈕，出現提式窗**/
    }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            double lat=0;
            double lng=0;
            String speed="0";
            Pattern patternDis=Pattern.compile("(\\d+\\.\\d+)");
            Matcher matcherDis=patternDis.matcher(s);

            if(matcherDis.find()) {
                lat = Double.parseDouble(matcherDis.group());
                matcherDis.find();
                lng = Double.parseDouble(matcherDis.group());
                matcherDis.find();
                speed = matcherDis.group();
            }
            message.setText(speed);
            animateMarker(marker,new LatLng(lat,lng),false);
        }

        @Override
        protected String doInBackground(String... params)/**將資料放入hashmap，測試call by value or call br address**/
        {
            HashMap<String, String> data = new HashMap<String,String>();
            data.put("id",params[0]);

            String result = ruc.sendPostRequest(Internet.REGISTER_URL+"gpsPull.php",data);
            return  result;
        }
    }
    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker)
    {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final long duration = 500 ;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed/duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f));
                if (t < 1.0)
                {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
                else
                {
                    if (hideMarker)
                    {
                        marker.setVisible(false);
                    }
                    else
                    {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }
}
