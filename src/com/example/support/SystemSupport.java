package com.example.support;

import java.io.File;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * 一些通用的函数，对Android系统的一些操作。
 * @author Jack
 */
public class SystemSupport {
	public static final String MIME_TYPE_EMAIL = "message/rfc822";
	public static final String MIME_TYPE_TEXT = "text/*";

	/**
	 * @param message
	 */
	public static Intent getStartMessageIntent(String message) {
		return getStartMessageWithNumberIntent(message, null);
	}

	/**
	 * @param message
	 * @param number
	 */
	public static Intent getStartMessageWithNumberIntent(String message,
			String number) {
		Uri uri = null;
		if (number != null) {
			uri = Uri.parse("smsto:" + number);
		} else {
			uri = Uri.parse("smsto:");
		}
		Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
		intent.putExtra("sms_body", message);
		return intent;
	}

	/**
	 * @param email
	 * @param subject
	 * @param message
	 */
	public static Intent getStartEmailIntent(String[] email, String subject,
			String body) {
		Intent data = new Intent(Intent.ACTION_SENDTO);
		data.setData(Uri.parse("mailto: "));
		data.putExtra(Intent.EXTRA_SUBJECT, subject);
		data.putExtra(Intent.EXTRA_TEXT, body);
		return data;
	}

	/**
	 * @param context
	 * @param req_code
	 */
	public static Intent getStartGalleryIntent() {
		Intent intent = new Intent(Intent.ACTION_PICK, null);
		intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
		return intent;
	}

	/**
	 * @param save_path
	 * @param file_name
	 */
	public static Intent getStartCameraIntent(File save_file) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(save_file));
		return intent;
	}

	/**
	 * @param srcLat
	 * @param srcLng
	 * @param tarLat
	 * @param tarLng
	 */
	public static Intent getStartTraficLineIntent(String srcLat, String srcLng,
			String tarLat, String tarLng) {
		String url = String
				.format("http://ditu.google.cn/maps?f=d&source=s_d&saddr=%s,%s&daddr=%s,%s",
						srcLat, srcLng, tarLat, tarLng);
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		return intent;
	}

	/**
	 * @return
	 */
	public static Intent getStartSecuritySettingsIntent() {
		Intent intent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
		return intent;
	}

	public static Intent getInstallApkIntent(String apk) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + apk),
				"application/vnd.android.package-archive");
		return intent;
	}

	/**
	 * @param context
	 * @param contentUri
	 * @return
	 */
	public static String getRealPathFromURI(Context context, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(contentUri, proj, null, null, null);
		String file_name = null;
		if (null != cursor && cursor.moveToFirst()) {
			try {
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				file_name = cursor.getString(column_index);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			file_name = contentUri.getPath();
		}
		if (cursor != null)
			cursor.close();

		return file_name;
	}

	public static boolean saveImage2Gallery(Context context, File file) {
		final Uri STORAGE_URI = Images.Media.EXTERNAL_CONTENT_URI;
		String IMAGE_MIME_TYPE = "image/png";
		ContentValues values = new ContentValues(7);
		values.put(Images.Media.TITLE, file.getName());
		values.put(Images.Media.DISPLAY_NAME, file.getName());
		values.put(Images.Media.DATE_TAKEN, System.currentTimeMillis());
		values.put(Images.Media.MIME_TYPE, IMAGE_MIME_TYPE);
		values.put(Images.Media.ORIENTATION, 0);
		values.put(Images.Media.DATA, file.getAbsolutePath());
		values.put(Images.Media.SIZE, 0);
		Uri uri = context.getContentResolver().insert(STORAGE_URI, values);
		return null != uri;
	}

	/**
	 * 
	 * // 1.调用显示系统默认的输入法 // 方法一、 InputMethodManager imm = (InputMethodManager)
	 * getSystemService(Context.INPUT_METHOD_SERVICE);
	 * imm.showSoftInput(m_receiverView
	 * (接受软键盘输入的视图(View)),InputMethodManager.SHOW_FORCED
	 * (提供当前操作的标记，SHOW_FORCED表示强制显示)); // 方法二、 InputMethodManager
	 * m=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
	 * m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	 * (这个方法可以实现输入法在窗口上切换显示，如果输入法在窗口上已经显示，则隐藏，如果隐藏，则显示输入法到窗口上)
	 * 
	 * // 2.调用隐藏系统默认的输入法
	 * ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE
	 * )).hideSoftInputFromWindow
	 * (WidgetSearchActivity.this.getCurrentFocus().getWindowToken(),
	 * InputMethodManager.HIDE_NOT_ALWAYS); (WidgetSearchActivity是当前的Activity)
	 * 
	 * // 3.获取输入法打开的状态 InputMethodManager imm =
	 * (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	 * boolean isOpen=imm.isActive(); // isOpen若返回true，则表示输入法打开
	 * @param context
	 */
	public static boolean toggleSoftKeyboard(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		return imm.isActive();
	}

	public static void hideSoftKeyboard(Context context, View v) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		if (v != null) {

			imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public static void showSoftKeyboard(Context context, View v) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
		imm.showSoftInputFromInputMethod(v.getApplicationWindowToken(),
				InputMethodManager.SHOW_IMPLICIT);
	}

	public static long readSDCard() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();
			return availCount * blockSize / 1024;
		} else {
			return 0;
		}
	}

	/**
	 * 判断是什么网络
	 * @param ctx
	 * @return 1移动网络 2wifi 3无网络
	 */
	public static DoveBoxNetState getNetState(Context ctx) {
		if (ctx == null)
			throw new IllegalAccessError("Context is Null!!!");
		ConnectivityManager connManager = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager != null) {
			NetworkInfo info = connManager.getActiveNetworkInfo();
			if (info == null)
				return DoveBoxNetState.UNKOWN;
			else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
				return DoveBoxNetState.MOBILE;
		}
		return DoveBoxNetState.WIFI;
	}

	/**
	 * 得到网络类型
	 * 1联通2g 2移动2g 4电信2g
	 * 5 6 12电信3G 3 8联通3G
	 * 
	 * @param ctx
	 * @return
	 */
	public static int getNetMobileType(Context ctx) {
		if (ctx != null) {
			ConnectivityManager connManager = (ConnectivityManager) ctx
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connManager != null) {
				NetworkInfo info = connManager.getActiveNetworkInfo();
				if (info != null
						&& info.getType() == ConnectivityManager.TYPE_MOBILE) {
					return info.getSubtype();
				}
			}
		}
		return -1;
	}

	public enum DoveBoxNetState {
		/**
		 * 无网络
		 */
		UNKOWN,
		/**
		 * wifi
		 */
		WIFI,
		/**
		 * 移动网络
		 */
		MOBILE;
	}
}
