package com.example.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Bitmap处理辅助类
 * 
 * @author Jack
 */
public class BitmapUtils {
	private static Canvas m_canvas = null;
	private static Paint s_paint_eraser = null;
	private static Paint s_paint_outline = null;
	static {
		m_canvas = new Canvas();
		s_paint_eraser = new Paint();
		s_paint_eraser.setAntiAlias(true);
		s_paint_eraser.setDither(true);
		s_paint_eraser.setColor(0xFFFFFFFF);
		s_paint_eraser.setStrokeWidth(20);
		s_paint_eraser.setStyle(Paint.Style.STROKE);
		s_paint_eraser.setStrokeJoin(Paint.Join.ROUND);
		s_paint_eraser.setStrokeCap(Paint.Cap.ROUND);
		s_paint_eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		s_paint_outline = new Paint();
		s_paint_outline.setColor(Color.WHITE);
		s_paint_outline.setStrokeWidth(20);
	}
	private static Paint s_paint_gray = null;
	static {
		s_paint_gray = new Paint();
		ColorMatrix matrix = new ColorMatrix();
		matrix.setSaturation(0.0f); // 饱和度
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
		s_paint_gray.setColorFilter(filter);
	}

	public static Bitmap toGray(Bitmap image) {
		Bitmap grayscalBitmap = Bitmap.createBitmap(image.getWidth(),
				image.getHeight(), Config.ARGB_4444);
		m_canvas.setBitmap(grayscalBitmap);
		m_canvas.drawBitmap(image, 0, 0, s_paint_gray);
		return grayscalBitmap;
	}

	/**
	 * Returns a new bitmap to be used as the object outline, e.g. to visualize
	 * the drop location. Responsibility for the bitmap is transferred to the
	 * caller.
	 */
	public static Bitmap toOutline(Bitmap image) {
		Bitmap bitmap1 = Bitmap.createBitmap(image.getWidth(),
				image.getHeight(), Bitmap.Config.ARGB_4444);
		m_canvas.setBitmap(bitmap1);
		// m_canvas.save();
		m_canvas.drawBitmap(image, 0, 0, null);
		m_canvas.drawBitmap(image, 0, 0, s_paint_eraser);
		Bitmap bitmap2 = bitmap1.extractAlpha();
		Bitmap bitmap3 = Bitmap.createBitmap(image.getWidth(),
				image.getHeight(), Bitmap.Config.ARGB_4444);
		m_canvas.setBitmap(bitmap3);
		m_canvas.drawBitmap(bitmap2, 0, 0, s_paint_outline);
		// m_canvas.restore();
		bitmap1.recycle();
		bitmap1 = null;
		bitmap2.recycle();
		bitmap2 = null;
		return bitmap3;
	}

	public static Bitmap scale(String file, int size, int density) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, opts);
		Log.d("l99", String.format(
				"opts.outWidth=%d, opts.outHeight=%d, opts.outMimeType=%s",
				opts.outWidth, opts.outHeight, opts.outMimeType));
		if (opts.outWidth <= size && opts.outHeight <= size) {
			opts.inJustDecodeBounds = false;
			return decodeBitmapFromDescriptor(file, opts);
		} else {
			opts.inScaled = true;
			opts.inTargetDensity = density;
			opts.inSampleSize = opts.outWidth / size;
			opts.inJustDecodeBounds = false;
			return decodeBitmapFromDescriptor(file, opts);
		}
	}

	/**
	 * 保存图片到指定的路径
	 * @param bitmap  要保存的bitmap
	 * @param path  保存到的路径
	 * @throws IOException IO异常
	 */
	public static void saveBitmap(Bitmap bitmap, String path)
			throws IOException {
		File file = new File(path);
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream stream = new FileOutputStream(file);
		bitmap.compress(CompressFormat.JPEG, 80, stream);
	}

	/**
	 * 利用Matrix旋转图片
	 * @param bm  需要旋转的图片
	 * @param degree  旋转角度
	 * @return 旋转后的图片
	 */
	public static Bitmap rotate(Bitmap bm, int degree) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
				bm.getHeight(), matrix, true);
		bm.recycle();
		return bitmap;
	}

	/**
	 * 根据已有bitmap重新绘制圆角bitmap
	 * @param bitmap 已有bitmap
	 * @param roundPixels 圆角半径
	 * @return
	 */
	public static Bitmap getRoundCornerImage(Bitmap bitmap, int roundPixels) {
		Bitmap roundConcerImage = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(roundConcerImage);
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawRoundRect(rectF, roundPixels, roundPixels, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, null, rect, paint);
		return roundConcerImage;
	}

	public static Bitmap decodeBitmapFromDescriptor(Context context, String filePath) {
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		bfOptions.inTargetDensity = metrics.densityDpi;
		bfOptions.inScaled = true;
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be  used to recover the Bitmap data
											// after being clear, when it will  be used in the future
		bfOptions.inTempStorage = new byte[12 * 1024];

		File file = new File(filePath);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (fs != null) {
			try {
				return BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						bfOptions);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fs != null) {
					try {
						fs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static Bitmap decodeBitmapFromDescriptor(String filePath, BitmapFactory.Options bfOptions) {
		bfOptions.inScaled = true;
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
										// memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be
											// used to recover the Bitmap data
											// after being clear, when it will
											// be used in the future
		bfOptions.inTempStorage = new byte[12 * 1024];

		File file = new File(filePath);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		if (fs != null) {
			try {
				return BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						bfOptions);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fs != null) {
					try {
						fs.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/**
	 * 转换图片成圆形
	 * @param bitmap 传入需要转换的Bitmap对象
	 * @return 转换后圆形的图片
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;

		if (width <= height) {
			roundPx = width / 2;
			top = 0;
			bottom = width;
			left = 0;
			right = width;
			height = width;
			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;
			float clip = (width - height) / 2;
			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;
			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right,
				(int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top,
				(int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);
		return output;
	}
}
