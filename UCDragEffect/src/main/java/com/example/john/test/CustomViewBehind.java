package com.example.john.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by john on 2015/1/20.
 */
public class CustomViewBehind extends ViewGroup{

    private static final String TAG = "CustomViewBehind";

    private static final int MARGIN_THRESHOLD = 48; // dips
   // private int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;

    private CustomViewAbove mViewAbove;

    private View mContent;
    private View mSecondaryContent;
    private int mMarginThreshold;
    private int mHeightOffset;
    //private CanvasTransformer mTransformer;
    private boolean mChildrenEnabled;

    private int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;

    public CustomViewBehind(Context context) {
        this(context,null);
    }

    public CustomViewBehind(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMarginThreshold = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MARGIN_THRESHOLD,getResources().getDisplayMetrics());
    }

    public void setCustomViewAbove(CustomViewAbove customViewAbove) {
        this.mViewAbove = customViewAbove;
    }

    public void setMarginThreshold(int marginThreshold) {
        mMarginThreshold = marginThreshold;
    }

    public int getMarginThreshold() {
        return mMarginThreshold;
    }

    public View getContent() {
        return mContent;
    }

    public void setChildrenEnabled(boolean enabled) {
        mChildrenEnabled = enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return !mChildrenEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return !mChildrenEnabled;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

    }

    public void setContent(View v) {
        if (mContent != null)
            removeView(mContent);
        mContent = v;
        weatherView = new WeatherController(v);
        addView(mContent);
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    private int mMode;
    private boolean mFadeEnabled;
    private final Paint mFadePaint = new Paint();
    private float mScrollScale;
    private Drawable mShadowDrawable;
    private Drawable mSecondaryShadowDrawable;
    private int mShadowWidth;
    private float mFadeDegree;
    private WeatherController weatherView;

    public int getMenuPage(int page) {
        page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
        if (mMode == SlidingMenu.LEFT && page > 1) {
            return 0;
        } else if (mMode == SlidingMenu.RIGHT && page < 1) {
            return 2;
        } else {
            return page;
        }
    }

    public void setHeightOffset(int heightOffset) {
        mHeightOffset = heightOffset;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
        int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0,width);
        int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, mHeightOffset);
        mContent.measure(contentWidth, contentHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        mContent.layout(0, 0, width, mHeightOffset);
    }

    public void setScrollScale(float scrollScale) {
        mScrollScale = scrollScale;
    }

    public float getScrollScale() {
        return mScrollScale;
    }

    public void setShadowDrawable(Drawable shadow) {
        mShadowDrawable = shadow;
        invalidate();
    }

    public void setSecondaryShadowDrawable(Drawable shadow) {
        mSecondaryShadowDrawable = shadow;
        invalidate();
    }

    public void setShadowWidth(int width) {
        mShadowWidth = width;
        invalidate();
    }

    public void setFadeEnabled(boolean b) {
        mFadeEnabled = b;
    }

    public void setFadeDegree(float degree) {
        if (degree > 1.0f || degree < 0.0f)
            throw new IllegalStateException("The BehindFadeDegree must be between 0.0f and 1.0f");
        mFadeDegree = degree;
    }

    public void weatherScrollTo(boolean isMenuOpen, float currScrollY, float scrollRange) {
        weatherView.weatherSmoothScrollTo(isMenuOpen, currScrollY, scrollRange);
    }

    public void scrollBehindTo(View content, int x, int y) {
        int vis = View.VISIBLE;
        if (mMode == SlidingMenu.LEFT) {
            if (x >= content.getLeft()) vis = View.INVISIBLE;
            scrollTo((int)(x + getBehindHeight() * mScrollScale), y);
        } else if (mMode == SlidingMenu.RIGHT) {
            if (x <= content.getLeft()) vis = View.INVISIBLE;
            scrollTo((int)(getBehindHeight() - getWidth() +
                    (x-getBehindHeight())*mScrollScale), y);
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            mContent.setVisibility(x >= content.getLeft() ? View.INVISIBLE : View.VISIBLE);
            vis = x == 0 ? View.INVISIBLE : View.VISIBLE;
            if (x <= content.getLeft()) {
                scrollTo((int)((x + getBehindHeight())*mScrollScale), y);
            } else {
                scrollTo((int)(getBehindHeight() - getWidth() +
                        (x-getBehindHeight())*mScrollScale), y);
            }
        }
        if (vis == View.INVISIBLE)
            Log.v(TAG, "behind INVISIBLE");
        setVisibility(vis);
    }

    public int getBehindHeight() {
        return mContent.getHeight();
    }

    public int getAbsTopBound() {
        if (mMode == SlidingMenu.LEFT || mMode == SlidingMenu.LEFT_RIGHT) {
            return mContent.getTop() - getBehindHeight();
        } else if (mMode == SlidingMenu.RIGHT) {
            return mContent.getTop();
        }
        return 0;
    }

    public int getAbsBottomBound() {
        if (mMode == SlidingMenu.LEFT) {
            return mContent.getTop();
        } else if (mMode == SlidingMenu.RIGHT || mMode == SlidingMenu.LEFT_RIGHT) {
            return mContent.getTop() + getBehindHeight();
        }
        return 0;
    }

    public int getMenuTop(View content, int page) {
        if (mMode == SlidingMenu.LEFT) {
            switch (page) {
                case 0:
                    return content.getTop() - getBehindHeight();
                case 2:
                    return content.getTop();
            }
        } else if (mMode == SlidingMenu.RIGHT) {
            switch (page) {
                case 0:
                    return content.getTop();
                case 2:
                    return content.getTop() + getBehindHeight();
            }
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            switch (page) {
                case 0:
                    return content.getTop() - getBehindHeight();
                case 2:
                    return content.getTop() + getBehindHeight();
            }
        }
        return content.getTop();
    }

    public boolean marginTouchAllowed(View content, int x) {
        int left = content.getTop();
        int right = content.getBottom();
        if (mMode == SlidingMenu.LEFT) {
            //return (x >= left && x <= mMarginThreshold + left);
            return true;
        } else if (mMode == SlidingMenu.RIGHT) {
            return (x <= right && x >= right - mMarginThreshold);
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return (x >= left && x <= mMarginThreshold + left) ||
                    (x <= right && x >= right - mMarginThreshold);
        }
        return false;
    }

    public boolean menuOpenTouchAllowed(View content, int currPage, float x) {
        switch (mTouchMode) {
            case SlidingMenu.TOUCHMODE_FULLSCREEN:
                return true;
            case SlidingMenu.TOUCHMODE_MARGIN:
                return menuTouchInQuickReturn(content, currPage, x);
        }
        return false;
    }

    public boolean menuTouchInQuickReturn(View content, int currPage, float x) {
        if (mMode == SlidingMenu.LEFT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 0)) {
            return x >= content.getLeft();
        } else if (mMode == SlidingMenu.RIGHT || (mMode == SlidingMenu.LEFT_RIGHT && currPage == 2)) {
            return x <= content.getRight();
        }
        return false;
    }

    public boolean menuClosedSlideAllowed(float dx) {
        if (mMode == SlidingMenu.LEFT) {
            return dx > 0;
        } else if (mMode == SlidingMenu.RIGHT) {
            return dx < 0;
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return true;
        }
        return false;
    }

    public boolean menuOpenSlideAllowed(float dx) {
        if (mMode == SlidingMenu.LEFT) {
            return dx < 0;
        } else if (mMode == SlidingMenu.RIGHT) {
            return dx > 0;
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            return true;
        }
        return false;
    }

    public void drawShadow(View content, Canvas canvas) {
        if (mShadowDrawable == null || mShadowWidth <= 0) return;
        int left = 0;
        if (mMode == SlidingMenu.LEFT) {
            left = content.getLeft() - mShadowWidth;
        } else if (mMode == SlidingMenu.RIGHT) {
            left = content.getLeft();
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            if (mSecondaryShadowDrawable != null) {
                left = content.getRight();
                mSecondaryShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
                mSecondaryShadowDrawable.draw(canvas);
            }
            left = content.getLeft() - mShadowWidth;
        }
        mShadowDrawable.setBounds(left, 0, left + mShadowWidth,getHeight());
        mShadowDrawable.draw(canvas);
    }

    public void drawFade(View content, Canvas canvas, float openPercent) {
        if (!mFadeEnabled) return;
        final int alpha = (int) (mFadeDegree * 255 *Math.abs(1 - openPercent));
        mFadePaint.setColor(Color.argb(alpha, 0,0,0));
        int left = 0;
        int right = 0;
        if (mMode == SlidingMenu.LEFT) {
            left = content.getLeft() - getBehindHeight();
            right = content.getLeft();
        } else if (mMode == SlidingMenu.RIGHT) {
            left = content.getRight();
            right = content.getRight() + getBehindHeight();
        } else if (mMode == SlidingMenu.LEFT_RIGHT) {
            left = content.getLeft() - getBehindHeight();
            right = content.getLeft();
            canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
            left = content.getRight();
            right = content.getRight() + getBehindHeight();
        }
        canvas.drawRect(left, 0, right, getHeight(), mFadePaint);
    }
}
