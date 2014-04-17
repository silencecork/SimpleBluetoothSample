package com.android.utility.bluetooth.connection;

import java.util.UUID;

public interface IConnection {
    
    public static final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    public static final int MSG_CONNECTED = 0;
    
    public static final int MSG_RECEIVED_MESSAGE = 1;
    
    public static final int MSG_DISCONNECT = 2;
    
    public static String DISCONNECT_MESSAGE = "btcmd:disconnect";
    
    public void connect();
    
    public void close();
    
    public void sendMessage(String message);
    
    public boolean isConnect();
    
    public void waitForConnection();
    
}
