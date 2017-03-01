package com.example;

import android.app.Application;
import com.example.cache.ImageLoader;
import com.example.support.ConfigWrapper;
import com.example.utils.BitmapWorkerTask;
import com.example.utils.ImageLoaderUtils;
import com.example.utils.Utils;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyApp extends Application {

	public static int screenWidth;
	public static int screenHeight;

	@Override
	public void onCreate() {

		super.onCreate();
		ConfigWrapper.initialize(getApplicationContext());
		ImageLoader.initialize(this);
		ImageLoader.startConnectivityMonitor(this);
		ImageLoader.setThreadPoolSize(10);
		ImageLoaderConfiguration config = ImageLoaderUtils
				.getApplicationOptions(screenWidth, screenHeight,
						getApplicationContext());
		// 配置
		com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);
	}

	public void setScreenWH() {
		int[] screenHeightAndWidth = Utils
				.getScreenHeightAndWidth(getApplicationContext());
		screenWidth = screenHeightAndWidth[0];
		if (screenWidth <= 480) {
			BitmapWorkerTask.getInstance().setMAX_IMAGE_SIZE(false);
		}
		screenHeight = screenHeightAndWidth[1];
	}
}
