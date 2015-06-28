package com.iflytek.speech.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boyi.newslistener.LocalValue;
import com.boyi.newslistener.R;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.inputmethod.EditorInfo;
import com.iflytek.speech.util.ApkInstaller;

public class SettingActivity extends Activity {

	private MyListView listview1;
	private MyListView listview2;
	private SimpleAdapter adapter1;
	private SimpleAdapter adapter2;
	// 语音+安装助手类
	ApkInstaller mInstaller;
	// private ArrayList<Map<String, String>> setting_list;
	private ArrayList<Map<String, String>> setting_list2;
	private Button about;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_setting);

		listview1 = (MyListView) findViewById(R.id.setting_list_view_1);
		adapter1 = new SimpleAdapter(this, getData(),
				R.layout.activity_setting_item,
				new String[] { "info1", "info2" }, new int[] {
						R.id.setting_info_1, R.id.setting_info_2 });
		listview1.setAdapter(adapter1);
		listview1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (arg2 == 0) {
					// 弹出对话选择框
					new AlertDialog.Builder(SettingActivity.this)
							.setTitle("语言设置")
							.setIcon(android.R.drawable.ic_dialog_info)
							.setSingleChoiceItems(
									new String[] { "普通话", "粤语", "河南话", "英语" },
									getIndex(),
									new DialogInterface.OnClickListener() {

										public void onClick(
												DialogInterface dialog,
												int which) {
											String content = getContent(which);
											SharedPreferences sp = getSharedPreferences(
													LocalValue.SP_NAMES,
													MODE_PRIVATE);
											SharedPreferences.Editor editor = sp
													.edit();
											editor.putInt(LocalValue.LANG_ID,
													which);
											editor.putString(
													LocalValue.LANG_IN, content);
											editor.commit();
											new Thread() {
												public void run() {
													Message mymessage = new Message();
													mymessage.what = 1;
													myHandler
															.sendMessage(mymessage);
												}
											}.start();
											dialog.dismiss();
										}
									}).setNegativeButton("取消", null).show();
				} else if (arg2 == 1) {
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();

					} else {
						SpeechUtility.getUtility().openEngineSettings(
								SpeechConstant.ENG_TTS);
					}
				}

			}
		});

		listview2 = (MyListView) findViewById(R.id.setting_list_view_2);
		adapter2 = new SimpleAdapter(this, getData2(),
				R.layout.activity_setting_item,
				new String[] { "info1", "info2" }, new int[] {
						R.id.setting_info_1, R.id.setting_info_2 });
		listview2.setAdapter(adapter2);
		listview2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Map<String, String> map = setting_list2.get(arg2);
				final EditText et = new EditText(SettingActivity.this);
				et.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
				// et.setInputType(EditorInfo.TYPE_CLASS_PHONE);
				if (map.get("info1").equals("语速")) {
					new CustomDialog.Builder(SettingActivity.this)
							.setTitle("请输入语速(0-100)")
							.setView(et)
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int arg1) {
											// TODO Auto-generated method stub
											int speed_get = Integer.parseInt(et
													.getText().toString());
											if (speed_get < 0
													|| speed_get > 100) {
												Toast.makeText(
														getApplicationContext(),
														"请输入0~100以内的数字",
														Toast.LENGTH_LONG)
														.show();
												et.setText("");
											} else {
												SharedPreferences sp = getSharedPreferences(
														LocalValue.SP_NAMES,
														MODE_PRIVATE);
												SharedPreferences.Editor editor = sp
														.edit();
												editor.putInt(LocalValue.SPEED,
														speed_get);
												editor.commit();
												new Thread() {
													public void run() {
														Message mymessage = new Message();
														mymessage.what = 2;
														myHandler
																.sendMessage(mymessage);
													}
												}.start();
											}
											dialog.dismiss();
										}
									}).setNegativeButton("取消", null).create()
							.show();
				} else if (map.get("info1").equals("音调")) {
					new CustomDialog.Builder(SettingActivity.this)
							.setTitle("请输入音调(0-100)")
							.setView(et)
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int arg1) {
											// TODO Auto-generated method stub
											int speed_get = Integer.parseInt(et
													.getText().toString());
											if (speed_get < 0
													|| speed_get > 100) {
												Toast.makeText(
														getApplicationContext(),
														"请输入0~100以内的数字",
														Toast.LENGTH_LONG)
														.show();
												et.setText("");
											} else {
												SharedPreferences sp = getSharedPreferences(
														LocalValue.SP_NAMES,
														MODE_PRIVATE);
												SharedPreferences.Editor editor = sp
														.edit();
												editor.putInt(LocalValue.TONE,
														speed_get);
												editor.commit();
												
												new Thread() {
													public void run() {
														Message mymessage = new Message();
														mymessage.what = 2;
														myHandler
																.sendMessage(mymessage);
													}
												}.start();
												dialog.dismiss();
											}
										}
										
									}).setNegativeButton("取消", null).create()
							.show();

				} else if (map.get("info1").equals("音量")) {
					new CustomDialog.Builder(SettingActivity.this)
							.setTitle("请输入音调(0-100)")
							.setView(et)
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog, int arg1) {
											// TODO Auto-generated method stub
											int speed_get = Integer.parseInt(et
													.getText().toString());
											if (speed_get < 0
													|| speed_get > 100) {
												Toast.makeText(
														getApplicationContext(),
														"请输入0~100以内的数字",
														Toast.LENGTH_LONG)
														.show();
												et.setText("");
											} else {
												SharedPreferences sp = getSharedPreferences(
														LocalValue.SP_NAMES,
														MODE_PRIVATE);
												SharedPreferences.Editor editor = sp
														.edit();
												editor.putInt(LocalValue.SPEED,
														speed_get);
												editor.commit();
												new Thread() {
													public void run() {
														Message mymessage = new Message();
														mymessage.what = 2;
														myHandler
																.sendMessage(mymessage);
													}
												}.start();
												dialog.dismiss();
											}
										}
									}).setNegativeButton("取消", null).create()
							.show();
				}
			}
		});

		about = (Button) findViewById(R.id.about);
		about.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingActivity.this,
						AboutActivity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	private List<Map<String, String>> getData() {
		ArrayList<Map<String, String>> setting_list = new ArrayList<Map<String, String>>();
		SharedPreferences sp = getSharedPreferences(LocalValue.SP_NAMES,
				MODE_PRIVATE);
		String language = getChinese(sp.getInt(LocalValue.LANG_ID, 0));
		Map<String, String> map = new HashMap<String, String>();
		map.put("info1", "话言设置");
		map.put("info2", language);
		setting_list.add(map);

		map = new HashMap<String, String>();
		map.put("info1", "发音人选择");
		map.put("info2", "");
		setting_list.add(map);

		return setting_list;
	}

	private List<Map<String, String>> getData2() {
		setting_list2 = new ArrayList<Map<String, String>>();
		SharedPreferences sp = getSharedPreferences(LocalValue.SP_NAMES,
				MODE_PRIVATE);
		int speed = sp.getInt(LocalValue.SPEED, 50);
		Map<String, String> map = new HashMap<String, String>();
		map.put("info1", "语速");
		map.put("info2", Integer.toString(speed));
		setting_list2.add(map);

		int tone = sp.getInt(LocalValue.TONE, 50);
		map = new HashMap<String, String>();
		map.put("info1", "音调");
		map.put("info2", Integer.toString(tone));
		setting_list2.add(map);

		int volume = sp.getInt(LocalValue.VOLUME, 50);
		map = new HashMap<String, String>();
		map.put("info1", "音量");
		map.put("info2", Integer.toString(volume));
		setting_list2.add(map);

		return setting_list2;
	}

	private int getIndex() {
		SharedPreferences sp = getSharedPreferences(LocalValue.SP_NAMES,
				MODE_PRIVATE);
		int index = sp.getInt(LocalValue.LANG_ID, 0);

		return index;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			/*Intent intent = new Intent(SettingActivity.this, MainActivity.class);
			startActivity(intent);
			finish();*/
		}
		 
		return super.onKeyDown(keyCode, event);
	}

	private String getContent(int index) {
		String content = "";
		switch (index) {
		case 0:
			content = "mandarin";
			break;
		case 1:
			content = "cantonese";
			break;
		case 2:
			content = "henan";
			break;
		case 3:
			content = "english";
			break;
		default:
			break;
		}

		return content;
	}

	private String getChinese(int index) {
		String content = "";
		switch (index) {
		case 0:
			content = "普通话";
			break;
		case 1:
			content = "粤语";
			break;
		case 2:
			content = "河南话";
			break;
		case 3:
			content = "英语";
			break;
		default:
			break;
		}

		return content;
	}

	@SuppressLint("HandlerLeak")
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				adapter1 = new SimpleAdapter(SettingActivity.this, getData(),
						R.layout.activity_setting_item, new String[] { "info1",
								"info2" }, new int[] { R.id.setting_info_1,
								R.id.setting_info_2 });
				listview1.setAdapter(adapter1);
				break;
			case 2:
				adapter2 = new SimpleAdapter(SettingActivity.this, getData2(),
						R.layout.activity_setting_item, new String[] { "info1",
								"info2" }, new int[] { R.id.setting_info_1,
								R.id.setting_info_2 });
				listview2.setAdapter(adapter2);
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
}