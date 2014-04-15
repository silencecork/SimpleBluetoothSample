package com.android.utility.bluetooth.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServerConnection implements IConnection {
    
    private Handler mHandler;
    
    private ConnectThread mConnectionThread;
    
    public ServerConnection(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void connect() {
        if (mConnectionThread == null) {
            mConnectionThread = new ConnectThread(mHandler);
            mConnectionThread.start();
        }
    }

    @Override
    public void close() {
        if (mConnectionThread != null) {
            mConnectionThread.stopBluetoothConnectionThread();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (mConnectionThread != null) {
            mConnectionThread.send(message);
        }
    }

    @Override
    public boolean isConnect() {
        return (mConnectionThread != null) ? mConnectionThread.isConnected() : false;
    }
    
    class ConnectThread extends Thread {
        
        private BluetoothServerSocket mServerSocket;
        
        private BluetoothSocket mSocket;
        
        private BluetoothAdapter mAdapter;
        
        private static final String TAG = "ConnectThread";
        
        private boolean mIsDone;
        
        private Handler mUIHandler;
        
        private boolean mIsConnect;
        
        private OutputStream mOut;
        private InputStream mIn;
        
        public ConnectThread(Handler handler) {
            mUIHandler = handler;
            mAdapter = BluetoothAdapter.getDefaultAdapter();
   
            try {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("BluetoothTest", APP_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            try {
                mSocket = mServerSocket.accept();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (mSocket == null) {
                return;
            }
            
            Log.i(TAG, "create server socket success");
            
            mIsConnect = true;
            mUIHandler.sendEmptyMessage(MSG_CONNECTED);
            
            
            try {
                mOut = mSocket.getOutputStream();
                mIn = mSocket.getInputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int bytesRead = -1;
                while (!mIsDone) {
                    bytesRead = mIn.read(buffer);
                    Log.d(TAG, " bytesRead " + bytesRead);
                    String message = new String(buffer, 0, bytesRead);

                    Log.d(TAG, "received message " + message + ", bytesRead " + bytesRead);
                    Message.obtain(mUIHandler, MSG_RECEIVED_MESSAGE, message).sendToTarget();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(mOut);
                close(mIn);
                disconnect(mSocket);
                mIsConnect = false;
                mUIHandler.sendEmptyMessage(MSG_DISCONNECT);
            }
        }
        
        public void stopBluetoothConnectionThread() {
            mIsDone = true;
            mIsConnect = false;
            disconnect(mSocket);
            disconnectServerSocket(mServerSocket);
        }
        
        public boolean isConnected() {
            return mIsConnect;
        }
        
        public void send(String message) {
            if (mOut == null || mIsDone) {
                return;
            }
            try {
                mOut.write(message.getBytes());
                mOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        private void close(Closeable c) {
            if (c != null) {
                try {
                    c.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void disconnectCurrentSocket() {
            disconnect(mSocket);
        }
        
        private void disconnect(BluetoothSocket socket){
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void disconnectServerSocket(BluetoothServerSocket socket){
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

}
