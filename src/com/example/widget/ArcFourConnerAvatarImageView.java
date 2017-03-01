package com.example.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;



/**
 * 四角圆角背景图ImageView
 * @author Jack  
 * @version 创建时间：2013-9-5  下午2:55:12
 */
public class ArcFourConnerAvatarImageView extends ArcAvatarImageView {

	public ArcFourConnerAvatarImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 圆角头像图片显示
	 */
	public void setImageBitmap(Bitmap bm) {
		setImageBitmapRoundConer(bm);
	}
	
}
