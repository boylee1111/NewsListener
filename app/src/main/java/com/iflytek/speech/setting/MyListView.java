package com.iflytek.speech.setting;

import com.example.newsreading.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView;
import android.widget.ListView;

/***
 * 自定义listview
 * 
 * @author Administrator
 * 
 */
public class MyListView extends ListView {
	public MyListView(Context context) {
		super(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/****
	 * 拦截触摸事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		int x = (int) ev.getX();
		int y = (int) ev.getY();
		int itemnum = pointToPosition(x, y);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:			
			if (itemnum == AdapterView.INVALID_POSITION)
				break;
			else {
				if (itemnum == 0) {
					if (itemnum == (getAdapter().getCount() - 1)) {
						// 只有一项
						setSelector(R.drawable.list_round);
					} else {
						// 第一项
						setSelector(R.drawable.list_top_round);
					}
				} else if (itemnum == (getAdapter().getCount() - 1))
					// 最后一项
					setSelector(R.drawable.list_bottom_round);
				else {
					// 中间项
					setSelector(R.drawable.list_center_round);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (itemnum == AdapterView.INVALID_POSITION)
				break;
			else {
				if (itemnum == 0) {
					if (itemnum == (getAdapter().getCount() - 1)) {
						// 只有一项
						setSelector(R.drawable.list_round_selector);
					} else {
						// 第一项
						setSelector(R.drawable.list_top_selector);
					}
				} else if (itemnum == (getAdapter().getCount() - 1))
					// 最后一项
					setSelector(R.drawable.list_bottom_selector);
				else {
					// 中间项
					setSelector(R.drawable.list_center_selector);
				}
			}
			break;
	
		}
		return super.onTouchEvent(ev);
	}
}