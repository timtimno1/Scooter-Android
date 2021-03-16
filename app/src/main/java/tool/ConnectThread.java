package tool;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class ConnectThread extends Thread
{
    public static BluetoothSocket mmSocket=null;
    static BluetoothDevice mmDevice=null;
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
        //ii.start();
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
