package com.example.cache;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import com.example.support.ConfigWrapper;
import com.example.support.ConnectivitySupport;
import com.example.support.ConnectivitySupport.IConnectivityListener;
import com.example.support.ConnectivitySupport.NetState;


/**
 * 图片加载类，它封装了一个ThreadPoolExecutor，线程池FixedThreadPool。 它为每一个网络图片启动一个线程，去下载。
 * 但当同一张网络图片，被同时load多次时，将对应的handler放入mHandlersCache中，而不是启动多个线程。
 * @author Jack
 */
public class ImageLoader implements Runnable{

	/**
	 * The limit ImageLoader code
	 */
	private static final String KEY_LIMIT = "key_limit_webimageview";

	public static int S_LIMIT;  // 300K
	
//	private boolean isNull=false;

	private static ConnectivitySupport s_ConnectivitySupport;

	private static IConnectivityListener s_ConnectivityListener;

	public static void startConnectivityMonitor(Context context) {
		setLimit(ConfigWrapper.get(KEY_LIMIT, 300));
		s_ConnectivitySupport = ConnectivitySupport.getInstance();
		s_ConnectivityListener = new IConnectivityListener() {

			@Override
			public void onConnectivityChanged(NetState oldstate,
					NetState newstate) {
				Log.d("l99", String.format(
						"onConnectivityChanged(oldstate=%s, newstate=%s)",
						oldstate, newstate));
			}
		};
		s_ConnectivitySupport.registerConnectivityListener(context,
				s_ConnectivityListener);
	}

	public static void stopConnectivityMonitor() {
		s_ConnectivitySupport
				.unregisterConnectivityListener(s_ConnectivityListener);
	}

	public static void setLimit(int limit) {
		S_LIMIT = limit * 1024;

		ConfigWrapper.put(KEY_LIMIT, limit);
		ConfigWrapper.commit();
	}

	/**
	 * The original ImageLoader code
	 */
	public static final int HANDLER_MESSAGE_ID1 = 1;
	public static final int HANDLER_MESSAGE_ID = 0;
	public static final String IMAGE_URL_EXTRA = "imageloader:extra_image_url";
	public static final String IMAGE_LIMIT_EXTRA = "imageloader:extra_image_limit";

	private static final int DEFAULT_RETRY_HANDLER_SLEEP_TIME = 1000;
	private static final int DEFAULT_NUM_RETRIES = 2;

	// the default thread pool size
	private static final int DEFAULT_POOL_SIZE = 6;

	private static ThreadPoolExecutor executor;
	private static MemImageCache mMemCache;
	private static DiskImageCache mDiskCache;
	private static int numRetries = DEFAULT_NUM_RETRIES;

	private static Context mContext;
	
//	private static ImageLoaderHandler mHandler;
	
//	private static DownloadListener myListener=null;

	/**
	 * @param numThreads
	 *            the maximum number of threads that will be started to download
	 *            images in parallel
	 */
	public static void setThreadPoolSize(int numThreads) {
		executor.setMaximumPoolSize(numThreads);
	}

	/**
	 * @param numAttempts
	 *            how often the image loader should retry the image download if
	 *            network connection fails
	 */
	public static void setMaxDownloadAttempts(int numAttempts) {
		ImageLoader.numRetries = numAttempts;
	}

	public static synchronized void initialize(Context context) {
		mContext = context.getApplicationContext();
		if (executor == null) {
			executor = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(DEFAULT_POOL_SIZE);
		}
		if (mMemCache == null) {
			mMemCache = new MemImageCache(10);
		}
		if (mDiskCache == null) {
			mDiskCache = new DiskImageCache(context);
		}
	}

	public static synchronized void clearMemCache() {
		if (mMemCache == null) {
			mMemCache.clearImage();
		}
	}

	private static ConcurrentHashMap<String, List<ImageLoaderHandler>> mHandlersCache = new ConcurrentHashMap<String, List<ImageLoaderHandler>>();
	private final String mImageUrl;
	private final boolean mImageLimit;
	private ImageLoaderHandler mHandler;

	private ImageLoader(ImageLoaderHandler handler, String url, boolean limit) {
		mHandler=handler;
		mImageUrl = url;
		mImageLimit = limit;
	}

	public static void start(String imageUrl, ImageLoaderHandler handler) {
		start(imageUrl, handler, false);
	}

	public static void start(String imageUrl, ImageLoaderHandler handler,
			boolean limit) {
		// Log.d("l99", String.format("start(%s)", imageUrl));
		Bitmap bitmap = mMemCache.getBitmap(imageUrl);
		if (null == bitmap) {
			List<ImageLoaderHandler> handlers = mHandlersCache.get(imageUrl);
			if (null == handlers) {
				handlers = new ArrayList<ImageLoaderHandler>(3);
				// Log.d("l99",
				// String.format("new ArrayList<ImageLoaderHandler>(6):%s",
				// imageUrl));
				handlers.add(handler);
				mHandlersCache.put(imageUrl, handlers);
				executor.execute(new ImageLoader(handler, imageUrl, limit));
			} else {
				handlers.add(handler);
				// Log.d("l99", String.format("handlers.add(handler):%s",
				// imageUrl));
			}
		} else {
			// do not go through message passing, handle directly instead
			handler.handleImageLoaded(imageUrl, bitmap);
		}
	}

