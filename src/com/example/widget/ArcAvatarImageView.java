package com.example.widget;

import java.util.Map.Entry;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.android_arcavatarimageview.R;
import com.example.cache.ImageLoader;
import com.example.cache.ImageLoaderHandler;
import com.example.utils.BitmapUtils;
import com.example.utils.BitmapWorkerTask.Callback;

/**
 * 圆形头像 继承自WebLimitImageView
 * @author Jack
 */
public class ArcAvatarImageView extends WebLimitImageView {
	
	private boolean mIsLoad = false;
	private boolean mIsLimit = false;
	private final int limit_background;
	private final int limit_background_loading;
	
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ArcAvatarImageView(Context context, AttributeSet attrs) {
		
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WebLimitImageView);
		limit_background = a.getResourceId(R.styleable.WebLimitImageView_limit_background,
				android.R.drawable.ic_dialog_info);
		limit_background_loading = a.getResourceId(
				R.styleable.WebLimitImageView_limit_background_loading,
				android.R.drawable.ic_dialog_alert);
		a.recycle();
	}
	
	@Override
	public void loadWebImage(String url) {
	    mCurrentUrl = url;
        Callback callback = new Callback() {
            
            @Override
            public void setImage(View v, Bitmap img, String url) {
                if (img != null) {
                    // 如果是当前View的url
                    if (!TextUtils.isEmpty(mCurrentUrl) && mCurrentUrl.equalsIgnoreCase(url)) {
                        setImageBitmap(BitmapUtils.getRoundCornerImage(img, 5));
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
	
	private class WebLimitImageViewHandler extends ImageLoaderHandler {
		@Override
		protected boolean handleImageLoaded(String url, Bitmap bitmap) {
			if (url.equalsIgnoreCase(mCurrentUrl)) {
				if (null != bitmap) {
					setImageBitmap(BitmapUtils.getRoundCornerImage(bitmap, 5));
					requestLayout();
					invalidate();
					mIsLoad = true;
					return true;
				}
			}
			return false;
		}
		@Override
		protected boolean handleImageLimited(String url) {
			if (url.equalsIgnoreCase(mCurrentUrl)) {
				mIsLimit = true;
				setImageResource(limit_background);
			}
			return false;
		}
		@Override
		protected boolean handleImagePercent(int percent) {
			return false;
		}
	}
	
	/**
	 * 圆角头像图片显示
	 */
	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(BitmapUtils.toRoundBitmap(bm));	//图片圆形显示
	}
	
	/**
	 * 图片四角带弧度显示
	 * @param bm
	 */
	public void setImageBitmapRoundConer(Bitmap bm) {
		super.setImageBitmap(BitmapUtils.getRoundCornerImage(bm, 5));	//图片四角带弧度显示
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mIsLimit && !mIsLoad && null != mCurrentUrl) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					ImageLoader.start(mCurrentUrl, new WebLimitImageViewHandler());
					setImageResource(limit_background_loading);
					return true;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_OUTSIDE:
					return false;
			}
		}
		return super.onTouchEvent(event);
	}
}