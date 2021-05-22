package Fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.notification.BluetoothConnectCallback;
import com.example.notification.Locate;
import com.example.notification.MainActivity;
import com.example.notification.MainService;
import com.example.notification.R;
import com.google.android.gms.maps.SupportMapFragment;

public class MainFragment extends Fragment implements RadioGroup.OnCheckedChangeListener{
    private RadioGroup rg_tab_bar;
    private RadioButton rb_main;

    //Fragment Object
    private MyFragment main;
    private Locate locate;
    //private MainFragment fragment_main;
    private FragmentManager fManager;
    private View view;
    private MainActivity mainActivity;
    private boolean isPause;
    private TextView connectStatus;
    //Broadcast
    private BroadcastReceiver receiver;
    private ProgressDialog dialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_main, container, false);
        mainActivity=((MainActivity)getActivity());
        // Inflate the layout for this fragment
        fManager = getFragmentManager();
        rg_tab_bar = (RadioGroup) view.findViewById(R.id.rg_tab_bar);
        rg_tab_bar.setOnCheckedChangeListener(this);
        rb_main = (RadioButton) view.findViewById(R.id.rb_main);
        initBluetoothButton(view);
        return view;

    }
    @Override
    public void onResume()
    {
        super.onResume();
        rb_main.setChecked(true);
        mainActivity.setActionBarTitle("Home");
        Log.d("MainFragment","onResume");
    }
    @Override
    public void onPause()
    {
        super.onPause();
        Log.d("MainFragment","onPause");
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK && resultCode==41)
        {
            Toast.makeText(getContext(), "藍芽開啟成功，請重新點連線", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
        else
        {
            Toast.makeText(getContext(), "使用著取消", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        FragmentTransaction fTransaction = fManager.beginTransaction();
        hideAllFragment(fTransaction);
        switch (checkedId) {
            case R.id.rb_main:
                if(locate!=null)
                    locate.onPause();
                if (main == null)
                {
                    main = new MyFragment();
                    fTransaction.add(R.id.ly_content, main);
                }
                else
                    fTransaction.show(main);
                break;
            case R.id.rb_locate:
                if (locate == null)
                {
                    locate=new Locate();
                    fTransaction.add(R.id.ly_content,locate);
                }
                else
                {
                    locate.onResume();
                    locate.bind();
                    fTransaction.show(locate);
                }
                break;
        }
        fTransaction.commit();

    }

    private void hideAllFragment(FragmentTransaction fTransaction) {
        if (main != null) fTransaction.hide(main);
        if (locate != null) fTransaction.hide(locate);
    }
    private void initBluetoothButton(View view)
    {
        dialog=new ProgressDialog(getContext());
        connectStatus = (TextView) view.findViewById(R.id.connectStatus);
        Button Bluetooth = (Button) view.findViewById(R.id.button5);
        Button send = (Button) view.findViewById(R.id.button);
        send.setOnClickListener(v -> send());
        Bluetooth.setOnClickListener(v -> connect());
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

                if (status == MainService.connecting)
                    dialog.show();
                else
                    dialog.dismiss();

                if (status == MainService.connect)
                    connectStatus.setText("連線狀態:已連線");
                else if (status == MainService.disconnect)
                    connectStatus.setText("連線狀態:連線中斷");
                else if (status == BluetoothConnectCallback.nobind)
                    Toast.makeText(getContext(), "未配對，請先配對", Toast.LENGTH_SHORT).show();
                else if (status == BluetoothConnectCallback.noSearch)
                    Toast.makeText(getContext(), "請確認裝置在附近", Toast.LENGTH_SHORT).show();
                else if (status == BluetoothConnectCallback.bluetoothNoSupport)
                    Toast.makeText(getContext(), "未支援藍芽，請更換設備", Toast.LENGTH_SHORT).show();

            }
        };
        IntentFilter filter = new IntentFilter("MainService");
        // 將 BroadcastReceiver 在 Activity 掛起來。
        getActivity().registerReceiver(receiver, filter);
    }
    private void connect()
    {
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled())
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
}
