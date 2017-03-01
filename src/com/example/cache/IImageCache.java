package com.example.cache;

import android.graphics.Bitmap;

/**
 * 图片缓存的接口
 * @author Jack
 */
public interface IImageCache {

    public Bitmap getBitmap(String url);
    
    public boolean putBitmap(String url, Bitmap bitmap);
    
    public boolean putBitmap(String url, byte[] imageData);
    
    public void clearImage();
    
    public boolean hasImage(String url);
    
    public boolean removeImage(String url);
}
