package com.android.utility.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class LocalBluetoothManager {
    
    private static final int REQUEST_ENABLE_BT = 1000;

    private static LocalBluetoothManager sInstance;
    
    private BluetoothAdapter mBluetoothAdapter;
    
    private Context mContext;
    
    private BluetoothBroadcastReceiver mReceiver;
    
    private OnOpenBluetoothEventListener mOnOpenBluetoothEventListener;
    
    private OnBluetoothDiscoverEventListener mOnBluetoothDiscoverEventListener;
    
    private LocalBluetoothManager() {
    }
    
    public static LocalBluetoothManager getInstance() {
        if (sInstance == null) {
            sInstance = new LocalBluetoothManager();
        }
        
        return sInstance;
    }
    
    public void startSession(Context context) {
        mContext = context.getApplicationContext();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        
        if (adapter == null) {
            throw new LocalBluetoothException("Your devic not support Bluetooth");
        }
        
        mBluetoothAdapter = adapter;
        mReceiver = new BluetoothBroadcastReceiver();
        IntentFilter btReceiverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        btReceiverIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, btReceiverIntentFilter);
    }
    
    public void endSession() {
        mOnOpenBluetoothEventListener = null;
        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.disable();
        }
        mContext = null;
    }
    
    public boolean isBluetoothTurnOn() {
        checkCondition();
        return mBluetoothAdapter.isEnabled();
    }
    
    public boolean isBluetoothDiscoverying() {
        checkCondition();
        return mBluetoothAdapter.isDiscovering();
    }
    
    public void turnOffBluetooth() {
        checkCondition();
        mBluetoothAdapter.disable();
    }
    
    public void turnOnBluetooth(Activity activity, OnOpenBluetoothEventListener listener) {
        mOnOpenBluetoothEventListener = listener;
        Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }
    
    public boolean isDeviceDiscoverable() {
        if (!isBluetoothTurnOn()) {
            return false;
        }
        
        return mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
    }
    
    public void setDeviceDiscoverable(Activity activity) {
        if (!isBluetoothTurnOn()) {
            return;
        }
        
        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            activity.startActivity(discoverableIntent);
        }
    }
    
    public List<BluetoothDevice> getPairedDevices() {
        if (!isBluetoothTurnOn()) {
            return null;
        }
        
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        ArrayList<BluetoothDevice> retList = new ArrayList<BluetoothDevice>();
        retList.addAll(devices);
        
        return retList;
    }
    
    public void discoveryDevice(OnBluetoothDiscoverEventListener listener) {
        if (!isBluetoothTurnOn()) {
            return;
        }
        mOnBluetoothDiscoverEventListener = listener;
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }
    
    public void pairDevice(BluetoothDevice device) {
        if (!isBluetoothTurnOn()) {
            return;
        }
        try {
            Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("BluetoothUtils", e.getMessage());
        }
    }
    
    public void unpairDevice(BluetoothDevice device) {
        if (!isBluetoothTurnOn()) {
            return;
        }
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("BluetoothUtils", e.getMessage());
        }
    }
    
    public boolean isPairedDevice(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        return device.getBondState() == BluetoothDevice.BOND_BONDED;
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (mOnOpenBluetoothEventListener != null) {
                if (resultCode == Activity.RESULT_OK) {
                    mOnOpenBluetoothEventListener.userConfirmTurnOnRequest();
                } else {
                    mOnOpenBluetoothEventListener.userCanceledTurnOnRequest();
                }
            } 
        }
    }
    
    private void checkCondition() {
        if (mContext == null) {
            throw new LocalBluetoothException("Call startSession first");
        }
        
        if (mBluetoothAdapter == null) {
            throw new LocalBluetoothException("Your devic not support Bluetooth");
        }
    }
    
    public void connectToDevice(BluetoothDevice device) {
        if (!isBluetoothTurnOn()) {
            return;
        }
        if (device == null || device.getBondState() != BluetoothDevice.BOND_BONDED) {
            return;
        }
        
    }
    
    class BluetoothBroadcastReceiver extends BroadcastReceiver {
        
        public BluetoothBroadcastReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mOnBluetoothDiscoverEventListener != null) {
                    mOnBluetoothDiscoverEventListener.discoverDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mOnBluetoothDiscoverEventListener != null) {
                    mOnBluetoothDiscoverEventListener.discoverFinish();
                }
            }
        }
        
    }
}
