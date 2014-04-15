
package com.example.bluetoothchat;

import java.util.List;

import com.android.utility.bluetooth.BluetoothListAdapter;
import com.android.utility.bluetooth.LocalBluetoothManager;
import com.android.utility.bluetooth.OnBluetoothEventListener;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class DiscoveryActivity extends ActionBarActivity {
    
    private ListView mListView;
    private BluetoothListAdapter mAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.device_list);
        mAdapter = new BluetoothListAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mItemClickListener);
        
        LocalBluetoothManager.getInstance().startSession(this, mListener);
        if (!LocalBluetoothManager.getInstance().isBluetoothTurnOn()) {
            LocalBluetoothManager.getInstance().turnOnBluetooth(this);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LocalBluetoothManager.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBluetoothManager.getInstance().endSession();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.discover_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_scan) {
            if (mAdapter != null) {
                mAdapter.clearAll();
            }
            LocalBluetoothManager.getInstance().discoveryDevice();
            return true;
        }
        return true;
    }

    private OnBluetoothEventListener mListener = new OnBluetoothEventListener() {
        
        @Override
        public void discoverFinish() {
            Toast.makeText(DiscoveryActivity.this, "discoverFinish", Toast.LENGTH_LONG).show();
            if (mAdapter != null) {
                List<BluetoothDevice> list = LocalBluetoothManager.getInstance().getPairedDevices();
                for (BluetoothDevice device : list) {
                    mAdapter.addItem(device);
                }
            }
        }
        
        @Override
        public void discoverDevice(BluetoothDevice device) {
            if (mAdapter != null) {
                mAdapter.addItem(device);
            }
        }

        @Override
        public void userCanceledTurnOnRequest() {
            finish();
        }

        @Override
        public void userConfirmTurnOnRequest() {
            List<BluetoothDevice> list = LocalBluetoothManager.getInstance().getPairedDevices();
            if (mAdapter != null) {
                mAdapter.setDeviceList(list);
            }
        }
    };
    
    private OnItemClickListener mItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = (BluetoothDevice) parent.getAdapter().getItem(position);
            if (!LocalBluetoothManager.getInstance().isPairedDevice(device)) {
                LocalBluetoothManager.getInstance().pairDevice(device);
            } else {
                Intent intent = new Intent(DiscoveryActivity.this, ClientConnectionActivity.class);
                intent.putExtra("device", device);
                startActivity(intent);
            }
        }
        
    };
}