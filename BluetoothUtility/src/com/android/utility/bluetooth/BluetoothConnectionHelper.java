package com.android.utility.bluetooth;

import java.util.UUID;

import com.android.utility.bluetooth.connection.ClientConnection;
import com.android.utility.bluetooth.connection.IConnection;
import com.android.utility.bluetooth.connection.ServerConnection;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;

public class BluetoothConnectionHelper {
    
    private OnBluetoothMessageListener mListener;
    
    private IConnection mConnection;
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (mListener != null) {
                if (IConnection.MSG_CONNECTED == msg.what) {
                    mListener.onConnected();
                    
                } else if (IConnection.MSG_DISCONNECT == msg.what) {
                    mListener.onDisconnect();
                } else if (IConnection.MSG_RECEIVED_MESSAGE == msg.what) {
                    String message = (String) msg.obj;
                    BluetoothDevice device = (BluetoothDevice) (msg.getData() != null ? msg.getData().getParcelable("device") : null);
                    mListener.onMessageReceived(device, message);
                }
            }
        }
        
    };
    
    public static BluetoothConnectionHelper createClient(UUID uuid, BluetoothDevice device) {
        return new BluetoothConnectionHelper(uuid, device);
    }
    
    public static BluetoothConnectionHelper createServer(UUID uuid, int maxAllowedConnection) {
        return new BluetoothConnectionHelper(uuid, maxAllowedConnection);
    }
    
    private BluetoothConnectionHelper(UUID uuid, BluetoothDevice device) {
        mConnection = new ClientConnection(uuid, device, mHandler);
    }
    
    private BluetoothConnectionHelper(UUID uuid, int maxConnection) {
        mConnection = new ServerConnection(uuid, mHandler, maxConnection);
    }
    
    public void setMessageReceiver(OnBluetoothMessageListener listener) {
        mListener = listener;
    }
    
    public void connect() {
        mConnection.connect();
    }
    
    public void close() {
        mConnection.close();
    }
    
    public void sendMessage(String message) {
        mConnection.sendMessage(message);
    }
    
    public boolean isConnect() {
        return mConnection.isConnect();
    }
    
    public void waitForConnection() {
        mConnection.waitForConnection();
    }
}
