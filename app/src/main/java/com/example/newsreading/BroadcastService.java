package com.example.newsreading;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class BroadcastService extends Service {

	public static final String MY_ACTION = "com.example.broadcastreceiverexample.action";
	public static final String MY_SERVICE = "com.example.broadcastreceiverexample.service";
	private MyDataReceiver myDataReceiver;
	private String mysearch = "";
	private String newsearch = "";
	private int mode = 0;
	private int index = 0;
	private boolean flag;
	private boolean beginsend = true;

	@Override
	public void onCreate() {
		Log.i("broad", "create");
		// TODO Auto-generated method stub
		myDataReceiver = new MyDataReceiver();
		flag = true;
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		IntentFilter filter = new IntentFilter();
		filter.addAction(MY_SERVICE);
		this.registerReceiver(myDataReceiver, filter);
		Log.i("broad", "send");
		thread1.start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(myDataReceiver);
		super.onDestroy();
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Log.i("socket", (String) msg.obj);
			String result = (String) msg.obj;

			Intent intent = new Intent();
			intent.setAction(MY_ACTION);
			intent.putExtra("result", result);

			sendBroadcast(intent);
		}
	};
	Thread thread1 = new Thread() {
		// boolean network = checkNet(this.);

		public void run() {
			Log.i("broad", "ok");
			while (flag) {

				if (mode == 0) {
					if (!newsearch.equals(mysearch)) {

						String urlcode = "";
						try {
							urlcode = java.net.URLEncoder.encode(newsearch,
									"UTF-8");
						} catch (UnsupportedEncodingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						String strUrl = "http://newsreading.duapp.com/newsreading/index.php?content="
								+ urlcode;
						// String strUrl="http://www.baidu.com";
						URL url = null;
						String result = "";
						try {
							url = new URL(strUrl);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (url != null) {
							try {
								HttpURLConnection urlConn = (HttpURLConnection) url
										.openConnection();
								InputStreamReader in = new InputStreamReader(
										urlConn.getInputStream());
								BufferedReader bufferReader = new BufferedReader(
										in);

								String readLine = null;
								while (((readLine = bufferReader.readLine()) != null)) {
									result += readLine + "\n";
								}
								in.close();
								urlConn.disconnect();
								if (!result.equals("")) {
									Log.i("broad", result);

									Message msg = new Message();
									msg.obj = result;
									mHandler.sendMessage(msg);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						mysearch = newsearch;
					}
				} else if (mode == 1) {
					if (beginsend) {
						String strUrl = "http://newsreading.duapp.com/newsreading/free.php?count="
								+ index;
						// String strUrl="http://www.baidu.com";
						URL url = null;
						String result = "";
						try {
							url = new URL(strUrl);
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (url != null) {
							try {
								HttpURLConnection urlConn = (HttpURLConnection) url
										.openConnection();
								InputStreamReader in = new InputStreamReader(
										urlConn.getInputStream());
								BufferedReader bufferReader = new BufferedReader(
										in);

								String readLine = null;
								while (((readLine = bufferReader.readLine()) != null)) {
									result += readLine + "\n";
								}
								in.close();
								urlConn.disconnect();
								if (!result.equals("")) {

									Message msg = new Message();
									msg.obj = result;
									mHandler.sendMessage(msg);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						beginsend = false;
					}
				}
			}

		}
	};

	private class MyDataReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			mode = intent.getIntExtra("mode", 0);
			if (mode == 0) {
				String search = intent.getStringExtra("search");
				if (!search.equals("")) {
					newsearch = search;
				}
			} else if (mode == 1) {
				beginsend = true;
				index = intent.getIntExtra("index", 0);
			}
		}
	}
}
