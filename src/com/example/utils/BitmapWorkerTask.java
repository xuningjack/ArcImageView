package com.example.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageSwitcher;

import com.example.cache.LocalCache;
import com.example.support.MediaFile;


/**
 * 图片加载
 * @author Jack
 */
public class BitmapWorkerTask {
	// 大于16M的图片降低图片清晰度
	private static int MAX_IMAGE_SIZE = 16 * 1024 * 1024;
	public static final int BUFF_SIZE = 1024 * 8;
	private static final int _1M = 1024 * 1024;
	
	private static ExecutorService EXECUTOR = null;
	private static BitmapWorkerTask mInstance;
	private Map<Bitmap, String> mImageCache;
	// 用于判断是否正在加载
	private Map<String, String> mLoadCache;
	
	/**
	 * 如果是小于480的屏宽设置最大图片未6M
	 * @param isBigScreen 是否大于480
	 */
	public void setMAX_IMAGE_SIZE(boolean isBigScreen) {
		if (!isBigScreen) {
			MAX_IMAGE_SIZE = 8 * 1024 * 1024;
		}
	}

	public void clearImageCache(Bitmap bitmap){
		if (bitmap == null) return;
		if (mImageCache != null && mImageCache.size() > 0) {
			mImageCache.remove(bitmap);
		}
	}
	
	public void clearImageCache() {
		if (mImageCache != null && !mImageCache.isEmpty()) {
			mImageCache.clear();
		}
	}

	public void clearLoadCache() {
		if (mLoadCache != null && !mLoadCache.isEmpty()) {
			mLoadCache.clear();
		}
	}

	public static BitmapWorkerTask getInstance() {
		if (mInstance == null) {
			mInstance = new BitmapWorkerTask();
		}
		
		if (EXECUTOR == null) {
			EXECUTOR = Executors.newFixedThreadPool(6);
		}
		return mInstance;
	}

	private BitmapWorkerTask() {}
	
	/*
	 * 加载任务
	 */
	class Task extends Handler implements Runnable {
		int width, height;
		View view = null;
		String url = null;
		boolean isCache = true;
		boolean isProcessLocal = false;
		BitmapWorkerTask.Callback callback = null;

