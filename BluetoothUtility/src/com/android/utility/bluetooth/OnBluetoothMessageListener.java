package com.android.utility.bluetooth;

public interface OnBluetoothMessageListener {
    public void onConnected();
    public void onMessageReceived(String message);
    public void onDisconnect();
}
