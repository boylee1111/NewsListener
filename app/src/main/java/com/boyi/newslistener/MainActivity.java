package com.boyi.newslistener;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.setting.CustomDialog;
import com.iflytek.speech.setting.SettingActivity;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.speech.util.JsonParser;
import com.umeng.analytics.MobclickAgent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Context mContext;
	private final  String mPageName = "MainActivity";

	private ImageButton setting;// 设置
	private ImageButton start;// 开始
	private Button continue_read;// 继续播放
	private Button pause;// 暂停
	private Button type;
	private int todaynews = 0;// 0为自由模式，1,为今日头条
	private ApkInstaller mInstaller;
	public static final int CMD_STOP_SERVICE = 0;
	public static final int WRITE_MODE = 1;
	public static final int AUTO_MODE = 0;
	private static String TAG = "IatDemo";
	private int start_tag = 0;// 0为语音输入，1为朗读
	private int news_index = 0;
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog iatDialog;
	// 听写结果内容
	private EditText mResultText;

	private Toast mToast;
	private MyDataReceiver myDataReceiver;
	int ret = 0;// 函数调用返回值

	// 语音合成对象
	private SpeechSynthesizer mTts;
	private int mode = AUTO_MODE;//
	private Button mode_choosed;

	public static final String MY_ACTION = "com.boyi.broadcastreceiver.action";
	public static final String MY_SERVICE = "com.boyi.broadcastreceiver.service";

	@SuppressLint({ "ShowToast", "CutPasteId" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mInstaller = new ApkInstaller(MainActivity.this);
		
		if (!SpeechUtility.getUtility().checkServiceInstalled()) {
			mInstaller.install();
		}
		
		start_tag = 0;
		// 开启服务
		type = (Button) findViewById(R.id.type);
		type.setText("今日头条");
		type.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				findViewById(R.id.pause).setEnabled(false);
				findViewById(R.id.iat_stop).setEnabled(false);
				mResultText.setText("");
				if (todaynews == 0)// 自由点播
				{
					todaynews = 1;
					news_index = 0;
					type.setText("自由点播");
					if (mTts != null)
						mTts.stopSpeaking();
					mode_choosed.setVisibility(View.INVISIBLE);
				} else if (todaynews == 1) {// 今日头条
					todaynews = 0;
					if (mTts != null)
						mTts.stopSpeaking();
					type.setText("今日头条");
					mode_choosed.setVisibility(View.VISIBLE);
				}
			}
		});
		Log.i("broad", "init");
		mode_choosed = (Button) findViewById(R.id.mode2);
		mode_choosed.setText("手动模式");
		mode = AUTO_MODE;
		mode_choosed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mTts != null)
					mTts.stopSpeaking();
				findViewById(R.id.pause).setEnabled(false);
				findViewById(R.id.iat_stop).setEnabled(false);
				mResultText.setText("");
				// TODO Auto-generated method stub
				if (mode == AUTO_MODE) {
					mode = WRITE_MODE;
					mode_choosed.setText("语音模式");
				} else {
					mode = AUTO_MODE;
					mode_choosed.setText("手动模式");
				}
			}
		});
		/*
		 * Intent myIntent = new Intent(); myIntent.setClass(MainActivity.this,
		 * BroadcastService.class); startService(myIntent);
		 */
		// 初始化识别对象
		mIat = SpeechRecognizer.createRecognizer(this, mInitListener);
		// 初始化听写Dialog,如果只使用有UI听写功能,无需创建SpeechRecognizer
		iatDialog = new RecognizerDialog(this, mInitListener);

		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mResultText = ((EditText) findViewById(R.id.iat_text));
		mResultText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    String str = s.toString();
                    int pos = str.indexOf("<script");
                    if (pos != -1) {
                        s.delete(pos, s.length());
                    }
