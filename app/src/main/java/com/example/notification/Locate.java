package com.example.notification;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class Locate extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView message;
    private Button start;
    private SupportMapFragment mapFragment;
    private View view;
    private getGpsService getGpsService;
    ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
	    public void onServiceConnected(ComponentName name, IBinder service)
        {
            // 建立連接
            // 獲取服務的操作對象
            getGpsService.MyBinder binder = (getGpsService.MyBinder)service;
            getGpsService=binder.getService();// 獲取到的Service即MyService
        }
        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            // 連接斷開
        }
    };

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        view =  inflater.inflate(R.layout.locate, container, false);
        Intent intent = new Intent(getActivity(),getGpsService.class);
        getActivity().bindService(intent, serviceConnection,  Context.BIND_AUTO_CREATE);
        return view;
    }
    @Override
    public void onStart( )
    {
        super.onStart();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        message=(TextView) view.findViewById(R.id.textView1);
        start=(Button) view.findViewById(R.id.getScooterGps);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        Marker temp;
        double lat=24.14458;
        double lng=120.72863;

        // Add a marker in Sydney and move the camera
        final LatLng sydney = new LatLng(lat, lng);
        temp=mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15f));

        start.setOnClickListener(v -> getGpsService.sync(mMap,temp,message));
    }

}