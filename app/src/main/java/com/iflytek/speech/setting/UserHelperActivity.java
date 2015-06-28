package com.iflytek.speech.setting;

import com.boyi.newslistener.R;
import android.os.Bundle;
import android.view.Window;
import android.app.Activity;

public class UserHelperActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_user_helper);
	}

}
