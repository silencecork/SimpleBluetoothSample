package com.android.utility.bluetooth.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.android.utility.bluetooth.LocalBluetoothException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientConnection implements IConnection {
    
    private BluetoothConnectionThread mConnectionThread;
    
    private BluetoothDevice mDevice;
    
    private BluetoothAdapter mBluetoothAdapter;
    
    private Handler mHandler;
    
    private UUID mUUID;
    
    public ClientConnection(UUID uuid, BluetoothDevice device, Handler handler) {
        mUUID = uuid;
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
    }

    @Override
    public void connect() {
        checkCondition();
        mConnectionThread = new BluetoothConnectionThread(mUUID, mDevice, mHandler);
        mConnectionThread.start();
    }

    @Override
    public void close() {
        if (mConnectionThread != null) {
            mConnectionThread.stopBluetoothConnectionThread();
        }
    }

    @Override
    public void sendMessage(String message) {
        if (mConnectionThread != null && mConnectionThread.isConnected()) {
            mConnectionThread.send(message);
        }
    }

    @Override
    public boolean isConnect() {
        if (mConnectionThread == null) {
            return false;
        }
        
        return mConnectionThread.isConnected();
    }
    
    private void checkCondition() {
        if (mBluetoothAdapter == null) {
            throw new LocalBluetoothException("Can not initial BluetoothAdapter");
        }
        if (mDevice == null) {
            throw new LocalBluetoothException("there is no BluetoothDevice defined");
        }
        if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new LocalBluetoothException("this device " + mDevice.getName() + " is not paired");
        }
    }

    class BluetoothConnectionThread extends Thread {
        private static final String TAG = "BTThread";
        private BluetoothDevice mDevice;
        private boolean mIsDone;
        
        private Handler mUIHandler;
        private OutputStream mOut;
        private InputStream mIn;
        
        private boolean mIsConnect;
        private  BluetoothSocket mSocket;
        
        private UUID mUUID;
        
        BluetoothConnectionThread(UUID uuid, BluetoothDevice device, Handler handler) {
            mUUID = uuid;
            mDevice = device;
            mUIHandler = handler;
        }
        
        public void stopBluetoothConnectionThread() {
            mIsDone = true;
            mIsConnect = false;
            send(DISCONNECT_MESSAGE);
            disconnect(mSocket);
        }
        
        public boolean isConnected() {
            return mIsConnect;
        }
        
        @Override 
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isDiscovering()) {
                adapter.cancelDiscovery();
            }
            
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(mUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (mSocket == null) {
                return;
            }
            
            Log.i(TAG, "create server socket success");
            
            try {
                mSocket.connect();
            } catch (IOException e1) {
                e1.printStackTrace();
                
                return;
            }
            
            
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
                    if (DISCONNECT_MESSAGE.equals(message)) {
                        mUIHandler.sendEmptyMessage(MSG_DISCONNECT);
                        stopBluetoothConnectionThread();
                    } else {
                        Message.obtain(mUIHandler, MSG_RECEIVED_MESSAGE, message).sendToTarget();
                    }
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
        
        private void disconnect(BluetoothSocket socket){
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public void waitForConnection() {
        throw new LocalBluetoothException("Client device can not perform this action");
    }

}
