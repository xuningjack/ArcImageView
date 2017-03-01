package com.example.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.android_arcavatarimageview.R;
import com.example.support.SystemSupport;


/**
 * 一些通用的方法
 * @author Jack
 */
public class Utils {
    
	/**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The file copy buffer size (30 MB)
     */
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;
    
    protected static SimpleDateFormat mSimpleDateFormat  = new SimpleDateFormat("MM/dd HH:mm");
    protected static SimpleDateFormat mSimpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static final int MEDIA_TYPE_VIDEO_THUMBNAIL = 1; //视频缩略图
	public static final int MEDIA_TYPE_VIDEO = 2; //视频
	public static final int MEDIA_TYPE_ZIP = 3; //视频
    
    public static void chmod(String permission, String path) {
        try {
            String command = "chmod " + permission + " " + path;
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param is
     * @return
     * @throws IOException
     */
    public static String streamToString(InputStream is) {
        return streamToString(is, "UTF-8");
    }
    
    public static String streamToString(InputStream is, String enc) {
        StringBuilder buffer = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, enc), 8192);
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return buffer.toString();
    }
    
    /**
     * @param is
     * @param os
     */
    public static void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            int count = is.read(bytes, 0, buffer_size);
            while (-1 != count) {
                os.write(bytes, 0, count);
                count = is.read(bytes, 0, buffer_size);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static String bundle2String(Bundle bundle) throws UnsupportedEncodingException {
        if (null != bundle) {
            Set<String> keys = bundle.keySet();
            if (null != keys && !keys.isEmpty()) {
                StringBuilder buffer = new StringBuilder();
                for (String key : keys) {
                    String value = bundle.getString(key);
                    buffer.append("&").append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                }
                return buffer.toString();
            }
        }
        
        return "";
    }
    
    /**
     * px(value)=(int) (dip(value) * density + 0.5)
     * 
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * density + 0.5f);
    }
    
    /**
     * dip(value)=(int) (px(value) / density + 0.5)
     * 
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }
    
    public static int dip2DeviceWidthPx(Context context, float dipValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        float widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        return (int) (widthPixels - density * 320 + density * dipValue);
    }
    
    /**
     * @param date
     * @return
     */
    public static String getDisplayTime(Long date) {
        return new SimpleDateFormat("MM/dd HH:mm").format(date);
    }
    
    /**
     * @param date
     * @return
     */
    public static String getDisplayTime2(Long date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }
    
    /**
     * 转换UTC时间
     * 
     * @param utcTime
     * @return
     */
    public static Date getDateByUTC(String utcTime) {
        Date date;
        SimpleDateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss ZZZZ yyyy", Locale.ENGLISH);
        
        try {
            date = formatter.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }
        
        return date;
    }
    
    public static String getDateStringByUTC(String utcTime) {
    	if (TextUtils.isEmpty(utcTime)) return "";
        return mSimpleDateFormat.format(new Date(utcTime));
    }
    
    public static String getDateStringByUTC2(String utcTime) {
        String timeStr = null;
        if (utcTime != null) {
            timeStr = mSimpleDateFormat2.format(new Date(utcTime));
        }
        return timeStr;
    }
    
    public static String getVideoTimeFormatString(int currentPosition, int duration) {
    	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    	StringBuffer resultStr = new StringBuffer();
    	resultStr.append(formatter.format(currentPosition));
    	resultStr.append("/");
    	resultStr.append(formatter.format(duration));
		return resultStr.toString();
    }
    
    public static String getVideoTimeFormatString(int time) {
    	SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
    	StringBuffer resultStr = new StringBuffer();
    	resultStr.append(formatter.format(time));
		return resultStr.toString();
    }
    
    /**
	 * 格式化时间
	 * @param time	单位为毫秒级
	 * @return
	 */
	public static String formatToDoveBoxDate (Context context, String time)
    {
		if (context == null) return "";
        final Date currentData = new Date();
        final Date targetData = new Date(time);
        int intervalSeconds = (int) ((currentData.getTime() - targetData.getTime()) / 1000);
        int intervalMin = intervalSeconds / 60; 
        if(intervalSeconds < 0 || intervalMin == 0) {
        	return context.getString(R.string.just_now);
        }
        StringBuilder sb = new StringBuilder();
        if(intervalMin < 60 && intervalMin > 0) {
        	sb.append(intervalMin).append(context.getString(R.string.minute_before));
        	return sb.toString();
        }
        int intervalHour = intervalMin / 60; 
        if(intervalHour < 24) {
        	sb.append(intervalHour).append(context.getString(R.string.hour_before));
        	return sb.toString();
        }
        int currentPassMin = currentData.getHours() * 60 + currentData.getMinutes();
        int targetPassMin = targetData.getHours() * 60 + targetData.getMinutes();
        int intervalDay = (intervalMin - ( targetPassMin - currentPassMin)) / (24 * 60);
        if(intervalDay < 3) {
        	sb.append(intervalDay).append(context.getString(R.string.day_before));
        	return sb.toString();
        }
		return mSimpleDateFormat2.format(targetData);
    }
    
    public static String getTakephotoFileName() {
    	String randomUUID = UUID.randomUUID().toString();
        return randomUUID + ".jpg";
    }
    
    /**
     * 获取屏幕高度的方法
     * 
     * @param context
     * @return 返回屏幕宽高
     */
    public static int[] getScreenHeightAndWidth(Context context){
        int[] returnIntArray = new int[2];
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        returnIntArray[0] = dm.widthPixels;
        returnIntArray[1] = dm.heightPixels;
        return returnIntArray;
    }
    
    /**
     * 限制图片不要超过1M内存
     * 
     * @param path
     * @return 返回小于1M的bitmap
     */
    public static Bitmap create1MBitmap(String path) {
    	Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        float m = 1024 * 1024;
        opts.inSampleSize = (int) Math.ceil(Math.sqrt(opts.outWidth * opts.outHeight * 4 / m) );
        opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError er) {	
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
		return bitmap;
		
    }
    
    public static Bitmap create1MBitmap(Bitmap bitmap) {
        float m = 1024 * 1024;
        int time = (int) Math.ceil(Math.sqrt(bitmap.getWidth() * bitmap.getHeight() * 4 / m) );
		Bitmap bitMap = null;
		try {
			bitMap = Bitmap.createBitmap(bitmap.getWidth() / time, bitmap.getHeight() / time, Config.ARGB_8888);
		} catch (OutOfMemoryError er) {	
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
		return bitMap;
    }
    
    /**
     * 取小图片
     * 
     * @param path
     * @return 
     */
    public static Bitmap createSmallBitmap(String path) {
    	Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        float m = 100 * 100;
        opts.inSampleSize = (int) Math.ceil(Math.sqrt(opts.outWidth * opts.outHeight * 4 / m) );
        opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError er) {	
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
		return bitmap;
    }
    
    /**
     * 创建小图
     * 
     * @param path
     * @return 
     */
    public static String createSmallImage(String path, String savePath) {
    	Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        if (opts.outWidth > 200 && opts.outHeight > 200) {
			opts.inSampleSize = Math.min(opts.outWidth / 200, opts.outHeight / 200);
		}
        opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError er) {	
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
		
		StringBuilder sb = new StringBuilder();
    	sb.append(savePath);
    	sb.append(System.currentTimeMillis());
    	sb.append("temp.jpg");
		File file = new File(sb.toString());
    	if (file.exists()) {
    		file.delete();
    	}
    	
    	BufferedOutputStream bos = null;
		try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
    	} catch(OutOfMemoryError e) {
    		Log.e(Utils.class.getSimpleName(), e.toString());
		} catch (Exception e) {
    		Log.e(Utils.class.getSimpleName(), e.toString());
    		return "";
    	} finally {
    		if (bos != null) {
    			try {
					bos.flush();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					return "";
				}
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * 创建一个新的文件(降低大于50Kb的图片的质量)用于网络传输 
     * 
     * @param savePath
     * @param bitmap
     * @return 返回新的图片或者为空。为空时表示没有生成新图片
     */
    public static String create50KBFile(String savePath, Bitmap bitmap, boolean isMake) {
//    	int width = bitmap.getWidth();
//		int height = bitmap.getHeight();
//    	if (!isMake && width * height < 1024 * 50) return "";
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(savePath);
    	sb.append(System.currentTimeMillis());
    	sb.append("temp.jpg");
    	File file = new File(sb.toString());
    	if (file.exists()) {
    		file.delete();
    	}
    	
    	BufferedOutputStream bos = null;
		try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
    	} catch(OutOfMemoryError e) {
    		Log.e(Utils.class.getSimpleName(), e.toString());
		} catch (Exception e) {
    		Log.e(Utils.class.getSimpleName(), e.toString());
    		return "";
    	} finally {
    		if (bos != null) {
    			try {
					bos.flush();
					bos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
					return "";
				}
    		}
    	}
    	return sb.toString();
    }
    
    /**
     * 创建一个指定大小的正方形图片
     * @param path
     * @param width
     * @return
     */
    public static Bitmap createBitmap(String path, int width, float orientationDegree) {
		Bitmap bitmap = null;
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		if (opts.outWidth > width && opts.outHeight > width) {
			opts.inSampleSize = Math.min(opts.outWidth / width, opts.outHeight / width);
		}
		opts.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeFile(path, opts);
			int height = opts.outHeight;
			if (opts.outWidth < width) width = opts.outWidth;
			if (height > width) height = width;
			if(orientationDegree != 0) {
				Matrix m = new Matrix();
	            m.setRotate(orientationDegree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
			} else {
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
			}
		} catch (OutOfMemoryError er) {
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
    	return bitmap;
    }
    
    /**
	 * 获取自定义拍摄视频存储路径，缩略图问题待完善
	 * 
	 * @throws IOException
	 */
	public static File getOutputMediaFile(Context context, int type, String pathDir) throws IOException {
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			throw new IOException(context.getString(R.string.no_sdcard_string));
		} else {
			if (SystemSupport.readSDCard() < 3096) {
				throw new IOException(context.getString(R.string.no_space_string));
			}
			;
		}
		File mediaStorageDir = new File(pathDir);
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				return null;
			}
		}
		// 创建媒体文件名
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_VIDEO_THUMBNAIL) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp
					+ ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp
					+ ".mp4");
		} else if (type == MEDIA_TYPE_ZIP)
		{
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp
					+ ".zip");
		}else {
			
			return null;
		}
		return mediaFile;
	}
	
	/**
	 * 复制文件
	 * 
	 * @param oldPath
	 * @param newPath
	 * @throws IOException
	 */
	public static void onCopyPic(String oldPath, String newPath) throws IOException {
		File from = new File(oldPath);
		if (!from.exists() || !from.isFile() || !from.canRead()) {
			return;
		}
		File to = new File(newPath);
		if (!to.getParentFile().exists()) {
			to.getParentFile().mkdirs();
		}
		if (to.exists()) {
			to.delete();
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try {
			fis = new FileInputStream(from);
			fos = new FileOutputStream(to);
			input  = fis.getChannel();
			output = fos.getChannel();
			long size = input.size();
			long pos = 0;
			long count = 0;
			while (pos < size) {
				count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
		} finally {
			closeQuietly(output);
			closeQuietly(fos);
			closeQuietly(input);
			closeQuietly(fis);
		}
		
		if (from.length() != to.length()) {
			throw new IOException("Failed to copy full contents from '" +
					from + "' to '" + to + "'");
		}
		to.setLastModified(from.lastModified());
	}
	
	public static String getCameraPath() {
		String fileDir;
		if (new File("sdcard/Camera").exists()) {
			fileDir = "sdcard/Camera/";
		} else if (new File("sdcard/DCIM/Camera").exists()) {
			fileDir = "sdcard/DCIM/Camera/";
		} else {
			fileDir = "sdcard/DCIM/";
		}
		return fileDir;
	}
	
	private static void closeQuietly(OutputStream output) {
        closeQuietly((Closeable)output);
    }
	
	private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }
	
    /**
     * 取小图片
     * 
     * @param path
     * @return 
     */
    public static Bitmap createChatBitmap(String path) {
    	Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        float m = 150 * 150;
        opts.inSampleSize = (int) Math.ceil(Math.sqrt(opts.outWidth * opts.outHeight * 4 / m) );
        opts.inJustDecodeBounds = false;
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(path, opts);
		} catch (OutOfMemoryError er) {	
			bitmap = null;
			er.printStackTrace();
		} catch (Exception e) {
			bitmap = null;
			e.printStackTrace();
		}
		return bitmap;
    }
	
    public  final static Object objectCopy(Object oldObj) { 
    	Object newObj = null; 
    	try { 
    		ByteArrayOutputStream bo = new ByteArrayOutputStream(); 
    		ObjectOutputStream oo = new ObjectOutputStream(bo); 
    		oo.writeObject(oldObj);//源对象 
    		ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray()); 
    		ObjectInputStream oi= new ObjectInputStream(bi); 
    		newObj = oi.readObject();//目标对象 
    	} catch (IOException e) { 
    		e.printStackTrace(); 
    	}catch (ClassNotFoundException e) { 
    		e.printStackTrace(); 
    	} 
    	return newObj; 
    } 
}
