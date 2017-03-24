package com.example.bluetoothtest;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

@SuppressLint("NewApi")
public class BluetoothLEService extends Service{
	
    private final static String TAG = BluetoothLEService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;	
 
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    
    public final static String ACTION_GATT_CONNECTED              = "com.example.bluetoothtest.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED           = "com.example.bluetoothtest.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_DATA_AVAILABLE              = "com.example.bluetoothtest.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA                         = "com.example.bluetoothtest.EXTRA_DATA";
    public final static String ACTION_GATT_SERVICE_NOT_DISCOVERED = "com.example.bluetoothtest.ACTION_GATT_SERVICE_NOT_DISCOVERED";
    public final static String ACTION_GATT_CHARAC_DISCOVERED      = "com.example.bluetoothtest.ACTION_GATT_CHARAC_DISCOVERED";
    
    public final static String HEART_ECG_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public final static String NOTIFY_CHARACTERISTIC = "0000fff1-0000-1000-8000-00805f9b34fb";
    public final static String CLIENT_CHARACTERISTIC_CONFIG ="00002902-0000-1000-8000-00805f9b34fb";
    
    //public final static UUID UUID_ECG_SERVICE = UUID.fromString(SampleGattAttributes.HEART_ECG_SERVICE);
    //public final static UUID UUID_CCC = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    //public final static UUID UUID_NOTIFY_CHARACTERISTIC = UUID.fromString(SampleGattAttributes.NOTIFY_CHARACTERISTIC);
    
    private BluetoothGattService service = null;
    private BluetoothGattCharacteristic characteristic = null;
    
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                System.out.println("Connected from GATT server.");
                // Attempts to discover services after successful connection.
                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                System.out.println("Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        } 
        
        @Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			// TODO Auto-generated method stub
			super.onServicesDiscovered(gatt, status);
			findService();	
		}
        

		@Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    
    private void findService(){
    	service = mBluetoothGatt.getService(UUID.fromString(HEART_ECG_SERVICE));
    	if(service == null){
    		System.out.println(" not Discovered service.");
    		broadcastUpdate(ACTION_GATT_SERVICE_NOT_DISCOVERED);
    		return;
    	}
    	else{
    		System.out.println(" Discovered service.");
    		characteristic = service.getCharacteristic(UUID.fromString(NOTIFY_CHARACTERISTIC));
    		if(characteristic != null){
    			System.out.println(" Discovered characteristic.");
    			broadcastUpdate(ACTION_GATT_CHARAC_DISCOVERED);
    		}
    		else{
    			System.out.println(" not Discovered characteristic.");
    		}
    	}
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, /*new String(data) + "\n" +*/ stringBuilder.toString());
                Log.d("AAA", "Send:     " + stringBuilder.toString());

            }
        //}
        sendBroadcast(intent);
    }    
    
    public class LocalBinder extends Binder {
        BluetoothLEService getService() {
            return BluetoothLEService.this;
        }
    }
    
    private final IBinder mBinder = new LocalBinder();
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.		
		close();
		return super.onUnbind(intent);
	}

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }
    
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                System.out.println("BluetoothLEDevice is reconnected");
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        System.out.println("Trying to create a new connection.");
        return true;
    }
    
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        System.out.println("BluetoothLEDevice is disconnected");
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }
    
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if(descriptor != null)System.out.println("Get CCC");
        else System.out.println("not Get CCC");
        if(enabled == true){
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else{
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);        	
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }	
	
}
