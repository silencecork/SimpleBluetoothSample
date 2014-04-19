package com.android.utility.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothMessageListener {
    public void onConnected();
    public void onMessageReceived(BluetoothDevice device, String message);
    public void onDisconnect();
}
