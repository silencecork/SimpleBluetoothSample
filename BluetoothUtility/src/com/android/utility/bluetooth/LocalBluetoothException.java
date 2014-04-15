package com.android.utility.bluetooth;

public class LocalBluetoothException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -7732351824023191864L;
    
    private String mMessage;
    
    public LocalBluetoothException() {
        super();
    }
    
    public LocalBluetoothException(String detaiMessage) {
        super(detaiMessage);
        mMessage = detaiMessage;
    }
    
}
