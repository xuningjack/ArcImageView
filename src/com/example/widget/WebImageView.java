package com.example.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * 可以加载指定url图片的ImageView
 */
public class WebImageView extends ImageView {
    
	protected String mCurrentUrl;
	
    public WebImageView(Context context) {
        this(context, null);
    }
    
    public WebImageView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    /**
     * @return the mImageUrl
     */
    public String getImageUrl() {
        return mCurrentUrl;
    }
}
