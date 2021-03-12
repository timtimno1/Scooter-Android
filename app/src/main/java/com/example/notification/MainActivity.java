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

import static android.content.ContentValues.TAG;

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
    private  BluetoothSocket mmSocket;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        //initListener();
        //bluetoothPair();
        if (!isPurview(this))// 檢查權限是否開啟，未開啟則開啟對話框
        {
            new AlertDialog.Builder(MainActivity.this)// 跳轉自開啟權限畫面，權限開啟後通知欄擷取服務將自動啟動。
                    .setTitle("啟用通知欄擷取權限")
                    .setMessage("請啟用通知欄擷取權限")
                    .setIcon(R.mipmap.ic_launcher_round)
                    .setCancelable(false)
                    .setPositiveButton("開啟", (d,w)-> super.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")))// 對話框按鈕事件
                    .show();
        }
        startService(new Intent(this, MainService.class));
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
        Bluetooth = (Button) findViewById(R.id.button5);
    }

    private void initListener()
    {
        Bluetooth.setOnClickListener(v -> bluetoothPair());
    }

    private void bluetoothPair()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            dialog=new ProgressDialog(this);
            dialog.setTitle("掃描中");
            dialog.show();
            deviceManager = getSystemService(CompanionDeviceManager.class);

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
            Toast.makeText(getApplicationContext(), "使用著取消", Toast.LENGTH_SHORT).show();
        }
    }
}
class ConnectThread extends Thread
{
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    MyBluetoothService temp=new MyBluetoothService();
    MyBluetoothService.ConnectedThread ii ;

    public ConnectThread(BluetoothDevice device)
    {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try
        {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"));
        }
        catch (IOException e)
        {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        ii=temp.new ConnectedThread(mmSocket);
    }

    public void run()
    {
        // Cancel discovery because it otherwise slows down the connection.
        bluetoothAdapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        MyBluetoothService temp=new MyBluetoothService();
        MyBluetoothService.ConnectedThread ii =temp.new ConnectedThread(mmSocket);
        ii.start();
        byte[] i={0,0,1,2,5,4,8,4,7,5,2,4,4,7,4,4,4,5,6};
        ii.write(i);
        //manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel()
    {
        try
        {
            ii.cancel();
            mmSocket.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
class MyBluetoothService
{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private Handler handler; // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants
    {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    public class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true)
            {
                try
                {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = handler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                }
                catch (IOException e)
                {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes)
        {
            try
            {
                mmOutStream.write(bytes);

                /*--Share the sent message with the UI activity.--*/
                /*Message writtenMsg = handler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, bytes);
                writtenMsg.sendToTarget();*/
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                handler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }
}