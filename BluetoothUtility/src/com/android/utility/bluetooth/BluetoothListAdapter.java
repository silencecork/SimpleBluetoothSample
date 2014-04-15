package com.android.utility.bluetooth;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BluetoothListAdapter extends BaseAdapter {
    
    private List<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    
    private LayoutInflater mInflater;
    
    public BluetoothListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }
    
    public void setDeviceList(List<BluetoothDevice> deviceList) {
        mDeviceList = deviceList;
        notifyDataSetChanged();
    }
    
    public void addItem(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        if (mDeviceList.contains(device)) {
            return;
        }
        mDeviceList.add(device);
        notifyDataSetChanged();
    }
    
    public void clearAll() {
        mDeviceList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return (mDeviceList != null) ? mDeviceList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (mDeviceList != null) ? mDeviceList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
        }
        
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        
        TextView title = (TextView) convertView.findViewById(android.R.id.text1);
        title.setText(device.getName());
        
        return convertView;
    }

}
