package com.example.cache;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 线程池中的线程下载完成后的回调Handler基类。
 * @author Jack
 */
public abstract class ImageLoaderHandler extends Handler {
    @Override
    public final void handleMessage(Message msg) {
        if (msg.what == ImageLoader.HANDLER_MESSAGE_ID) {
            Bundle data = msg.getData();
            String url = data.getString(ImageLoader.IMAGE_URL_EXTRA);
            if (data.getBoolean(ImageLoader.IMAGE_LIMIT_EXTRA, false)) {
                handleImageLimited(url);
            } else {
                Bitmap bitmap = (Bitmap) msg.obj;
                handleImageLoaded(url, bitmap);
            }
        }
        else if(msg.what==ImageLoader.HANDLER_MESSAGE_ID1){
        	handleImagePercent((int)(((double)msg.arg1 / msg.arg2) * 255));
        }
    }

    protected boolean handleImageLimited(String url) {
        return false;
    }
    
    protected abstract boolean handleImageLoaded(String url, Bitmap bitmap);
    
    protected abstract boolean handleImagePercent(int percent);
}
