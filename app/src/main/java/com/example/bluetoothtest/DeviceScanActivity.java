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
	// 10秒后停止查找搜索.
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//关闭WelcomeActivity
		WelcomeActivity.welcomActivity.finish();
		
		//实例化本Activity对象
		devicescanactivity = this;
		
		//引入界面，设置标题
		setContentView(R.layout.device_scan);
		getActionBar().setTitle(R.string.scan_device);
		
		//实例化控件对象
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

		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 检查设备上是否支持蓝牙
		if (bluetoothManager == null || mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
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
	//若点击扫描则先清空设备列表mLeDevices，再进行扫描就列表中与新扫描添加的设备相同，造成重复添加。
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
        // 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        //进入界面后自动扫描设备
        scanLeDevice(true);		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		//如果用户拒绝打开蓝牙，则结束进程，退出程序。
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
		//如果该Activity被暂停时（如用户离开扫面界面去干其他的事情）
        scanLeDevice(false);
	}

	//如果参数为true则进行扫描，并且设置了一个进程十秒后则结束扫描，以降低功耗。若参数为false则停止扫描。
	//并且在做完上述操作后，无效掉菜单以进行菜单的刷新。
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
    //异步执行，若扫描到设备则立即调用mLeScanCallback实例中的onLeScan方法。
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
    
    //bind设备，存储设备的MAC地址
    //启动UserControlActivity
    //将设备名称和MAC地址传过去
    private void bindDevice(){
    	
    	System.out.println("binding the Device");
    	
    	//bind设备，将设备MAC地址存入SharedPreferences中
    	mPreferences = getSharedPreferences("boundDevice", MODE_PRIVATE);
    	mEditor = mPreferences.edit();
    	mEditor.putString("NAME", mDeviceName);
    	mEditor.putString("ADDRESS", mDeviceAddress);
    	mEditor.commit();
    	
    	//启动UerControlActivity
    	//将设备的名称和地址传送过去
        final Intent intent = new Intent(this, UserControlActivity.class);
        intent.putExtra(UserControlActivity.EXTRAS_PREVIOUS_ACTIVITY, UserControlActivity.EXTRAS_DEVICESCANACTIVITY);
        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_NAME, mDeviceName);
        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        startActivity(intent);
    }
}
