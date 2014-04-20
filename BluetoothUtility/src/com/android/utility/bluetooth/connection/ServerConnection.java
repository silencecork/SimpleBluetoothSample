package com.android.utility.bluetooth.connection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.android.utility.bluetooth.LocalBluetoothException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ServerConnection implements IConnection {
    
    private static final int MAX_ACCEPT_CONNECTION = 7;
    private Handler mHandler;
    private AcceptConnectionThread mConnectionThread;
    private UUID mUUID;
    private List<ConnectThread> mConnectionList;
    private int mMaxConnection;
    
    public ServerConnection(UUID uuid, Handler handler, int maxConnection) {
        mUUID = uuid;
        mHandler = handler;
        maxConnection = (maxConnection > MAX_ACCEPT_CONNECTION) ? MAX_ACCEPT_CONNECTION : maxConnection;
        mMaxConnection = maxConnection;
        mConnectionList = Collections.synchronizedList(new ArrayList<ConnectThread>(maxConnection));
    }

    @Override
    public void connect() {
        throw new LocalBluetoothException("Server device can not perform this action");
    }

    @Override
    public void close() {
        if (mConnectionList != null) {
            synchronized (mConnectionList) {
                for (ConnectThread th : mConnectionList) {
                    th.stopBluetoothConnectionThread();
                }
                mConnectionList.clear();
            }
        }
        if (mConnectionThread != null) {
            mConnectionThread.close();
        }
    }

    @Override
    public void sendMessage(String message) {
        synchronized (mConnectionList) {
            for (ConnectThread th : mConnectionList) {
                th.send(message);
            }
        }
    }

    @Override
    public boolean isConnect() {
        if (mConnectionList != null) {
            synchronized (mConnectionList) {
                for (ConnectThread th : mConnectionList) {
                    return th.isConnected();
                }
            }
        }
        return false;
    }
    
    public void checkThreadCanAcceptAgain() {
        if (mConnectionThread != null) {
            mConnectionThread.checkThreadCanAcceptAgain();
        }
    }
    
    class AcceptConnectionThread extends Thread {
        private static final String TAG = "AcceptConnectionThread";
        private BluetoothServerSocket mServerSocket;
        private BluetoothAdapter mAdapter;
        private boolean mIsDone;
        private Handler mUIHandler;
        private UUID mUUID;
        
        public AcceptConnectionThread(UUID uuid, Handler handler) {
            super("accept_connection_thread");
            mUUID = uuid;
            mUIHandler = handler;
            mAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        
        public void checkThreadCanAcceptAgain() {
            synchronized (AcceptConnectionThread.this) {
                try {
                    Log.w(TAG, "notify");
                    AcceptConnectionThread.this.notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        @Override
        public void run() {
            try {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord("BluetoothTest", mUUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!mIsDone) {
                if (mConnectionList.size() >= mMaxConnection) {
                    try {
                        synchronized (AcceptConnectionThread.this) {
                            AcceptConnectionThread.this.wait();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, "", e);
                    }
                    continue;
                }
                BluetoothSocket socket = null;
                try {
                    socket = mServerSocket.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (socket == null) {
                    continue;
                }
                
                Log.d(TAG, "has new connection " + socket.getRemoteDevice().getName());
                
                ConnectThread th = new ConnectThread(socket, mUIHandler);
                th.start();
                mConnectionList.add(th);
            }
        }
        
        public void close() {
            if (mServerSocket != null) {
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mIsDone = true;
                checkThreadCanAcceptAgain();
            }
        }
    }
    
    class ConnectThread extends Thread {
        
        private BluetoothSocket mSocket;
        
        private static final String TAG = "ConnectThread";
        
        private boolean mIsDone;
        
        private Handler mUIHandler;
        
        private boolean mIsConnect;
        
        private OutputStream mOut;
        private InputStream mIn;
        
        public ConnectThread(BluetoothSocket socket, Handler handler) {
            super("conneciton_thread_" + socket.getRemoteDevice().getName());
            mUIHandler = handler;
            mSocket = socket;
        }
        
        @Override
        public void run() {

            Log.i(TAG, "create server socket success");
            
            mIsConnect = true;
            BluetoothDevice device = mSocket.getRemoteDevice();
            Message msg = ConnectionHelper.createConnectionMessage(mUIHandler, device);
            msg.sendToTarget();
            
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
                        disconnectCurrentSocket(device);
                    } else {
                        Message dispatchToTargetMsg = ConnectionHelper.createReceivedMessage(mUIHandler, device, message);
                        dispatchToTargetMsg.sendToTarget();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close(mOut);
                close(mIn);
                disconnect(mSocket);
                mIsConnect = false;
                disconnectCurrentSocket(device);
            }
        }
        
        private void disconnectCurrentSocket(BluetoothDevice device) {
            Message disconnectMsg = ConnectionHelper.createDisconnectMessage(mUIHandler, device);
            disconnectMsg.sendToTarget();
            stopBluetoothConnectionThread();
            if (mConnectionList != null) {
                mConnectionList.remove(this);
            }
            checkThreadCanAcceptAgain();
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
        if (mConnectionThread == null) {
            mConnectionThread = new AcceptConnectionThread(mUUID, mHandler);
            mConnectionThread.start();
        }
    }

}
