package com.android.utility.bluetooth.connection;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ConnectionHelper {

    public static Message createConnectionMessage(Handler target, BluetoothDevice device) {
        Message msg = Message.obtain(target, IConnection.MSG_CONNECTED);
        if (device != null) {
            Bundle data = new Bundle();
            data.putParcelable("device", device);
            msg.setData(data);
        }
        return msg;
    }
    
    public static Message createReceivedMessage(Handler target, BluetoothDevice device, String receivedMsg) {
        Message msg = Message.obtain(target, IConnection.MSG_RECEIVED_MESSAGE, receivedMsg);
        if (device != null) {
            Bundle data = new Bundle();
            data.putParcelable("device", device);
            msg.setData(data);
        }
        return msg;
    }
    
    public static Message createDisconnectMessage(Handler target, BluetoothDevice device) {
        Message msg = Message.obtain(target, IConnection.MSG_DISCONNECT);
        if (device != null) {
            Bundle data = new Bundle();
            data.putParcelable("device", device);
            msg.setData(data);
        }
        return msg;
    }
    
}
