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
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import java.util.Set;
import java.util.regex.Pattern;
import tool.ConnectThread;
import com.example.notification.MainService;
import com.example.notification.R;



public class MyFragment extends Fragment
{
    //Bluetooth
    private CompanionDeviceManager deviceManager;
    private AssociationRequest pairingRequest;
    private BluetoothDeviceFilter deviceFilter;
    private  BluetoothSocket mmSocket;
    //ScreenMessage
    private ProgressDialog dialog;
    private TextView connectStatus;
    Set<BluetoothDevice> pairedDevices =  BluetoothAdapter.getDefaultAdapter().getBondedDevices();


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
            //view
            View view = inflater.inflate(R.layout.frament_home, container, false);
            //button
            Button Bluetooth = (Button) view.findViewById(R.id.button5);
            Button send=(Button) view.findViewById(R.id.button);
            //textView
            connectStatus=(TextView) view.findViewById(R.id.connectStatus);
            //buttonListener
            send.setOnClickListener(v -> send());
            Bluetooth.setOnClickListener(v -> connect());

            BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // 處理 Service 傳來的訊息。
                    Bundle message = intent.getExtras();
                    int status = message.getInt("connectStatus");
                    if(status== MainService.connect)
                        connectStatus.setText(" 已連線");
                    else if(status==MainService.disconnect)
                        connectStatus.setText("連線中斷");
                }
            };

            IntentFilter filter = new IntentFilter("MainService");
            // 將 BroadcastReceiver 在 Activity 掛起來。
            requireActivity().registerReceiver(receiver, filter);
            return view;
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
        Intent intent=new Intent(getContext(),MainService.class);
        getContext().startService(intent);
    }

    private void send()
    {
        Intent intent=new Intent(getContext(),MainService.class);
        getContext().stopService(intent);
    }
}



