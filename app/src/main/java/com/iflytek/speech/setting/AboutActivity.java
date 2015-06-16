package com.iflytek.speech.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.newslistener.R;
import com.example.update.UpdateVersionService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AboutActivity extends Activity {

	
	private MyListView listview;
	private ArrayList<Map<String,String>> about_list;
	private UpdateVersionService updateVersionService;
	private TextView version;
	private static final String UPDATEVERSIONXMLPATH = "http://newsreading.duapp.com/newsreadingapk/version.xml";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_about);
		
		listview = (MyListView) findViewById(R.id.about_list_view);
		SimpleAdapter listItemAdapter = new SimpleAdapter(this, getData(),
				R.layout.activity_about_item, new String[] {
						"info1"}, new int[] {
						R.id.about_info1});

		listview.setAdapter(listItemAdapter);
		listview.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Map<String, String> map = about_list.get(arg2);
				if(map.get("info1").equals("版本更新"))
				{
					updateVersionService = new UpdateVersionService(
							UPDATEVERSIONXMLPATH, AboutActivity.this);
					updateVersionService.checkUpdate();
				}
				else if(map.get("info1").equals("版本说明"))
				{
					startActivity(new Intent(AboutActivity.this,VersionInfoActivity.class));
				}
				else if(map.get("info1").equals("使用指南"))
				{
					startActivity(new Intent(AboutActivity.this,UserHelperActivity.class));
				}
				
			}});
		version = (TextView) findViewById(R.id.person_info_1);

		try {
			String pkName = this.getPackageName();
			String versionName = this.getPackageManager().getPackageInfo(
					pkName, 0).versionName;

			version.setText("V" + versionName);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			/*startActivity(new Intent(AboutActivity.this,MainActivity.class));
			finish();*/
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private List<Map<String, String>> getData() {
		about_list=new ArrayList<Map<String,String>>();
		Map<String,String> map=new HashMap<String,String>();
		map.put("info1", "版本更新");
		about_list.add(map);
		
		map=new HashMap<String,String>();
		map.put("info1", "版本说明");
		about_list.add(map);
		
		map=new HashMap<String,String>();
		map.put("info1", "使用指南");
		about_list.add(map);
		
		return about_list;
	}

}
