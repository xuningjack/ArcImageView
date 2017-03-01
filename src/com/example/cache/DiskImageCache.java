package com.example.cache;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.example.utils.BitmapUtils;

/**
 * 本地磁盘-图片缓存
 * @author Jack
 */
public class DiskImageCache implements IImageCache {

    private static String mRootDir;
    private static Context mContext;
    
    public static String getDiskCacheDir() {
        return mRootDir;
    }
    
    public DiskImageCache(Context context) {
        Context appContext = context.getApplicationContext();
        mContext = appContext;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // SD-card available
            mRootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + appContext.getPackageName() + "/cache";
        }
        else {
            File internalCacheDir = appContext.getCacheDir();
            // apparently on some configurations this can come back as null
            mRootDir = internalCacheDir.getAbsolutePath();
        }

        File outFile = new File(mRootDir);
        if (outFile.mkdirs()) {
            File nomedia = new File(mRootDir, ".nomedia");
            try {
                nomedia.createNewFile();
            }
            catch (IOException e) {
                Log.w("l99", e);
            }
        }
    }
    
    public static String getLocalPathFromUrl(String url) {
        String filename = CacheHelper.getFileNameFromUrl(url);
        return new StringBuilder(mRootDir).append(File.separator).append(filename).toString();
    }

    @Override
    public synchronized Bitmap getBitmap(String url) {
        Bitmap bitmap = null;
        String path = getLocalPathFromUrl(url);
        /*
        File file = new File(path);
        if (file.exists()) {
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                opts.inTargetDensity = metrics.densityDpi;
                opts.inScaled = true;
                
                opts.inDither = false; // Disable Dithering mode
                opts.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
                opts.inInputShareable = true;
                
                bitmap = BitmapFactory.decodeFile(path, opts);
            } catch (Throwable e) {
                Log.w("l99", e);
            }
            if (null == bitmap) {
                Log.e("l99", String.format("Diskcache decode file failed:(url=%s)", url));
                file.delete();
            }
        }
        else {
            //Log.d("l99", "Disk Cache the file does not exist: " + path);
        }
        return bitmap;*/
        bitmap = BitmapUtils.decodeBitmapFromDescriptor(mContext, path);
        return bitmap;
    }

    @Override
    public synchronized boolean putBitmap(String url, Bitmap bitmap) {
        boolean result = false;
        
        String path = getLocalPathFromUrl(url);
        String suffix = CacheHelper.getSuffix(path);
        
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        if (suffix.equalsIgnoreCase(".png")) {
            format = Bitmap.CompressFormat.PNG;
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(path);
            result = bitmap.compress(format, 100, fos);
        } catch (FileNotFoundException e) {
            Log.w("l99", e);
        }
        
        return result;
    }

    @Override
    public synchronized boolean putBitmap(String url, byte[] imageData) {
        boolean result = false;
        
        String path = getLocalPathFromUrl(url);
        File file = new File(path);
        if (file.exists()) {
            // file.delete();
            result = true;
            Log.e("l99", String.format("Diskcache file already exist: url=%s", url));
        } else {
            try {
                file.createNewFile();
                BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(file), 8192);
                ostream.write(imageData);
                ostream.close();
                
                result = true;
            } catch (FileNotFoundException e) {
                Log.w("l99", e);
            } catch (IOException e) {
                Log.w("l99", e);
            }
        }
        
        return result;
    }

    @Override
    public synchronized void clearImage() {
    }

    @Override
    public synchronized boolean hasImage(String url) {
        String path = DiskImageCache.getLocalPathFromUrl(url);
        return new File(path).exists();
    }

    @Override
    public synchronized boolean removeImage(String url) {
        boolean result = false;
        String path = DiskImageCache.getLocalPathFromUrl(url);
        File file = new File(path);
        if (file.exists()) {
            result = file.delete();
            Log.e("l99", String.format("Delete bad image %s: %s.", url, result));
        }
        return result;
    }
}
