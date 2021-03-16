package com.example.notification;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import tool.ConnectThread;
import tool.MyBluetoothService;

import static android.content.ContentValues.TAG;


public class MyFragment extends Fragment {
    private int num;
    //Bluetooth
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothDeviceFilter deviceFilter;
    private  BluetoothSocket mmSocket;
    private ProgressDialog dialog;
    Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();

    public MyFragment(int content) {
        this.num = content;
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.num==1) {
            View view = inflater.inflate(R.layout.fragment_main, container, false);
            Button Bluetooth = (Button) view.findViewById(R.id.button5);
            Button send=(Button) view.findViewById(R.id.button);
            send.setOnClickListener(v -> send());
            Bluetooth.setOnClickListener(v -> connect());
            return view;
        }
        else{
            View view = inflater.inflate(R.layout.locate, container, false);
            return view;
        }
        /*TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
        txt_content.setText(content);*/
    }

    private void bluetoothPair()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            dialog=new ProgressDialog(getContext());
            dialog.setTitle("掃描中");
            dialog.show();
            deviceManager = getActivity().getSystemService(CompanionDeviceManager.class);

            deviceFilter = new BluetoothDeviceFilter.Builder()
                    .setNamePattern(Pattern.compile("raspberrypi"))
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
                                startIntentSenderForResult(chooserLauncher, 42, null, 0, 0, 0,null);
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
                            Toast.makeText(getContext(), "請確認裝置已開機", Toast.LENGTH_SHORT).show();
                        }
                    },
                    null);
        }
        else
        {
            Toast.makeText(getContext(), "Not supper Android version", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 42 && resultCode == Activity.RESULT_OK)
        {
            // User has chosen to pair with the Bluetooth device.
            BluetoothDevice deviceToPair = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            ConnectThread ii =new ConnectThread(deviceToPair);
            ii.start();
            /*if(deviceToPair.getBondState()==BluetoothDevice.BOND_BONDED) {
                Toast.makeText(getApplicationContext(), "裝置已經配對，將進行連線", Toast.LENGTH_SHORT).show();
                try {
                    mmSocket = deviceToPair.createInsecureRfcommSocketToServiceRecord(deviceToPair.getUuids()[0].getUuid());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                } catch (IOException connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {

                    }
                }
            }
            else
                deviceToPair.createBond();

            // ... Continue interacting with the paired device.*/
        }
        else
        {
            Toast.makeText(getContext(), "使用著取消", Toast.LENGTH_SHORT).show();
        }
    }

    private void connect()
    {
        if (pairedDevices.size() > 0)
        {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices)
            {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                if("raspberrypi".equals(deviceName))
                {
                    ConnectThread ii =new ConnectThread( BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceHardwareAddress));
                    ii.start();
                    break;
                }
            }
        }
    }

    private void send()
    {
        MyBluetoothService temp=new MyBluetoothService();
        MyBluetoothService.ConnectedThread ii =temp.new ConnectedThread(ConnectThread.mmSocket);
        byte[] i={0,0,1,2,5,4,8,4,7,5,2,4,4,7,4,4,4,5,6};
        ii.write(i);
    }
}