	@Override
	public void run() {
		DownLoadResult result = null;
		Bitmap bitmap = mDiskCache.getBitmap(mImageUrl);
		if (null != bitmap) {
			result = new DownLoadResult(bitmap, false);
			mMemCache.putBitmap(mImageUrl, bitmap);
		} else {
			result = downloadImage(mHandler);
		}
		notifyImageLoaded(mImageUrl, result.bitmap, result.limit);
	}

	class DownLoadResult {
		public final Bitmap bitmap;
		public final boolean limit;

		public DownLoadResult(Bitmap bitmap, boolean limit) {
			this.bitmap = bitmap;
			this.limit = limit;
		}
	}

	// TODO: we could probably improve performance by re-using connections
	// instead of closing them
	// after each and every download
	protected DownLoadResult downloadImage(ImageLoaderHandler handler) {
		boolean save_image = true;
		int timesTried = 1;
		Bitmap bitmap = null;
		boolean limit = false;
		while (timesTried <= numRetries) {
			try {
				URL url = new URL(mImageUrl);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();

				int fileSize = getContentLength(connection);
				if (fileSize < 0) {
					Log.e("l99", String.format("downloadImage(fileSize=%d): %s", fileSize,mImageUrl));
				} else {
					if (mImageLimit&& NetState.NET_CONNECT_MOBILE == s_ConnectivitySupport.getNetState() && fileSize > S_LIMIT) {
						limit = true;
						connection.disconnect();
						break;
					} else {
						byte[] imageData = retrieveImageData(handler,connection,fileSize);
						connection.disconnect();
						if (imageData != null) {
							BitmapFactory.Options opts = new BitmapFactory.Options();
							DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
							opts.inTargetDensity = metrics.densityDpi;
							opts.inScaled = true;
							opts.inDither = false; // Disable Dithering mode
							opts.inPurgeable = true; // Tell to gc that whether
														// it needs free memory,
														// the Bitmap can be
														// cleared
							opts.inInputShareable = true;

							bitmap = BitmapFactory.decodeByteArray(imageData,0, imageData.length, opts);
							mMemCache.putBitmap(mImageUrl, bitmap);
							save_image = mDiskCache.putBitmap(mImageUrl,imageData);

							break;
						}
					}
				}
			} catch (Throwable e) {
				Log.w("l99", e);
			} finally {
				SystemClock.sleep(DEFAULT_RETRY_HANDLER_SLEEP_TIME);
				timesTried++;
			}
		}

		if (!save_image) {
			mDiskCache.removeImage(mImageUrl);
		}
		return new DownLoadResult(bitmap, limit);
	}

	public static boolean isCached(String imageUrl) {
		return mDiskCache.hasImage(imageUrl);
	}

	protected int getContentLength(HttpURLConnection connection) {
		// determine the image size and allocate a buffer
		int fileSize = connection.getContentLength();
		return fileSize;
	}

	protected byte[] retrieveImageData(ImageLoaderHandler handler,HttpURLConnection connection,int fileSize) throws IOException {
		byte[] imageData = new byte[fileSize];

		handler.sendMessage(handler.obtainMessage(ImageLoader.HANDLER_MESSAGE_ID1, 0, fileSize));
		// download the file
		BufferedInputStream istream = new BufferedInputStream(connection.getInputStream(), 8192);
		int bytesRead = 0;
		int offset = 0;
		while (bytesRead != -1 && offset < fileSize) {
			bytesRead = istream.read(imageData, offset, fileSize - offset);
			offset += bytesRead;
			
			handler.sendMessage(handler.obtainMessage(ImageLoader.HANDLER_MESSAGE_ID1, offset, fileSize));

		}

		handler.sendMessage(handler.obtainMessage(ImageLoader.HANDLER_MESSAGE_ID1, fileSize, fileSize));
		// clean up
		istream.close();
		return imageData;
	}

	public void notifyImageLoaded(String url, Bitmap bitmap, boolean limit) {
		List<ImageLoaderHandler> handlers = mHandlersCache.remove(url);
		// Log.i("l99", String.format("notifyImageLoaded(%d):limit=%s, %s",
		// handlers.size(), limit, mImageUrl));
		for (ImageLoaderHandler handler : handlers) {
			Message message = Message.obtain();
			message.what = HANDLER_MESSAGE_ID;

			Bundle data = new Bundle();
			data.putString(IMAGE_URL_EXTRA, url);
			data.putBoolean(IMAGE_LIMIT_EXTRA, limit);
			message.setData(data);

			message.obj = bitmap;

			handler.sendMessage(message);
		}
	}
}
