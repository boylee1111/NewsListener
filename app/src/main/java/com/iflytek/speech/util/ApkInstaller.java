package com.iflytek.speech.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.speech.setting.CustomDialog;
import com.example.newsreading.R;

/**
 * 弹出提示框，下载服务组件
 */
public class ApkInstaller {
	private static final int DOWN = 1;// 用于区分正在下载
	private static final int DOWN_FINISH = 0;// 用于区分下载完成
	private Activity mActivity;
	private Context context;
	private ProgressBar progressBar;
	private Dialog downLoadDialog;
	private TextView tag;
	private boolean cancelUpdate = false;// 是否取消下载
	private String fileSavePath;// 下载新apk的厨房地点
	private HashMap<String, String> hashMap;// 存储跟心版本的xml信息
	private int progress;// 获取新apk的下载数据量,更新下载滚动条
	private Handler handler = new Handler() {// 跟心ui

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch ((Integer) msg.obj) {
			case DOWN:
				progressBar.setProgress(progress);
				tag.setText(progress + "%");
				break;
			case DOWN_FINISH:
				// cancelUpdate=true;
				Toast.makeText(context, "文件下载完成,正在安装更新", Toast.LENGTH_SHORT)
						.show();
				installAPK();
				break;

			default:
				break;
			}
		}
	};

	public ApkInstaller(Activity activity) {
		mActivity = activity;
		this.context = activity;
	}

	@SuppressWarnings("deprecation")
	public void install() {
		final Dialog dialog = new Dialog(mActivity, R.style.dialog);
		LayoutInflater inflater = mActivity.getLayoutInflater();
		View alertDialogView = inflater.inflate(R.layout.install_dialog, null);
		dialog.setContentView(alertDialogView);
		Button okButton = (Button) alertDialogView.findViewById(R.id.ok);
		Button cancelButton = (Button) alertDialogView
				.findViewById(R.id.cancel);
		// TextView comeText=(TextView)
		// alertDialogView.findViewById(R.id.title);
		// comeText.setTypeface(Typeface.MONOSPACE,Typeface.ITALIC);
		// 确认
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				showDownloadDialog();
				// String url = SpeechUtility.getUtility().getComponentUrl();
				// String assetsApk="SpeechService.apk";
				// processInstall(mActivity, url,assetsApk);
			}
		});
		// 取消
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.show();
		WindowManager windowManager = mActivity.getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		lp.width = (int) (display.getWidth()); // 设置宽度
		dialog.getWindow().setAttributes(lp);
		return;
	}

	/**
	 * 如果服务组件没有安装打开语音服务组件下载页面，进行下载后安装。
	 */
	private boolean processInstall(Context context, String url, String assetsApk) {
		// 直接下载方式
		Uri uri = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(it);
		return true;
	}

	/**
	 * 下载的提示框
	 */
	protected void showDownloadDialog() {
		{
			// 构造软件下载对话框
			CustomDialog.Builder builder = new CustomDialog.Builder(context);
			builder.setTitle("正在下载");
			// 给下载对话框增加进度条
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.downloaddialog, null);
			progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
			tag = (TextView) v.findViewById(R.id.tag);
			builder.setView(v);
			// 取消更新
			builder.setNegativeButton("取消",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int arg1) {
							// TODO Auto-generated method stub
							dialog.dismiss();
							// 设置取消状态
							cancelUpdate = true;
						}

					});
			/*
			 * builder.setNegativeButton("取消", new OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { dialog.dismiss(); // 设置取消状态 //cancelUpdate = true; } });
			 */
			downLoadDialog = builder.create();
			downLoadDialog.show();
			// 现在文件
			downloadApk();
		}

	}

	/**
	 * 下载apk,不能占用主线程.所以另开的线程
	 */
	private void downloadApk() {
		new downloadApkThread().start();

	}

	/**
	 * 下载apk的方法
	 * 
	 * @author rongsheng
	 * 
	 */
	public class downloadApkThread extends Thread {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			try {
				// 判断SD卡是否存在，并且是否具有读写权限
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					// 获得存储卡的路径
					String sdpath = Environment.getExternalStorageDirectory()
							+ "/";
					fileSavePath = sdpath + "Download";
					Log.i("newsreading", fileSavePath);
					URL url = new URL(
							"http://newsreading.duapp.com/newsreadingapk/SpeechService.apk");
					// 创建连接
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setReadTimeout(5 * 1000);// 设置超时时间
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Charser",
							"GBK,utf-8;q=0.7,*;q=0.3");
					// 获取文件大小
					int length = conn.getContentLength();
					// 创建输入流
					InputStream is = conn.getInputStream();

					File file = new File(fileSavePath);
					// 判断文件目录是否存在
					if (!file.exists()) {
						file.mkdir();
					}
					File apkFile = new File(fileSavePath,
							"SpeechService.apk");
					FileOutputStream fos = new FileOutputStream(apkFile);
					int count = 0;
					// 缓存
					byte buf[] = new byte[1024];
					// 写入到文件中
					do {
						int numread = is.read(buf);
						count += numread;
						// 计算进度条位置
						progress = (int) (((float) count / length) * 100);
						// 更新进度
						Message message = new Message();
						message.obj = DOWN;
						handler.sendMessage(message);
						if (numread <= 0) {
							// 下载完成
							// 取消下载对话框显示
							downLoadDialog.dismiss();
							Message message2 = new Message();
							message2.obj = DOWN_FINISH;
							handler.sendMessage(message2);
							break;
						}
						// 写入文件
						fos.write(buf, 0, numread);
					} while (!cancelUpdate);// 点击取消就停止下载.
					fos.close();
					is.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 安装apk文件
	 */
	private void installAPK() {
		File apkfile = new File("SpeechService.apk");
		/*if (!apkfile.exists()) {
			return;
		}*/
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		System.out.println("filepath=" + apkfile.toString() + "  "
				+ apkfile.getPath());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.fromFile(new File(fileSavePath+"/" + apkfile.toString())),
				"application/vnd.android.package-archive");
		context.startActivity(i);
		// android.os.Process.killProcess(android.os.Process.myPid());//
		// 如果不加上这句的话在apk安装完成之后点击单开会崩溃

	}
}
