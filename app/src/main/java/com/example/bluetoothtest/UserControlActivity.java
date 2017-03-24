package com.example.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

public class UserControlActivity extends Activity {

	private final static String TAG = UserControlActivity.class.getSimpleName();

	public static final String EXTRAS_PREVIOUS_ACTIVITY = "PREVIOUS_ACTIVITY";
	public static final byte EXTRAS_DEVICESCANACTIVITY = 0x01;
	public static final byte EXTRAS_WELCOMEACTIVITY = 0x00;
	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private Button testSwitch;
	private NumberPicker numPicker;
	private static final short mMaxValue = 30;
	private static final short mMinValue = 1;
	private static final short mCurrentValue = 15;

	private String mDeviceName = null;
	private String mDeviceAddress = null;
	private byte mPreviousActivity = 0x03;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLEService mBluetoothLEService;
	private boolean mDeviceScanned = false;
	private boolean testSwicthState = false;
	private boolean mConnected = false;
	private boolean mCharacDiscovered = false;
	private boolean mScanning = false;
	private boolean mServiceBound = false;
	private Handler mHandler;
	private static final int REQUEST_ENABLE_BT = 1;
	// 10���ֹͣ��������.
	private static final long SCAN_PERIOD = 10000;

	//zz
	private StringBuilder stringToProc = new StringBuilder("");
	private ProcessEcg processEcg;
	private boolean isFinished = false;
//yy

	// Code to manage Service lifecycle.
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			System.out.println("binded Service");
			
			//����󶨱�־���������������豸
			mServiceBound = true;
			
			mBluetoothLEService = ((BluetoothLEService.LocalBinder) service).getService();
			if (!mBluetoothLEService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			System.out.println("Connecting......");
			if (!mBluetoothLEService.connect(mDeviceAddress)) {
				Toast.makeText(UserControlActivity.this, getResources().getString(R.string.connection_failed),
						Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLEService = null;
		}
	};

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLEService.ACTION_GATT_CONNECTED.equals(action)) {
				mConnected = true;
				System.out.println("Connected");
				// �����豸������
			} else if (BluetoothLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
				mConnected = false;
				// Toast.makeText(UserControlActivity.this,
				// getResources().getString(R.string.connection_failed),
				// Toast.LENGTH_SHORT).show();
				// finish();
			} else if (BluetoothLEService.ACTION_GATT_SERVICE_NOT_DISCOVERED.equals(action)) {
				// �����ض�UUID����û��Ѱ�ҵ�
				Toast.makeText(UserControlActivity.this, getResources().getString(R.string.not_discovered_service),
						Toast.LENGTH_SHORT).show();
				finish();
			} else if (BluetoothLEService.ACTION_GATT_CHARAC_DISCOVERED.equals(action)) {
				mCharacDiscovered = true;
				System.out.println("Chracteristic is discovered");
			} else if (BluetoothLEService.ACTION_DATA_AVAILABLE.equals(action)) {
				// �����������յ�
				System.out.println(intent.getStringExtra(BluetoothLEService.EXTRA_DATA));
				//zz
				String  dataString = intent.getStringExtra(BluetoothLEService.EXTRA_DATA);
				Log.d("BBB","Receive:  " + dataString);
				stringToProc.append(dataString);
				Log.d("BBB", "Send:     "+stringToProc.toString());
				if(isFinished){
					try {
						ProcessData();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}
	};

	private void ProcessData() throws Exception {
		Log.d("CCC","Receive:  " + stringToProc.toString());
		processEcg = new ProcessEcg(stringToProc.toString());
		Log.d("CCC","dataRec:  " + processEcg.getDataRec());
		try {
			processEcg.procData();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void bluetoothConfig() {
		// ��ʼ�� Bluetooth adapter, ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϺͰ汾)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// Ϊ��ȷ���豸��������ʹ��, �����ǰ�����豸û����,�����Ի������û�Ҫ������Ȩ��������
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		// ��ʼɨ���豸
		if(mDeviceScanned == false){
			scanLeDevice(true);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		// ����û��ܾ�����������������̣��˳�����
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// �������Ϊtrue�����ɨ�裬����������һ������ʮ��������ɨ�裬�Խ��͹��ġ�������Ϊfalse��ֹͣɨ�衣
	// ����������������������Ч���˵��Խ��в˵���ˢ�¡�
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
					if(mDeviceScanned == false){
						Toast.makeText(UserControlActivity.this, R.string.not_scanned, Toast.LENGTH_SHORT).show();
					}
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mDeviceScanned = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Device scan callback.
	// �첽ִ�У���ɨ�赽�豸����������mLeScanCallbackʵ���е�onLeScan������
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					System.out.println("find Device");
					if (mDeviceAddress.equals(device.getAddress())) {
						System.out.println("find " + mDeviceAddress);
						mDeviceScanned = true;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
						mScanning = false;
						if(mServiceBound == true){
							mBluetoothLEService.connect(mDeviceAddress);
						}
						invalidateOptionsMenu();
					}
				}
			});

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_scan, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(R.layout.progressbar_indeterminate);
		}
		return true;
	}

	@Override
	// �����ɨ����������豸�б�mLeDevices���ٽ���ɨ����б�������ɨ����ӵ��豸��ͬ������ظ���ӡ�
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			break;
		}
		return true;
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_control);
		getActionBar().setTitle(R.string.title_ecg_test);

		final Intent intent = getIntent();
		mPreviousActivity = intent.getByteExtra(EXTRAS_PREVIOUS_ACTIVITY, (byte) 0x04);
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

		mHandler = new Handler();

		testSwitch = (Button) findViewById(R.id.testswitch);
		testSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mCharacDiscovered == true){
					if (testSwicthState == false) {
						testSwitch.setText(R.string.stop_test);
						testSwicthState = true;
						//zz
						isFinished = false;

						mBluetoothLEService.setCharacteristicNotification(true);
					} else {
						testSwitch.setText(R.string.start_test);
						testSwicthState = false;
						//zz
						isFinished = true;

						mBluetoothLEService.setCharacteristicNotification(false);
					}					
				}
			}
		});
		
		numPicker = (NumberPicker) findViewById(R.id.numpicker);
		//����numPicker��������Сֵ
		numPicker.setMaxValue(mMaxValue);
		numPicker.setMinValue(mMinValue);
		//����numPicker�ĵ�ǰֵ
		numPicker.setValue(mCurrentValue);

		if (mPreviousActivity == EXTRAS_DEVICESCANACTIVITY) {
			DeviceScanActivity.devicescanactivity.finish();
			mDeviceScanned = true;
			bluetoothConfig();
			System.out.println("the previous Activity is DEVICESCANACTIVITY");
		} else if (mPreviousActivity == EXTRAS_WELCOMEACTIVITY) {
			WelcomeActivity.welcomActivity.finish();
			bluetoothConfig();
			System.out.println("the previous Activity is WELCOMEACTIVITY");
		}

		Intent gattServiceIntent = new Intent(this, BluetoothLEService.class);
		boolean test = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		if (test == true) {
			System.out.println("Sevice is Successfully bound");
		} else {
			System.out.println("Service is not Successfully bound");
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("UserContrlActivity onResume");
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver);
		unbindService(mServiceConnection);
		mBluetoothLEService = null;
		mCharacDiscovered = false;
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLEService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_SERVICE_NOT_DISCOVERED);
		intentFilter.addAction(BluetoothLEService.ACTION_GATT_CHARAC_DISCOVERED);
		return intentFilter;
	}

}
