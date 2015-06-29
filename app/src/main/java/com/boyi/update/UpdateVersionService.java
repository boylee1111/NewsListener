package com.boyi.update;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.boyi.newslistener.R;
import com.iflytek.speech.setting.CustomDialog;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 检测安装更新文件的助手类
 * 
 * @author Administrator
 * 
 */
@SuppressLint({ "NewApi", "HandlerLeak" })
public class UpdateVersionService {
	private static final int DOWN = 1;// 用于区分正在下载
	private static final int DOWN_FINISH = 0;// 用于区分下载完成
	private HashMap<String, String> hashMap;// 存储跟心版本的xml信息
	private String fileSavePath;// 下载新apk的厨房地点
	private String updateVersionXMLPath;// 检测更新的xml文件
	private int progress;// 获取新apk的下载数据量,更新下载滚动条
	private boolean cancelUpdate = false;// 是否取消下载
	private Context context;
	private ProgressBar progressBar;
	private Dialog downLoadDialog;
	private TextView tag;
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

	/**
	 * 构造方法
	 * 
	 * @param updateVersionXMLPath
	 *            比较版本的xml文件地址(服务器上的)
	 * @param context
	 *            上下文
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	public UpdateVersionService(String updateVersionXMLPath, Context context) {
		super();
		this.updateVersionXMLPath = updateVersionXMLPath;
		this.context = context;

		// 如果本系统为4.0以上（Build.VERSION_CODES.ICE_CREAM_SANDWICH为android4.0）
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// 详见StrictMode文档
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().build());
		}
	}

	/**
	 * 检测是否可更新
	 * 
	 * @return
	 */
	public void checkUpdate() {
		if (isUpdate()) {
			showUpdateVersionDialog();// 显示提示对话框
		} else {
			Toast.makeText(context, "已经是最新版本", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 更新提示框
	 */
	private void showUpdateVersionDialog() {

		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
		customBuilder
				.setTitle("软件更新")
				.setMessage("检测到新版本,是否下载更新")
				.setNegativeButton("更新", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 显示下载对话框
						showDownloadDialog();
					}
				})
				.setPositiveButton("稍后更新",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

		Dialog noticeDialog = customBuilder.create();
		noticeDialog.show();
	}

	/**
	 * 下载的提示框
	 */
	protected void showDownloadDialog() {
		{
			// 构造软件下载对话框
			CustomDialog.Builder builder = new CustomDialog.Builder(context);
			builder.setTitle("正在更新");
			// 给下载对话框增加进度条
			final LayoutInflater inflater = LayoutInflater.from(context);
			View v = inflater.inflate(R.layout.downloaddialog, null);
			progressBar = (ProgressBar) v.findViewById(R.id.updateProgress);
			tag = (TextView) v.findViewById(R.id.tag);
			builder.setView(v);
			// 取消更新
			builder.setNegativeButton("取消", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					// 设置取消状态
					cancelUpdate = true;
				}
			});
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
	 * 判断是否可更新
	 * 
	 * @return
	 */
	private boolean isUpdate() {
		int versionCode = getVersionCode(context);

		try {
			// 把version.xml放到网络上，然后获取文件信息
			URL url = new URL(updateVersionXMLPath);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000);
			conn.setRequestMethod("GET");// 必须要大写
			InputStream inputStream = conn.getInputStream();
			// 解析XML文件。

			ParseXmlService service = new ParseXmlService();
			hashMap = service.parseXml(inputStream);
			inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (null != hashMap) {
			int serverCode = Integer.valueOf(hashMap.get("versionCode"));
			// 版本判断
			if (serverCode > versionCode) {
				/*
				 * Toast.makeText(context, "新版本是: " +
				 * serverCode+"当前版本是："+versionCode, Toast.LENGTH_SHORT).show();
				 */
				return true;
			}
		}
		return false;

	}

	/**
	 * 获取当前版本和服务器版本.如果服务器版本高于本地安装的版本.就更新
	 * 
	 * @param context2
	 * @return
	 */
	private int getVersionCode(Context context2) {
		int versionCode = 0;
		try {
			// 获取软件版本号，对应AndroidManifest.xml下android:versionCode
			versionCode = context.getPackageManager().getPackageInfo(
					"com.boyi.newslistener", 0).versionCode;
			/*
			 * Toast.makeText(context, "当前版本是: " + versionCode,
			 * Toast.LENGTH_SHORT) .show();
			 */
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;

	}

	/**
	 * 安装apk文件
	 */
	private void installAPK() {
		File apkfile = new File(fileSavePath, hashMap.get("fileName") + ".apk");
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		System.out.println("filepath=" + apkfile.toString() + "  "
				+ apkfile.getPath());
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		context.startActivity(i);
		// android.os.Process.killProcess(android.os.Process.myPid());//
		// 如果不加上这句的话在apk安装完成之后点击单开会崩溃

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
					
					URL url = new URL(hashMap.get("loadUrl"));
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
							hashMap.get("fileName") + ".apk");
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
}