		@Override
		public void run() {
			Bitmap bitmap = null;
			byte[] data = null;
			if (callback.isContinue() && !TextUtils.isEmpty(url)) {
				bitmap = loadLoaclCache(view.getContext(), url, callback, isProcessLocal);
				if (!isProcessLocal && bitmap == null) {
					URL m = null;
					InputStream in = null;
					BufferedInputStream bis = null;
					ByteArrayOutputStream out = null;
					try {
						m = new URL(url);
						in = (InputStream) m.getContent();
						if (!callback.isContinue()) return;
						if (in != null) {
							bis = new BufferedInputStream(in, BUFF_SIZE);
							out = new ByteArrayOutputStream();
							int len = 0;
							byte[] buffer = new byte[1024];
							while ((len = bis.read(buffer)) != -1) {
								out.write(buffer, 0, len);
								out.flush();
							}
							data = out.toByteArray();
							if (!callback.isContinue()) return;
							Options opts = new Options();
					        opts.inJustDecodeBounds = true;
							BitmapFactory.decodeByteArray(data, 0, data.length, opts);
							if (opts.outWidth * opts.outHeight * 4 >= MAX_IMAGE_SIZE) {
								opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
							}
							opts.inJustDecodeBounds = false;
							if (width <= 0 && height <= 0) bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
							else bitmap = processBitMap(data, width, height);
							if (bitmap != null) {
								bitmap = callback.formatBitmap(bitmap);
								saveLoaclCache(view.getContext(), url, data, bitmap);
							}
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (in != null) in.close();
							if (out != null) out.close();
							if (bis != null) bis.close();
							if (mLoadCache != null) mLoadCache.remove(url);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			obtainMessage(513, bitmap).sendToTarget();
		}

		@Override
		public void handleMessage(Message msg) {
			if (isCache && msg.obj != null) {
				if (mImageCache == null) mImageCache = Collections.synchronizedMap(new WeakHashMap<Bitmap, String>());
				mImageCache.put((Bitmap) msg.obj, url);
			}
			callback.setImage(view, (Bitmap) msg.obj, url);// 设置加载完成
		}
	}
	
	/**
	 * 根据参数宽高获取Bitmap
	 * @param data
	 * @param height 
	 * @param width 
	 * @return
	 */
	private Bitmap processBitMap(byte[] data, int width, int height) {
		Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		// Calculate inSampleSize
		options.inSampleSize = (int) Math.ceil(Math.sqrt(options.outWidth * options.outHeight * 4 / _1M ) );
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
	    
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	/**
	 * 执行下一个任务
	 * @param task
	 */
	private void startNextTask(Task task) {
		try {
			EXECUTOR.submit(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 带图片参数的加载图片
	 * @param view
	 * @param url
	 * @param callback
	 * @param width
	 * @param height
	 * @param isCache 是否缓存到内存
	 */
	public void loadWithSize(View view, String url, Callback callback, int width, int height, boolean isCache){
		if (!check(view, url, callback))
			return;
		loadStart(view, url, callback, width, height, isCache, false);
	}

	/**
	 * 使用ImageSwitcher加载图片
	 * @param view
	 * @param url
	 * @param callback
	 * @param isCache 是否缓存到内存
	 */
	public void loadWithImageSwitcher(ImageSwitcher view, String url, Callback callback, boolean isCache){
		if (!check(view, url, callback)) return;
		
		loadStart(view, url, callback, 0, 0, isCache, false);
	}
	
	/**
	 * 加载图片
	 * @param view
	 * @param url
	 * @param callback
	 * @param isCache 是否缓存到内存
	 */
	public void load(View view, String url, BitmapWorkerTask.Callback callback, boolean isCache) {
		if (!check(view, url, callback)) 
			return;

		loadStart(view, url, callback, 0, 0, isCache, false);
	}
	
	/**
	 * @param view
	 * @param url
	 * @param callback
	 * @param isProcessLocal 是否处理本地图片
	 */
	public void loadLocalFileCallback(View view, String url, BitmapWorkerTask.Callback callback, boolean isProcessLocal) {
		if (!check(view, url, callback)) 
			return;
		
		loadStart(view, url, callback, 0, 0, true, isProcessLocal);
	}

	/**
	 * 开始加载
	 * @param view
	 * @param url
	 * @param callback
	 * @param width
	 * @param height
	 * @param isCache 是否缓存到内存
	 * @param isProcessLocal 是否处理本地图片
	 */
	private void loadStart(View view, String url, BitmapWorkerTask.Callback callback, 
			int width, int height, boolean isCache, boolean isProcessLocal) {
		if (isCache && mImageCache != null && !mImageCache.isEmpty()) {
			Iterator<Entry<Bitmap, String>> iterator = mImageCache.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Bitmap, String> entry = iterator.next();
				if (url.equals(entry.getValue())) {
					if (entry.getKey() != null && !entry.getKey().isRecycled()) {
						callback.setImage(view, entry.getKey(), url); // 直接返回
						return;
					} else {
						mImageCache.remove(entry.getKey());
						break;
					}
				}
			}
		}
		if (mLoadCache == null) mLoadCache = Collections.synchronizedMap(new WeakHashMap<String, String>());
		// 如果正在加载返回（还未下载成功）
		if (!isProcessLocal && mLoadCache.containsKey(url)) {
			File file = localFile(view.getContext(), url);
			if (file == null || !file.exists()) {
				return;
			}
		}
		
		mLoadCache.put(url, "");
		startNextTask(createTask(view, url, callback, width, height, isCache, isProcessLocal));
	}

	/**
	 * 创建任务
	 * @param view
	 * @param url
	 * @param callback
	 * @param width
	 * @param height
	 * @param isCache 是否缓存到内存
	 * @param processLocal 是否处理本地图片
	 * @return
	 */
	private Task createTask(View view, String url,
			BitmapWorkerTask.Callback callback, int width, int height, boolean isCache, boolean processLocal) {
		Task task = new Task();
		task.callback = callback;
		task.view = view;
		task.url = url;
		task.width = width;
		task.height = height;
		task.isCache = isCache;
		task.isProcessLocal = processLocal;
		return task;
	}

	/**
	 * 是否通过检查
	 * @param view
	 * @param url
	 * @param callback
	 * @return
	 */
	private boolean check(View view, String url, BitmapWorkerTask.Callback callback) {
		if (view == null) 
			return false;
		if (TextUtils.isEmpty(url))
			return false;
		if (callback == null) 
			return false;
		return true;
	}

	/**
	 * 加载本地缓存
	 * @param ctx
	 * @param url
	 * @param callback
	 * @param isProcessLocal
	 * @return
	 */
	private synchronized Bitmap loadLoaclCache(Context ctx, String url, Callback callback, boolean isProcessLocal) {
		// 是否处理本地图片
		if (!isProcessLocal) {
			File file = localFile(ctx, url);
			if (file != null && file.exists() && file.canRead()) {
				try {
					Options opts = new Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(file.getPath(), opts);
					if (opts.outWidth * opts.outHeight * 4 >= MAX_IMAGE_SIZE) {
						opts.inPreferredConfig = Bitmap.Config.ARGB_4444;
					}
					opts.inJustDecodeBounds = false;
					return BitmapUtils.decodeBitmapFromDescriptor(file.getPath(), opts);
				}  catch (OutOfMemoryError e1) {
				} catch (Exception e) {
				}
			}
		} else {
			return callback.formatBitmap(null);
		}
		return null;
	}

	private File localFile(Context ctx, String url) {
		if (ctx == null || TextUtils.isEmpty(url)) 
			return null;
		File file = new File(LocalCache.IMAGE.getLocalImgPath(ctx, url));
		return file;
	}

	/**
	 * 保存本地缓存
	 * @param context
	 * @param url
	 * @param buffer
	 * @param img 
	 */
	private synchronized void saveLoaclCache(Context ctx, String url, byte[] buffer, Bitmap img) {
		if (buffer == null) 
			return;
		File file = localFile(ctx, url);
		deleteFile(file);
		FileOutputStream fos = null;
		try {
		    if (MediaFile.isGifFileType(url)) {
		        fos = new FileOutputStream(file);
		        BufferedOutputStream bos = new BufferedOutputStream(fos, BUFF_SIZE);
		        if (bos != null) {
		        	bos.write(buffer);
		        	bos.flush();
		        	bos.close();
		        }
		    } else {
	            fos = new FileOutputStream(file);
	            img.compress(Bitmap.CompressFormat.PNG, 100, fos);
		    }
		} catch (Exception e) {
			e.printStackTrace();
			deleteFile(file);
		} finally {
			try {
				if (fos != null) fos.close();
			} catch (IOException e) {
				e.printStackTrace();
				deleteFile(file);
			}
		}
	}

	private void deleteFile(File file) {
		if (file != null && file.exists() && file.canWrite()) {
			file.delete();
		}
	}
	
	/**
	 * 加载回调
	 * @author Mike
	 */
	public interface Callback {
		/**
		 * 格式化图片
		 * @param img
		 * @return img
		 */
		public Bitmap formatBitmap(Bitmap img);

		/**
		 * 使用图片
		 * @param View
		 * @param img
		 */
		public void setImage(View v, Bitmap img, String url);
		
		/**
		 * 当前任务是否继续
		 * @return
		 */
		public boolean isContinue();
	}
}
