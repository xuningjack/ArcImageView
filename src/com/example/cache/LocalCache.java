package com.example.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.utils.BitmapWorkerTask;
import com.example.utils.CommUtils;

import android.content.Context;

/**
 * 本地缓存
 * @author Jack
 */
public enum LocalCache {
	
	/** 图片缓存 */
	IMAGE("image", 50 * 1024 * 1024);

	private static File DIR = null;
	private String path; // 路径
	private StringBuilder localPathSb;
	private long maxSize; // 最大大小 

	private LocalCache(String name, long maxSize) {
		this.path = name;
		this.maxSize = maxSize;
	}

	public File getPath(Context ctx) {
		File dir = new File(getDir(ctx), path);
		dir.mkdirs();
		return dir;
	}
	
	public String getLocalImgPath(Context ctx, String url) {
		if (localPathSb == null) localPathSb = new StringBuilder();
		if (DIR == null) getDir(ctx);
		synchronized (localPathSb) {
			localPathSb.delete(0, localPathSb.length());
			localPathSb.append(DIR.getAbsolutePath()).append("/")
			.append(CommUtils.getMD5(url));
		}
		return localPathSb.toString();
	}

	/**
	 * 清空缓存
	 * @param ctx
	 */
	public static void clear(Context ctx) {
		List<File> fileList = fileList(getDir(ctx));
		for (File file : fileList) {
			file.delete();
		}
		BitmapWorkerTask.getInstance().clearImageCache();
		BitmapWorkerTask.getInstance().clearLoadCache();
	}

	/**
	 * 检查缓存大小
	 * @param ctx
	 * @param cachePath
	 */
	public static long checkSize(Context ctx) {
		long size = 0;
		for (LocalCache cache : values()) {
			List<File> fileList = fileList(getDir(ctx));

			//文件最后修改时间排序
			Collections.sort(fileList, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					return (int) (f2.lastModified() - f1.lastModified());
				}
			});

			for (File file : fileList) {
				if (size <= cache.maxSize) {
					size += file.length();
				} else file.delete();
			}
		}
		return size;
	}

	/**
	 * 获得目录
	 */
	private static File getDir(Context ctx) {
		if (DIR != null) return DIR;
		DIR = ctx.getCacheDir();
		DIR.mkdirs();
		return DIR;
	}

	/**
	 * 获得文件列表
	 */
	private static List<File> fileList(File dir) {
		List<File> fileList = new ArrayList<File>();
		if (dir == null || !dir.canWrite() || !dir.canRead()) return fileList;
		if (dir.isFile()) {
			fileList.add(dir);
			return fileList;
		}

		String fileArray[] = dir.list();
		for (String name : fileArray) {
			File file = new File(dir, name);
			if (file.isFile()) fileList.add(file);
			else fileList.addAll(fileList(file));//递归
		}

		return fileList;
	}
}
