package com.example.utils;

import java.io.File;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import com.example.android_arcavatarimageview.R;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;


/**
 * 图片加载配置工具类
 * @author Jack
 */
public class ImageLoaderUtils {
	
	public static ImageLoaderConfiguration getApplicationOptions(int width,int height,Context context){
		File cacheDir = StorageUtils.getCacheDirectory(context);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//                .memoryCacheExtraOptions(width,height)
//                .discCacheExtraOptions(width, height, CompressFormat.JPEG, 70, null)
        		/** 长图保存最大高度为屏幕的屏高*5 **/
                .memoryCacheExtraOptions(width, height*5)
//                .discCacheExtraOptions(width, height*5, CompressFormat.JPEG, 75, null)
                .threadPoolSize(5) // default
//                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .memoryCache(new LruMemoryCache(4 * 1024 * 1024))
                .memoryCacheSize(4 * 1024 * 1024)
                .discCache(new UnlimitedDiscCache(cacheDir)) // default
                .discCacheSize(50 * 1024 * 1024)
                .discCacheFileCount(100)
                .writeDebugLogs()
                .build();
        return config;
	}

	public static ImageLoaderConfiguration getMediaLoadOptions(int width,int height,Context context){
	    File cacheDir = StorageUtils.getCacheDirectory(context);
	    ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
	    .memoryCacheExtraOptions(width,height)
	    .discCacheExtraOptions(width, height, CompressFormat.JPEG, 70, null)
	    .threadPoolSize(30) // default
//                .threadPriority(Thread.NORM_PRIORITY - 1) // default
	    .tasksProcessingOrder(QueueProcessingType.FIFO) // default
	    .memoryCache(new LruMemoryCache(4 * 1024 * 1024))
	    .memoryCacheSize(4 * 1024 * 1024)
	    .discCache(new UnlimitedDiscCache(cacheDir)) // default
	    .discCacheSize(50 * 1024 * 1024)
	    .discCacheFileCount(100)
	    .writeDebugLogs()
	    .build();
	    return config;
	}
	
	/**
	 * 默认配置
	 * @return
	 */
	public static DisplayImageOptions getDefaultOptions(){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.resetViewBeforeLoading(true)
		.build();
		return options;
	}
	
	/**
	 * 活动相关
	 */
	public static DisplayImageOptions getActionOptions() {
	    DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true)
	    .cacheOnDisc(true)
	    .resetViewBeforeLoading(true)
//	    .showImageForEmptyUri(R.drawable.default_avatar)
//	    .showImageOnFail(R.drawable.default_avatar)
//	    .showStubImage(R.drawable.default_avatar)
	    .imageScaleType(ImageScaleType.EXACTLY)
	    .build();
	    return options;
	}
	
	public static DisplayImageOptions getMediaOptions() {
	    DisplayImageOptions options = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisc(false)
        .bitmapConfig(Bitmap.Config.RGB_565)
        .build();
        return options;
	}
	
	/**
	 * 时间轴 单篇文章等使用多张图片的加载
	 * @return
	 */
	public static DisplayImageOptions getPhotosItemOptions(){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.build();
		return options;
	}
	

	
	/**
	 * 大学广场、单个大学中图片的加载；
	 * add by wenqiang 09-04
	 * @return
	 */
	public static DisplayImageOptions getUniversityItemOptions(){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
        .cacheOnDisc(true)
        .bitmapConfig(Bitmap.Config.RGB_565)
        
//        .displayer(new FadeInBitmapDisplayer(100))
        
//        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//        .imageScaleType(ImageScaleType.EXACTLY)
		.build();
		return options;
	}
	
	/**
	 * 个人空间中个性封面图片的加载；
	 * add by wenqiang 09-07
	 * @return
	 */
	public static DisplayImageOptions getUserDomainCoverOptions(){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.cacheInMemory(true)
        .cacheOnDisc(true)
        .bitmapConfig(Bitmap.Config.RGB_565)
//        .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
//        .imageScaleType(ImageScaleType.EXACTLY)
		.build();
		return options;
	}

	/**
	 * 默认配置
	 * @return
	 */
	public static DisplayImageOptions getCircleAvatarOptions(){
		DisplayImageOptions options = new DisplayImageOptions.Builder()
//		.showImageForEmptyUri(R.drawable.default_avatar)
//		.cacheInMemory(true)
//		.cacheOnDisc(false)
//		.bitmapConfig(Bitmap.Config.RGB_565)
//		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.build();
		return options;
	}
	
	/**
	 * 活动加载头像
	 * @return
	 */
	public static DisplayImageOptions getAvactorOptions() {
	    DisplayImageOptions options = new DisplayImageOptions.Builder()
	    .cacheInMemory(true)
	    .cacheOnDisc(true)
	    .resetViewBeforeLoading(true)
	    .showImageForEmptyUri(R.drawable.default_round_avatar2)
	    .showImageOnFail(R.drawable.default_round_avatar2)
//	    .showStubImage(R.drawable.default_avatar)
//	    .imageScaleType(ImageScaleType.EXACTLY)
	    .build();
	    return options;
	}

}
