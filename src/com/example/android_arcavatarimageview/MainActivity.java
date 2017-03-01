package com.example.android_arcavatarimageview;

import com.example.utils.ImageLoaderUtils; 
import com.example.widget.ArcAvatarImageView;
import com.example.widget.ArcFourConnerAvatarImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import android.os.Bundle;
import android.app.Activity;

/**
 * @author Jack
 */
public class MainActivity extends Activity {

	private ArcAvatarImageView mAvatar;
	private ArcFourConnerAvatarImageView mFourAvatar;
	private String url = "http://www.baidu.com/img/bdlogo.gif";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAvatar = (ArcAvatarImageView)findViewById(R.id.avatar);
		ImageLoader.getInstance().displayImage(url, mAvatar, ImageLoaderUtils.getDefaultOptions());
		
		mFourAvatar = (ArcFourConnerAvatarImageView)findViewById(R.id.roundconneravatar);
		ImageLoader.getInstance().displayImage(url, mFourAvatar, ImageLoaderUtils.getDefaultOptions());
	}
}
