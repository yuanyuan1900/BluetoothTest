package com.example.bluetoothtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DeviceScanActivity extends Activity {
	
	public static DeviceScanActivity devicescanactivity = null;

	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mEditor;
	
	private BluetoothAdapter mBluetoothAdapter;
	private String mDeviceName = null;
	private String mDeviceAddress = null;
	private boolean mScanning;
	private boolean mScanResult = false;
	private Handler mHandler;
	
	private TextView devicename;
	private TextView deviceaddress;
	private TextView scan;
	private Button bind;
	
	private static String preDeviceName = "SimpleBLEPeripheral";
	

	private static final int REQUEST_ENABLE_BT = 1;
	// 10���ֹͣ��������.
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//�ر�WelcomeActivity
		WelcomeActivity.welcomActivity.finish();
		
		//ʵ������Activity����
		devicescanactivity = this;
		
		//������棬���ñ���
		setContentView(R.layout.device_scan);
		getActionBar().setTitle(R.string.scan_device);
		
		//ʵ�����ؼ�����
		devicename = (TextView) findViewById(R.id.devicename);
		deviceaddress = (TextView) findViewById(R.id.deviceaddress);
		scan = (TextView) findViewById(R.id.scan);
		bind = (Button) findViewById(R.id.bind);
		
		bind.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mScanning == false &&mScanResult == true){
					bindDevice();
				}
				else{
					Toast.makeText(DeviceScanActivity.this, R.string.no_device_to_bind, Toast.LENGTH_SHORT).show();
				}
			}
		});
				
		mHandler = new Handler();

		// ��ʼ�� Bluetooth adapter, ͨ�������������õ�һ���ο�����������(API����������android4.3�����ϺͰ汾)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// ����豸���Ƿ�֧������
		if (bluetoothManager == null || mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// ��鵱ǰ�ֻ��Ƿ�֧��ble ����,�����֧���˳�����
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		System.out.println("entered the method of onCreate");
	}

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
	//�����ɨ����������豸�б�mLeDevices���ٽ���ɨ����б�������ɨ����ӵ��豸��ͬ������ظ���ӡ�
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
	protected void onResume() {
		// TODO Auto-generated method stub
		System.out.println("entered the method of onResume");
		super.onResume();
        // Ϊ��ȷ���豸��������ʹ��, �����ǰ�����豸û����,�����Ի������û�Ҫ������Ȩ��������
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        //���������Զ�ɨ���豸
        scanLeDevice(true);		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//����û��ܾ�����������������̣��˳�����
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }		
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//�����Activity����ͣʱ�����û��뿪ɨ�����ȥ�����������飩
        scanLeDevice(false);
	}

	//�������Ϊtrue�����ɨ�裬����������һ������ʮ��������ɨ�裬�Խ��͹��ġ�������Ϊfalse��ֹͣɨ�衣
	//����������������������Ч���˵��Խ��в˵���ˢ�¡�
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mScanResult = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                    scan.setText(R.string.not_scanned);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mScanResult = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
    
    // Device scan callback.
    //�첽ִ�У���ɨ�赽�豸����������mLeScanCallbackʵ���е�onLeScan������
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
    	
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   mDeviceName = device.getName();
                   System.out.println("faxian "+mDeviceName);
                   if(mDeviceName.equals(preDeviceName)){
                	   System.out.println("find "+mDeviceName);
                	   mDeviceAddress = device.getAddress();
                	   devicename.setText(mDeviceName);
                	   deviceaddress.setText(mDeviceAddress);
                	   scan.setText(R.string.scanned);
                	   mScanResult = true;
                	   mScanning = false;
                	   mBluetoothAdapter.stopLeScan(mLeScanCallback);
                	   invalidateOptionsMenu();
                   }
                }
            });
        }
    };
    
    //bind�豸���洢�豸��MAC��ַ
    //����UserControlActivity
    //���豸���ƺ�MAC��ַ����ȥ
    private void bindDevice(){
    	
    	System.out.println("binding the Device");
    	
    	//bind�豸�����豸MAC��ַ����SharedPreferences��
    	mPreferences = getSharedPreferences("boundDevice", MODE_PRIVATE);
    	mEditor = mPreferences.edit();
    	mEditor.putString("NAME", mDeviceName);
    	mEditor.putString("ADDRESS", mDeviceAddress);
    	mEditor.commit();
    	
    	//����UerControlActivity
    	//���豸�����ƺ͵�ַ���͹�ȥ
        final Intent intent = new Intent(this, UserControlActivity.class);
        intent.putExtra(UserControlActivity.EXTRAS_PREVIOUS_ACTIVITY, UserControlActivity.EXTRAS_DEVICESCANACTIVITY);
        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        startActivity(intent);
    }
}
