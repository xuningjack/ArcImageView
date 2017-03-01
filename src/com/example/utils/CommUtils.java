package com.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class CommUtils {
	public static final String APP_CLIENT_VERSION = "MIGAMEAPP_1.0.0";
	/**
	 * 调试日志的
	 */
	public static final boolean DEBUG = true;

	protected static final int BYTES_IN_MEGA = 1048576;
	protected static final int BYTES_IN_KILO = 1024;

	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
			"6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	/**
	 * URL 加密密码
	 */
	private static final byte[] URL_KEY = "_&L^W%&*20120724#$U%I)M%I^@"
			.getBytes();
	private static final byte[] HASH_KEY = "_&W^A%&*20120814#$D%T)M%R^@"
			.getBytes();

	/**
	 * 客户端显示页面的大小
	 */
	public static final String PAGE_SIZE = "15";
	public static final int PAGE_COUNT_REQUEST = 15;

	/**
	 * 获得本地缓存的名字，对url进行hash得到
	 */
	public static final String getCacheFileName(String url) {
		return getMD5(url);
	}

	public static final String getMD5(String string) {
		if (TextUtils.isEmpty(string)) {
			return null;
		}
		MessageDigest digester = null;
		try {
			digester = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			if (CommUtils.DEBUG)
				e.printStackTrace();
			return null;
		}
		digester.update(string.getBytes());
		byte[] digest = digester.digest();
		return byteArrayToString(digest);
	}

	public static final String getFileMD5(String filename) {
		InputStream fis;
		byte[] buffer = new byte[1024];
		int numRead = 0;
		MessageDigest md5;
		try {
			fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			if (CommUtils.DEBUG)
				e.printStackTrace();
			return null;
		}

		try {
			md5 = MessageDigest.getInstance("MD5");
			while ((numRead = fis.read(buffer)) > 0) {
				md5.update(buffer, 0, numRead);
			}
		} catch (NoSuchAlgorithmException e) {
			if (CommUtils.DEBUG)
				e.printStackTrace();
			return null;
		} catch (IOException e) {
			if (CommUtils.DEBUG)
				e.printStackTrace();
			return null;
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				if (CommUtils.DEBUG)
					e.printStackTrace();
			}
		}

		return byteArrayToString(md5.digest());
	}

	private static String byteArrayToString(byte[] b) {
		StringBuffer resultSb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			resultSb.append(byteToHexString(b[i]));
		}
		return resultSb.toString();
	}

	private static String byteToHexString(byte b) {
		int n = b;
		if (n < 0) {
			n = 256 + n;
		}
		int d1 = n / 16;
		int d2 = n % 16;
		return hexDigits[d1] + hexDigits[d2];
	}

	public static boolean isConnected(Context context) {
		if (context == null)
			return false;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnectedOrConnecting();
	}

	public static boolean isWifiConnected(Context context) {
		if (null == context)
			return false;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}

	public static boolean isMobileConnected(Context context) {
		if (null == context)
			return false;
		ConnectivityManager connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	public static boolean isLandscape(Context context) {
		if (context == null)
			throw new IllegalAccessError("Contex is Null");
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		float widthDp = metrics.widthPixels / metrics.density;
		if (widthDp > 480) {
			return true;
		} else {
			return false;
		}
	}

	public static Bitmap makeRoundImage(Bitmap bm, int rx, int ry) {
		if (bm == null) {
			return null;
		}

		int width = bm.getWidth();
		int height = bm.getHeight();
		Bitmap round = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(round);

		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		int minRound = Math.min(width, height) / 3;
		rx = Math.min(rx, minRound);
		ry = Math.min(ry, minRound);

		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(0xff424242);
		canvas.drawRoundRect(new RectF(0, 0, width, height), rx, ry, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bm, 0, 0, paint);
		bm.recycle();

		return round;
	}

	/**
	 * 数据加密
	 * @param src
	 */
	public static byte[] encrypt(byte[] src, byte[] pass) {
		if (null == src)
			return null;
		if (null == pass)
			return src;
		final int srcLen = src.length;
		final int passLen = pass.length;
		byte[] res = new byte[srcLen];
		for (int i = 0; i < srcLen; i += passLen) {
			for (int j = 0; j < passLen && (i + j < srcLen); j++) {
				res[i + j] = (byte) (src[i + j] ^ pass[j]);
			}
		}
		return res;
	}

	/**
	 * 对URL进行加密
	 * @param src
	 * @return
	 */
	public static byte[] encryptUrl(byte[] src) {
		return encrypt(src, URL_KEY);
	}

	/**
	 * 对HASH 进行加密
	 * @return
	 */
	public static byte[] encryptApkHash(byte[] src) {
		return encrypt(src, HASH_KEY);
	}

	/**
	 * 删除文件
	 * @param dir
	 * @param numDays
	 * @return
	 */
	public static int clearCacheFolder(File dir, long numDays) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, numDays);
					}
					if (numDays == 0
							|| (numDays != 0 && child.lastModified() < numDays)) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	public static String getSuffix(String remote) {
		int start = remote.lastIndexOf(".");
		int end = remote.lastIndexOf("?");
		end = (-1 == end || end <= start) ? remote.length() : end;
		String suffix = remote.substring(start, end);

		return suffix;
	}
}
