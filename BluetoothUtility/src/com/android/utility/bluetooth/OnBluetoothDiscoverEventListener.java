package com.android.utility.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothDiscoverEventListener {
    
    public void discoveredDevice(BluetoothDevice device, int rssi);
    
    public void discoverFinish();

}
