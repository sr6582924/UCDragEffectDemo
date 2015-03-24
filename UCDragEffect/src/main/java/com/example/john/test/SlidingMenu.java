package com.example.john.test;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by john on 2015/1/20.
 */
public class SlidingMenu extends RelativeLayout{


    public static final int TOUCHMODE_MARGIN = 0;

    /** Constant value for use with setTouchModeAbove(). Allows the SlidingMenu to be opened with a swipe
     * gesture anywhere on the screen
     */
    public static final int TOUCHMODE_FULLSCREEN = 1;

    /** Constant value for use with setTouchModeAbove(). Denies the SlidingMenu to be opened with a swipe
     * gesture
     */
    public static final int TOUCHMODE_NONE = 2;

    public static final int LEFT = 0;

    public static final int RIGHT = 1;

    public static final int LEFT_RIGHT = 2;

    private CustomViewAbove mViewAbove;
    private CustomViewBehind mViewBehind;



    public SlidingMenu(Context context) {
        this(context,null);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutParams behindParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mViewBehind = new CustomViewBehind(context);
        addView(mViewBehind, behindParams);
        LayoutParams aboveParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mViewAbove = new CustomViewAbove(context);
        addView(mViewAbove, aboveParams);
        mViewAbove.setCustomViewBehind(mViewBehind);
        this.setBackgroundColor(Color.parseColor("#3366ff"));
        //mViewBehind.setCustomViewAbove(mViewAbove);
    }

    public void setAboveOffset(int i) {
        mViewAbove.setAboveOffset(i);
    }

    public void setBehindOffset(int i) {
        mViewBehind.setHeightOffset(i);
    }

    public void setMenu(View v) {
        mViewBehind.setContent(v);
    }

    public void setContent(View view) {
        mViewAbove.setContent(view);
    }


}
