package com.android.utility.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothDiscoverEventListener {
    
    public void discoverDevice(BluetoothDevice device);
    
    public void discoverFinish();

}
