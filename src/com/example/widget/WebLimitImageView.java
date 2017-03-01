package com.example.widget;

import java.util.Map.Entry;
import java.util.WeakHashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.example.android_arcavatarimageview.R;
import com.example.utils.BitmapWorkerTask;
import com.example.utils.BitmapWorkerTask.Callback;
import com.example.utils.Utils;



/**
 * 具有二级缓存的、能够限制自动下载的、网络图片ImageView。
 * @author Jack
 */
public class WebLimitImageView extends WebImageView {

	protected boolean mIsLoadSmallImg = false;
	protected final int limit_background;
	protected final int limit_background_loading;
	protected WeakHashMap<OnLoadFinish, String> mListeners;
	
	@Override
	public boolean isInEditMode() {
		return true;
	}

	/**
	 * @param context
	 */
	public WebLimitImageView(Context context) {
		this(context, null);
	}

	/**
	 * @param context
	 * @param attributes
	 */
	public WebLimitImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.WebLimitImageView);
		limit_background = a.getResourceId(
				R.styleable.WebLimitImageView_limit_background,
				android.R.drawable.ic_dialog_info);
		limit_background_loading = a.getResourceId(
				R.styleable.WebLimitImageView_limit_background_loading,
				android.R.drawable.ic_dialog_alert);
		a.recycle();
	}
	
	public void setLoadSmallImg(boolean isLoadSmallImg) {
		mIsLoadSmallImg = isLoadSmallImg;
	}

	/**
	 * 传入的是本地地址，不要传入网络url地址
	 * @param path
	 */
	public void loadLocalImage(String path) {
		if(path == null || path.length() == 0) return;
		Bitmap bitmap = null;
		try {
			if(mIsLoadSmallImg) {
				bitmap = Utils.createSmallBitmap(path);
			} else {
				bitmap = Utils.create1MBitmap(path);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		if(bitmap != null) {
		    setImageDrawable(new BitmapDrawable(getResources(), bitmap));
		}
	}
	
	/**
	 * @param url
	 */
	public void loadWebImage(String url) {
		mCurrentUrl = url;
		Callback callback = new Callback() {
			
			@Override
			public void setImage(View v, Bitmap img, String url) {
				if (img != null) {
					// 如果是当前View的url
					if (!TextUtils.isEmpty(mCurrentUrl) && mCurrentUrl.equalsIgnoreCase(url)) {
						setImageDrawable(new BitmapDrawable(getContext().getResources(), img));
					} else {
						img.recycle();
					}
					if (mListeners != null && !mListeners.isEmpty()) {
						for (Entry<OnLoadFinish, String> entry : mListeners.entrySet()) {
							entry.getKey().onImagLoadFinish();
						}
					}
				} 
			}
			
			@Override
			public boolean isContinue() {
				return true;
			}
			
			@Override
			public Bitmap formatBitmap(Bitmap img) {
				return img;
			}
		};
		loadWebImage(url, callback, true);
	}
	
	/**
	 * @param url
	 * @param position ListView中不同位置用于判断同一张图片
	 * @param callback
	 * @param isCache
	 */
	public void loadWebImage(String url, Callback callback, boolean isCache) {
		BitmapWorkerTask.getInstance().load(this, url, callback, isCache);
	}
	
	public void setOnImageLoadFinish(OnLoadFinish l) {
		if (mListeners == null) mListeners = new WeakHashMap<OnLoadFinish, String>();
		if (!mListeners.containsKey(l)) mListeners.put(l, "");
	}

	public interface OnLoadFinish {
		void onImagLoadFinish();
	}
}
