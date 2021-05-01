package Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notification.BluetoothConnectCallback;
import com.example.notification.BluetoothConnectThread;
import com.example.notification.MainActivity;
import com.example.notification.MainService;
import com.example.notification.R;
import com.example.notification.WebDrive;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tool.ConnectThread;
import tool.MyBluetoothService;
import tool.SharedPreferencesHelper;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingFragment} factory method to
 * create an instance of this fragment.
 */
public class SettingFragment extends Fragment
{
    //Bluetooth
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothDeviceFilter deviceFilter;
    private BluetoothAdapter blueDefaultAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mmSocket;
    //ScreenMessage
    private ProgressDialog dialog;
    private TextView connectStatus;
    private MainActivity mainActivity;
    private String TAG = "SettingFragment";
    //Broadcast
    BroadcastReceiver receiver;
    //SharedPreferences
    private SharedPreferences pref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        dialog=new ProgressDialog(getContext());
        //view
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        mainActivity = (MainActivity) getActivity();
        //button
        Button Bluetooth = (Button) view.findViewById(R.id.button5);
        Button send = (Button) view.findViewById(R.id.button);
        Button googleDriveConnect = (Button) view.findViewById(R.id.googleDriveConnect);
        Button pair = (Button) view.findViewById(R.id.Pair);
        //textView
        connectStatus = (TextView) view.findViewById(R.id.connectStatus);
        //getPref
        pref = getActivity().getSharedPreferences("Bluetooth", MODE_PRIVATE);
        //buttonListener
        send.setOnClickListener(v -> send());
        Bluetooth.setOnClickListener(v -> connect());
        googleDriveConnect.setOnClickListener(v -> googleDriveConnect());
        pair.setOnClickListener(v -> bluetoothPair());

        receiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                // 處理 Service 傳來的訊息。
                Bundle message = intent.getExtras();
                int status = message.getInt("connectStatus");

