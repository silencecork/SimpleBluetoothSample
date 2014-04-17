package com.android.utility.bluetooth;

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
                    mListener.onMessageReceived(message);
                }
            }
        }
        
    };
    
    public BluetoothConnectionHelper(BluetoothDevice device) {
        mConnection = new ClientConnection(device, mHandler);
    }
    
    public BluetoothConnectionHelper() {
        mConnection = new ServerConnection(mHandler);
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
