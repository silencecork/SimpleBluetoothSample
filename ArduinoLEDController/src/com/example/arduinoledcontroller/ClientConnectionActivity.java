package com.example.arduinoledcontroller;

import java.util.UUID;

import com.android.utility.bluetooth.BluetoothConnectionHelper;
import com.android.utility.bluetooth.OnBluetoothMessageListener;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ClientConnectionActivity extends Activity {
    
    private static final String APP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private static final String APP_UUID = "8778e27e-564b-4c9c-94fb-4b8138ee61f0";
    private BluetoothConnectionHelper mHelper;
    private Button mLedOnButton;
    private Button mLedOffButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.connection_activity);
        
        mLedOnButton = (Button) findViewById(R.id.btn_on);
        mLedOnButton.setOnClickListener(mLedOnClickListener);
        
        mLedOffButton = (Button) findViewById(R.id.btn_off);
        mLedOffButton.setOnClickListener(mLedOffClickListener);
        
        BluetoothDevice device = getIntent().getParcelableExtra("device");
        mHelper = BluetoothConnectionHelper.createClient(UUID.fromString(APP_UUID), device);
        mHelper.setMessageReceiver(mListener);
        mHelper.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.close();
        }
    }
    
    private OnClickListener mLedOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mHelper.isConnect()) {
               mHelper.sendMessage("1");
            }
        }
    };
    
    private OnClickListener mLedOffClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mHelper.isConnect()) {
               mHelper.sendMessage("0");
            }
        }
    };
    
    private OnBluetoothMessageListener mListener = new OnBluetoothMessageListener() {
        @Override
        public void onMessageReceived(BluetoothDevice device, String message) {
            
        }
        @Override
        public void onDisconnect(BluetoothDevice device) {
            Toast.makeText(ClientConnectionActivity.this, "Disconnect", Toast.LENGTH_LONG).show();
            finish();
        }
        @Override
        public void onConnected(BluetoothDevice device) {
            Toast.makeText(ClientConnectionActivity.this, "Connect", Toast.LENGTH_LONG).show();
        }
    };

}