package com.example.bluetoothchat;

import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.utility.bluetooth.BluetoothConnectionHelper;
import com.android.utility.bluetooth.LocalBluetoothManager;
import com.android.utility.bluetooth.OnBluetoothMessageListener;

public class ServerAndConnectionActivity extends Activity  {
    
    private static final String APP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private static final String APP_UUID = "8778e27e-564b-4c9c-94fb-4b8138ee61f0";
    
    private OnBluetoothMessageListener mListener = new OnBluetoothMessageListener() {
        @Override
        public void onMessageReceived(BluetoothDevice device, String message) {
            if (message != null) {
                mCurrentText += device.getName();
                mCurrentText += ":";
                mCurrentText += message;
                mReceivedText.setText(mCurrentText);
            }
        }
        @Override
        public void onDisconnect(BluetoothDevice device) {
            Toast.makeText(ServerAndConnectionActivity.this, "Disconnect "  + device.getName(), Toast.LENGTH_LONG).show();
        }
        @Override
        public void onConnected(BluetoothDevice device) {
            Toast.makeText(ServerAndConnectionActivity.this, "Connect " + device.getName(), Toast.LENGTH_LONG).show();
        }
    };
    
    private BluetoothConnectionHelper mHelper;
    private Button mSendButton;
    private EditText mContentInputText;
    private TextView mReceivedText;
    private String mCurrentText = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connection_activity);
        mSendButton = (Button) findViewById(R.id.btn_send);
        mSendButton.setOnClickListener(mSendClickListener);
        mContentInputText = (EditText) findViewById(R.id.input);
        mReceivedText = (TextView) findViewById(R.id.content);
        
        LocalBluetoothManager.getInstance().startSession(this);
        if (!LocalBluetoothManager.getInstance().isBluetoothTurnOn()) {
            LocalBluetoothManager.getInstance().turnOnBluetooth(this);
            return;
        }
        waitConnection();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!LocalBluetoothManager.getInstance().isBluetoothTurnOn()) {
            finish();
        } else {
            waitConnection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            mHelper.close();
        }
        LocalBluetoothManager.getInstance().endSession();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.server_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_discoverable) {
            if (!LocalBluetoothManager.getInstance().isDeviceDiscoverable()) {
                LocalBluetoothManager.getInstance().setDeviceDiscoverable(this);
            }
            return true;
        }
        return true;
    }
    
    private void waitConnection() {
        mHelper = BluetoothConnectionHelper.createServer(UUID.fromString(APP_UUID), 1);
        mHelper.setMessageReceiver(mListener);
        mHelper.waitForConnection();
    }
    
    private OnClickListener mSendClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String content = mContentInputText.getText().toString();
            if (mHelper.isConnect()) {
               mHelper.sendMessage(content);
               mContentInputText.setText("");
            }
        }
    };
    
}
