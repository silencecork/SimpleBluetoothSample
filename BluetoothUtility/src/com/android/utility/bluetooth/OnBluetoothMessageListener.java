package com.android.utility.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothMessageListener {
    public void onConnected(BluetoothDevice device);
    public void onMessageReceived(BluetoothDevice device, String message);
    public void onDisconnect(BluetoothDevice device);
}
