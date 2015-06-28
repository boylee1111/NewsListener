package com.boyi.newslistener;


import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.umeng.analytics.MobclickAgent;

public class SplashActivity extends Activity {

	private Button connect_web;
	private MyDataReceiver myDataReceiver;
	public static final String MY_ACTION = "com.example.broadcastreceiverexample.action";
	public static final String MY_SERVICE = "com.example.broadcastreceiverexample.service";
	private Context mContext;
	private final String mPageName = "SplashActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		connect_web = (Button) findViewById(R.id.connectweb);
		connect_web.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (connect_web.getVisibility() == View.VISIBLE) {
					Intent intent = new Intent(
							"android.settings.WIRELESS_SETTINGS");
					startActivity(intent);
				}
			}
		});
		mContext = this;
		MobclickAgent.updateOnlineConfig(mContext);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(myDataReceiver);
		super.onStop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		connect_web.setVisibility(View.INVISIBLE);
		boolean network = checkNet(this.getApplicationContext());
		// init();
		if (network)
			init();
		else {
			Toast.makeText(getApplicationContext(), "网络未连接，请检查网络连接",
					Toast.LENGTH_LONG).show();
			connect_web.setVisibility(View.VISIBLE);
		}
		MobclickAgent.onPageStart(mPageName);
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd(mPageName );
		MobclickAgent.onPause(mContext);
	}

	private void init() {
		
		Intent myIntent = new Intent();
		myIntent.setClass(SplashActivity.this, BroadcastService.class);
		startService(myIntent);
		mHandler.sendEmptyMessageDelayed(LocalValue.GO_HOME,
				LocalValue.SPLASH_DELAY_MILLIS);
		// 取得相应的值，如果没有该值，说明还未写入，用true作为默认值
	
	}

	/**
	 * Handler:接收子线程发送的消息，跳转到不同界面
	 */
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LocalValue.GO_HOME:
				goHome();
				break;
			case 2:
				break;		
			}
			super.handleMessage(msg);
		}
	};
	
	private void goHome() {
		Intent intent = new Intent(SplashActivity.this, MainActivity.class);
		SplashActivity.this.startActivity(intent);
		SplashActivity.this.finish();
	}
	/**
	 * 判断Android客户端网络是否连接
	 * 
	 * @param context
	 * @return 真假
	 */
	public static boolean checkNet(Context context) {

		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {

				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {

					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		myDataReceiver = new MyDataReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MY_ACTION);
		this.registerReceiver(myDataReceiver, filter);
		super.onStart();
	}

	private class MyDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub			
		}
	}


}