//                    s.delete(pos, s.length() - 1);
                }
			}
		});

		// 语音朗读
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
		setParam();
		findViewById(R.id.pause).setEnabled(false);
		findViewById(R.id.iat_stop).setEnabled(false);
		start = (ImageButton) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				findViewById(R.id.pause).setEnabled(false);
				findViewById(R.id.iat_stop).setEnabled(false);
				if (start_tag == 1)// 退出语音朗读
				{
					mTts.stopSpeaking();
					start_tag = 0;
				}
				mResultText.setText(null);// 清空显示内容
				// 设置参数
				if (todaynews == 0) {
					if (mode == AUTO_MODE) {
						// 显示听写对话框
						iatDialog.setListener(recognizerDialogListener);
						iatDialog.show();
						showTip(getString(R.string.text_begin));
					} else if (mode == WRITE_MODE) {
						final EditText ipinfo = new EditText(MainActivity.this);
						CustomDialog.Builder customBuilder = new CustomDialog.Builder(
								MainActivity.this);
						customBuilder.setTitle("请输入关键字");
						customBuilder.setContentView(ipinfo);
						customBuilder.setPositiveButton("确定",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										String content = ipinfo.getText()
												.toString();
										if (!content.equals("")) {

											if (content.length() > 8)
												content = content.substring(0,
														8);
											Intent myintent = new Intent();
											myintent.setAction(MY_SERVICE);
											myintent.putExtra("search", content);
											myintent.putExtra("mode", 0);
											sendBroadcast(myintent);
											Log.i("broad", content);
											dialog.dismiss();
											read("搜索"+content+"中，请稍候");
										} else {
											showTip("请输入搜索关键字");
										}
									}
								});
						customBuilder.setNegativeButton("取消", null);
						customBuilder.create().show();
					}
				} else if (todaynews == 1) {
					if (news_index != -1) {
						Intent myintent = new Intent();
						myintent.setAction(MY_SERVICE);
						myintent.putExtra("index", news_index);
						myintent.putExtra("mode", 1);
						sendBroadcast(myintent);
						read("自动搜索今日头条新闻，请稍后");
						news_index++;
					} else {
						CustomDialog.Builder customBuilder = new CustomDialog.Builder(
								MainActivity.this);
						customBuilder.setTitle("提示");
						customBuilder.setMessage("今日头条已经播报完毕！");
						customBuilder.setPositiveButton("重新播报",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										news_index = 0;
										Intent myintent = new Intent();
										myintent.setAction(MY_SERVICE);
										myintent.putExtra("index", news_index);
										myintent.putExtra("mode", 1);
										sendBroadcast(myintent);
										dialog.dismiss();
										news_index++;
									}
								});
						customBuilder.setNegativeButton("去自由点播",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										todaynews = 0;// 自由点播模式
										dialog.dismiss();
										mode_choosed
												.setVisibility(View.VISIBLE);
									}
								});
						customBuilder.create().show();
					}
				}
			}
		});

		pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				mTts.pauseSpeaking();
			}
		});
		continue_read = (Button) findViewById(R.id.iat_stop);
		continue_read.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setParam();
				mTts.resumeSpeaking();
			}
		});
		setting = (ImageButton) findViewById(R.id.setting);
		setting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent myintent = new Intent(MainActivity.this,
						SettingActivity.class);
				startActivity(myintent);
				//finish();
			}
		});

		AdView mAdView = (AdView) findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		mAdView.loadAd(adRequest);
        mContext = this;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		super.onResume();
		MobclickAgent.onPageStart(mPageName);
		MobclickAgent.onResume(mContext);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd( mPageName );
		MobclickAgent.onPause(mContext);
	}

	/**
	 * 初期化监听。语音朗读
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			/*
			 * if (code == ErrorCode.SUCCESS) {
			 * ((Button)findViewById(R.id.tts_play)).setEnabled(true); }
			 */

		}
	};

	/**
	 * 初始化监听器。语音识别
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.i("newslistener", Integer.toString(code));
			if (code == ErrorCode.SUCCESS) {
				/*
				 * findViewById(R.id.pause).setEnabled(false);
				 * findViewById(R.id.iat_stop).setEnabled(false);
				 */
				findViewById(R.id.start).setEnabled(true);
			}
			else
				Toast.makeText(getApplicationContext(), "shibai11111111111111111111", Toast.LENGTH_LONG).show();
			
		}
	};

	/**
	 * 听写UI监听器，显示对话框
	 */
	private RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			String text = JsonParser.parseIatResult(results.getResultString());
			text = text.replaceAll("。", "");
			text = text.replaceAll("！", "");
			if (!text.equals("")) {
				if (text.length() > 8)
					text = text.substring(0, 8);

				Intent myintent = new Intent();
				myintent.setAction(MY_SERVICE);
				myintent.putExtra("mode", 0);
				myintent.putExtra("search", text);
				sendBroadcast(myintent);
				// Log.i("broad", text);
				read("正在搜索" + text + ",请稍候");
			}
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			Log.i("newsreading", error.toString());
			showTip(error.getPlainDescription(true));
				//Toast.makeText(getApplicationContext(), "shibai11111111111111111111", Toast.LENGTH_LONG).show();
		}

	};

	/**
	 * 语音朗读
	 */
	private void read(String content_text) {
		String text = content_text;
		start_tag = 1;// 朗读模式
		findViewById(R.id.pause).setEnabled(true);
		findViewById(R.id.iat_stop).setEnabled(true);
		int code = mTts.startSpeaking(text, mTtsListener);
		Log.i("newsreading",Integer.toString(code));
		if (code != ErrorCode.SUCCESS) {
			Log.i("newsreading",Integer.toString(ErrorCode.ERROR_COMPONENT_NOT_INSTALLED));
			if (code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED) {
				// 未安装则跳转到提示安装页面
				mInstaller.install();
			} else {
				showTip("语音合成失败,错误码: " + code);
			}
		}
	}

	/**
	 * 合成回调监听。语音播报新闻内容
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		@Override
		public void onSpeakBegin() {
			showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {

			// mToast.show();
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
		}

		@Override
		public void onCompleted(SpeechError error) {
			//if (error == null) {
			//	showTip("播放完成");
			//} else 
			if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}
	};

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	/**
	 * 语音转化 参数设置
	 * 
	 * @param param
	 * @return
	 */
	@SuppressLint("SdCardPath")
	public void setParam() {
		SharedPreferences sp = getSharedPreferences(LocalValue.SP_NAMES,
				MODE_PRIVATE);
		String lang = sp.getString(LocalValue.LANG_IN, "mandarin");
		if (lang.equals("mandarin")) {
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lang);
		}
		// 设置语音前端点
		mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
		// 设置语音后端点
		mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
		// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_PTT, "1");
		// 设置音频保存路径
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,
				"/sdcard/iflytek/wavaudio.pcm");
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
		// 设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, "");

		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED,
				Integer.toString(sp.getInt(LocalValue.SPEED, 50)));

		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH,
				Integer.toString(sp.getInt(LocalValue.TONE, 50)));

		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME,
				Integer.toString(sp.getInt(LocalValue.VOLUME, 50)));
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(myDataReceiver);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时释放连接
		mIat.cancel();
		mIat.destroy();

		mTts.stopSpeaking();
		// 退出时释放连接
		mTts.destroy();

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
			String content = intent.getStringExtra("result");
			// TODO Auto-generated method stub
			if (todaynews == 1) {
				// mTextView.setText(content);
				// Log.i("broad",content.substring(0,20));

				news_index = Integer.parseInt(content.substring(0, 1));
				showTip(Integer.toString(news_index));
				if (news_index == 9) {
					mResultText.setText("今日头条已经播报完毕");
					content = "今日头条已经播报完毕";
					news_index = -1;
				} else {
					news_index++;
					content = content.substring(1, content.length() - 1);
					mResultText.setText(content);
					content += "播报完毕，请继续！";
				}
			} else {
				mResultText.setText(content);
				if (!content.equals("抱歉没有找到，请换下一条"))
					content += "播报完毕，请继续！";
			}
			read(content);
		}
	}

	// 确认对话框
	protected void dialog() {
		CustomDialog.Builder builder = new CustomDialog.Builder(
				MainActivity.this);
		builder.setMessage("确定要退出么？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (mIat != null) {
					mIat.cancel();
					mIat.destroy();
				}
				if (mTts != null) {
					mTts.stopSpeaking();
					// 退出时释放连接
					mTts.destroy();
				}
				dialog.dismiss();
				MainActivity.this.finish();
				Intent myintent = new Intent();
				myintent.setClass(MainActivity.this, BroadcastService.class);
				stopService(myintent);
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	// 绑定返回键事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			dialog();
			return false;
		}
		return false;
	}

}