                dialog.setTitle("連線中");
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);

                if(status==MainService.connecting)
                    dialog.show();
                else
                    dialog.dismiss();

                if (status == MainService.connect)
                    connectStatus.setText(" 已連線");
                else if (status == MainService.disconnect)
                    connectStatus.setText("連線中斷");
                else if(status==BluetoothConnectCallback.nobind)
                    Toast.makeText(getContext(), "未配對，請先配對", Toast.LENGTH_SHORT).show();
                else if(status==BluetoothConnectCallback.noSearch)
                    Toast.makeText(getContext(), "請確認裝置在附近", Toast.LENGTH_SHORT).show();
            }
        };

        IntentFilter filter = new IntentFilter("MainService");
        // 將 BroadcastReceiver 在 Activity 掛起來。
        getActivity().registerReceiver(receiver, filter);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mainActivity.setActionBarTitle("Setting");
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        getActivity().unregisterReceiver(receiver);
    }

    private void bluetoothPair()
    {
        boolean bluetoothIsEnable = false;
        Set<BluetoothDevice> pairedDevices = blueDefaultAdapter.getBondedDevices();
        String ScotterMac = pref.getString("PairMAC", "noPair");
        boolean pair = false;
        if (pairedDevices.size() > 0)
        {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices)
            {
                if (ScotterMac.equals(device.getAddress()))
                {
                    pair = true;
                    break;
                }
            }
        }
        if (pair)
            Toast.makeText(getContext(), "已配對過", Toast.LENGTH_SHORT).show();
        else
        {
            if (!blueDefaultAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 40);
            }
            else
                bluetoothIsEnable = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && bluetoothIsEnable)
            {
                dialog.setTitle("掃描中");
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                deviceManager = (CompanionDeviceManager) getActivity().getSystemService(Context.COMPANION_DEVICE_SERVICE);

                Log.d("Bluetooth test", new ParcelUuid(new UUID(0x94f39d297d6d437dL, 0x973bfba39e49d4eeL)).toString());
                BluetoothAdapter ad = BluetoothAdapter.getDefaultAdapter();
                Log.d("Bluetooth test", "" + BluetoothAdapter.checkBluetoothAddress("B8:27:EB:9F:BB:E4"));
                ad.cancelDiscovery();
                //BluetoothDevice de=ad.getRemoteDevice("B8:27:EB:9F:BB:E4");
                //de.fetchUuidsWithSdp();
                //for(ParcelUuid i:de.getUuids())
                //   Log.d("Bluetooth",i.toString());

                deviceFilter = new BluetoothDeviceFilter.Builder()
                        //.addServiceUuid(new ParcelUuid(UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")), new ParcelUuid(UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF")))
                        .setNamePattern(Pattern.compile("raspberry"))
                        .build();

                pairingRequest = new AssociationRequest.Builder()
                        .addDeviceFilter(deviceFilter)
                        .setSingleDevice(false)
                        .build();

                deviceManager.associate(
                        pairingRequest,
                        new CompanionDeviceManager.Callback()
                        {
                            @Override
                            public void onDeviceFound(IntentSender chooserLauncher)
                            {
                                try
                                {
                                    startIntentSenderForResult(chooserLauncher, 42, null, 0, 0, 0, null);
                                }
                                catch (IntentSender.SendIntentException e)
                                {
                                    e.printStackTrace();
                                } finally
                                {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onFailure(CharSequence error)
                            {
                                dialog.dismiss();
                                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                            }
                        },
                        null);
            }
            else if (!bluetoothIsEnable)
                Toast.makeText(getContext(), "藍芽未開啟", Toast.LENGTH_SHORT).show();
            else
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
            deviceToPair.createBond();

            BroadcastReceiver receiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED)
                    {
                        //means device paired
                        Log.d("Bluetooth", "Bond succe");
                        BluetoothConnectThread bluetoothConnectThread = new BluetoothConnectThread(getContext(), new BluetoothConnectCallback()
                        {
                            @Override
                            public void onSuccess(ConnectThread connectThread)
                            {
                                SharedPreferences pref = getActivity().getSharedPreferences("Login", MODE_PRIVATE);
                                MyBluetoothService myBluetoothService = new MyBluetoothService(connectThread);
                                myBluetoothService.write(("UUID:" + pref.getString("uuid", "")).getBytes());
                                pref.edit().putString("PairMAC", deviceToPair.getAddress()).commit();
                                try
                                {
                                    Thread.sleep(1000);
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }
                                connectThread.cancel();
                                tost("配對完成");
                                dialog.dismiss();
                            }

                            @Override
                            public void onFailure(int code)
                            {
                                Method m = null;
                                try
                                {
                                    m = deviceToPair.getClass().getMethod("removeBond", (Class[]) null);
                                    m.invoke(deviceToPair, (Object[]) null);
                                }
                                catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
                                {
                                    e.printStackTrace();
                                }
                                dialog.dismiss();
                                tost("無法配對，請重試");
                                Log.d("Bluetooth", "無法連線");
                            }
                        });
                        bluetoothConnectThread.start();
                        getActivity().unregisterReceiver(this);
                    }
                    else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING)
                    {
                        dialog.setTitle("配對中...");
                        dialog.show();
                    }
                    else if (mDevice.getBondState() == BluetoothDevice.BOND_NONE)
                    {
                        dialog.dismiss();
                        Toast.makeText(getContext(), "無法配對，請重試", Toast.LENGTH_SHORT).show();
                        Log.d("Bluetooth", "無法連線");
                    }
                }
            };

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            // 將 BroadcastReceiver 在 Activity 掛起來。
            getActivity().registerReceiver(receiver, filter);
            //ConnectThread ii = new ConnectThread(deviceToPair);
            //ii.start();
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
                    } catch (IOException closeExcelllption) {

                    }
                }
            }
            else
                deviceToPair.createBond();

            // ... Continue interacting with the paired device.*/
        }
        else if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case 42:
                    Toast.makeText(getContext(), "藍芽開啟成功，請重新點選配對", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
                case 41:
                    Toast.makeText(getContext(), "藍芽開啟成功，請重新點連線", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    break;
            }

        }
        else
        {
            Toast.makeText(getContext(), "使用著取消", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }

    private void connect()
    {
        if (!blueDefaultAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 41);
        }
        else
        {
            Intent intent = new Intent("tt");
            intent.setPackage(getContext().getPackageName());
            getContext().startService(intent);
        }
    }

    private void send()
    {
        Intent intent = new Intent(getActivity(), MainService.class);
        getContext().stopService(intent);
    }

    private void tost(String text)
    {
        mainActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void googleDriveConnect()
    {

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", getString(R.string.client_id))
                .add("scope", getString(R.string.scope))
                .build();
        final Request request = new Request.Builder()
                .url("https://oauth2.googleapis.com/device/code")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                JSONObject jsonObject = null;
                Intent intent = new Intent();
                String device_code = null;
                String user_code = null;
                String url = null;
                int intarval = 5;

                try
                {
                    jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);
                    Log.i("okhttp", message);
                    device_code = jsonObject.getString("device_code");
                    user_code = jsonObject.getString("user_code");
                    url = jsonObject.getString("verification_url");
                    intarval = jsonObject.getInt("interval");
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                intent.putExtra("user_code", user_code)
                        .putExtra("device_code", device_code)
                        .putExtra("interval", intarval)
                        .putExtra("url", url)
                        .setClass(getContext(), WebDrive.class);
                startActivity(intent);

            }
        });
    }
}