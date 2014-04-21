package com.example.arduinosensorreceiver;

import java.util.UUID;

import com.android.utility.bluetooth.BluetoothConnectionHelper;
import com.android.utility.bluetooth.OnBluetoothMessageListener;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ClientConnectionActivity extends Activity {
    
    private static final String APP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//    private static final String APP_UUID = "8778e27e-564b-4c9c-94fb-4b8138ee61f0";
    private BluetoothConnectionHelper mHelper;
    private Button mStartReceiveButton;
    private TextView mReceivedText;
    private String mCurrentText = "";
    private ScrollView mScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.connection_activity);
        
        mStartReceiveButton = (Button) findViewById(R.id.btn_receive);
        mStartReceiveButton.setOnClickListener(mSendClickListener);
        mReceivedText = (TextView) findViewById(R.id.content);
        mScrollView = (ScrollView) findViewById(R.id.content_scroller);
        
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
    
    private OnClickListener mSendClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mHelper.isConnect()) {
               mHelper.sendMessage("S");
            }
        }
    };
    
    private OnBluetoothMessageListener mListener = new OnBluetoothMessageListener() {
        @Override
        public void onMessageReceived(BluetoothDevice device, String message) {
            if (message != null) {
                mCurrentText += message;
                mReceivedText.setText(mCurrentText);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
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