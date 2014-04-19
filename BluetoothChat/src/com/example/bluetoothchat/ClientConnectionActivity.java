package com.example.bluetoothchat;

import java.util.UUID;

import com.android.utility.bluetooth.BluetoothConnectionHelper;
import com.android.utility.bluetooth.OnBluetoothMessageListener;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ClientConnectionActivity extends Activity {
    
    private static final String APP_UUID = "937d0cd9-d263-4344-8d72-e561d803dd07";
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
            String content = mContentInputText.getText().toString();
            if (mHelper.isConnect()) {
               mHelper.sendMessage(content);
               mContentInputText.setText("");
            }
        }
    };
    
    private OnBluetoothMessageListener mListener = new OnBluetoothMessageListener() {
        @Override
        public void onMessageReceived(String message) {
            if (message != null) {
                mCurrentText += message;
                mReceivedText.setText(mCurrentText);
            }
        }
        @Override
        public void onDisconnect() {
            Toast.makeText(ClientConnectionActivity.this, "Disconnect", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onConnected() {
            Toast.makeText(ClientConnectionActivity.this, "Connect", Toast.LENGTH_LONG).show();
        }
    };

}