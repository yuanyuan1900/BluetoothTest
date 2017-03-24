package com.example.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class WelcomeActivity extends Activity{
	
	public static WelcomeActivity welcomActivity = null;

	private SharedPreferences mPreferences;
	private Handler mHandler;
	private static final long DELAY_PERIOD = 3000;
	private String deviceAddress = null;
	private String deviceName = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		//全屏设置，隐藏窗口所有装饰
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//标题是属于View，所以窗口的修饰部分被隐藏后标题依然有效，需要去掉标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		mHandler = new Handler();
		
		welcomActivity = this;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mPreferences = getSharedPreferences("boundDevice", MODE_PRIVATE);
		deviceAddress = mPreferences.getString("ADDRESS", null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	choiceAcivity();
            }
        }, DELAY_PERIOD);		

	}
	
	void choiceAcivity(){
		if(deviceAddress == null){
			final Intent intent = new Intent(this, DeviceScanActivity.class);
			startActivity(intent);
		}
		else{
			
			deviceName = mPreferences.getString("NAME", null);
			final Intent intent = new Intent(this, UserControlActivity.class);
	        intent.putExtra(UserControlActivity.EXTRAS_PREVIOUS_ACTIVITY, UserControlActivity.EXTRAS_WELCOMEACTIVITY);
	        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_NAME, deviceName);
	        intent.putExtra(UserControlActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress);
			startActivity(intent);
		}
	}
	
}
