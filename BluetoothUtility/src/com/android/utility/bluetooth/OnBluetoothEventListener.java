package com.android.utility.bluetooth;

import android.bluetooth.BluetoothDevice;

public interface OnBluetoothEventListener {
    
    public void discoverDevice(BluetoothDevice device);
    
    public void discoverFinish();
    
    public void userCanceledTurnOnRequest();
    
    public void userConfirmTurnOnRequest();
}
