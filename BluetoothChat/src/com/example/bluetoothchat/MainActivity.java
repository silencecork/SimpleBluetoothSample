package com.example.bluetoothchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        
        Button serverButton = (Button) findViewById(R.id.btn_server);
        serverButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServerAndConnectionActivity.class);
                startActivity(intent);
            }
        });
        
        Button clientButton = (Button) findViewById(R.id.btn_client);
        clientButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DiscoveryActivity.class);
                startActivity(intent);
            }
        });
    }

}
