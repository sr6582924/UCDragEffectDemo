package com.example.john.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by john on 2015/1/20.
 */
public class CustomViewAbove extends ViewGroup{

    private static final String TAG = "CustomViewAbove";
    private static final boolean DEBUG = false;

    private static final boolean USE_CACHE = false;

    private static final int MAX_SETTLE_DURATION = 600; // ms
    private static final int MIN_DISTANCE_FOR_FLING = 25; // dips

    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

    private View mContent;

    private int mCurItem = 1;
    private Scroller mScroller;

    private boolean mScrollingCacheEnabled;

    private boolean mScrolling;

    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
    private int mTouchSlop;
    private float mInitialMotionY;
    /**
     * Position of the last motion event.
     */
    private float mLastMotionX;
    private float mLastMotionY;
    /**
     * ID of the active pointer. This is used to retain consistency during
     * drags/flings if multiple pointers are used.
     */
    protected int mActivePointerId = INVALID_POINTER;
    /**
     * Sentinel value for no current active pointer.
     * Used by {@link #mActivePointerId}.
     */
    private static final int INVALID_POINTER = -1;

    /**
     * Determines speed during touch scrolling
     */
    protected VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    protected int mMaximumVelocity;
    private int mFlingDistance;

    private CustomViewBehind mViewBehind;
    //	private int mMode;
    private boolean mEnabled = true;

    private int mHeightOffset;
    private int mDragDistance;

    private OnPageChangeListener mOnPageChangeListener;
    private OnPageChangeListener mInternalPageChangeListener;

    //	private OnCloseListener mCloseListener;
    //	private OnOpenListener mOpenListener;
    //private OnClosedListener mClosedListener;
    //private OnOpenedListener mOpenedListener;

    private List<View> mIgnoredViews = new ArrayList<View>();



    //	private int mScrollState = SCROLL_STATE_IDLE;

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public interface OnPageChangeListener {

        /**
         * This method will be invoked when the current page is scrolled, either as part
         * of a programmatically initiated smooth scroll or a user initiated touch scroll.
         *
         * @param position Position index of the first page currently being displayed.
         *                 Page position+1 will be visible if positionOffset is nonzero.
         * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
         * @param positionOffsetPixels Value in pixels indicating the offset from position.
         */
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        /**
         * This method will be invoked when a new page becomes selected. Animation is not
         * necessarily complete.
         *
         * @param position Position index of the new selected page.
         */
        public void onPageSelected(int position);

    }


    public static class SimpleOnPageChangeListener implements OnPageChangeListener {

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // This space for rent
        }

        public void onPageSelected(int position) {
            // This space for rent
        }

        public void onPageScrollStateChanged(int state) {
            // This space for rent
        }

    }



    public CustomViewAbove(Context context) {
        this(context, null);
    }

    public CustomViewAbove(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCustomViewAbove();
    }

    void initCustomViewAbove() {
        setWillNotDraw(false);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setFocusable(true);
        final Context context = getContext();
        mScroller = new Scroller(context, sInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        //最小的拖动距离
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;
        mFlingDistance = (int) (MIN_DISTANCE_FOR_FLING * density);
        mDragDistance = (int)(30 * density);
        setInternalPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                if (mViewBehind != null) {
                    switch (position) {
                        case 0:
                        case 2:
                            mViewBehind.setChildrenEnabled(true);
                            break;
                        case 1:
                            mViewBehind.setChildrenEnabled(false);
                            break;
                    }
                }
            }
        });
    }

    OnPageChangeListener setInternalPageChangeListener(OnPageChangeListener listener) {
        OnPageChangeListener oldListener = mInternalPageChangeListener;
        mInternalPageChangeListener = listener;
        return oldListener;
    }

    public void setAboveOffset(int i) {
        this.mHeightOffset = i;
    }

    public void setCustomViewBehind(CustomViewBehind viewBehind) {
        this.mViewBehind = viewBehind;
    }

    public void setCurrentItem(int item) {
        setCurrentItemInternal(item, true, false);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        setCurrentItemInternal(item, smoothScroll,false);
    }

    private void setCurrentItemInternal(int item, boolean smoothScroll, boolean always) {
        setCurrentItemInternal(item, smoothScroll, always, 0);
    }

    private void setCurrentItemInternal(int item, boolean smoothScroll, boolean always, int velocity) {
        if (!always && mCurItem == item) {
            setScrollingCacheEnabled(false);
            return;
        }

        item = mViewBehind.getMenuPage(item);
        final boolean dispatchSelected = mCurItem != item;
        mCurItem = item;
        final int destY = getDestScrollY(mCurItem);
        if (dispatchSelected && mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(item);
        }
        if (dispatchSelected && mInternalPageChangeListener != null) {
            mInternalPageChangeListener.onPageSelected(item);
        }
        if (smoothScroll) {
            smoothScrollTo(0, destY, velocity);
        } else {
            completeScroll();
            scrollTo(0, destY);
        }

    }

    private void smoothScrollTo(int x, int y, int velocity) {
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        int sx = getScrollX();
        int sy = getScrollY();
        int dx = x - sx;
        int dy = y - sy;
        if (dx == 0 && dy == 0) {
            completeScroll();
            return;
        }

        setScrollingCacheEnabled(true);
        mScrolling = true;

        final int height = getBehindHeight();
        final int halfHeight = height / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dy) / height);
        Log.d(TAG,"distanceRatio:" + distanceRatio);
        final float distance = halfHeight + halfHeight * distanceInfluenceForSnapDuration(distanceRatio);
        Log.d(TAG,"distance:" + distance);
        int duration = 0;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dy) / height;
            duration = MAX_SETTLE_DURATION;
        }
        duration = Math.min(duration, MAX_SETTLE_DURATION);

        mScroller.startScroll(sx, sy, dx, dy, duration);
        invalidate();
    }

    float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f;
        f *= 0.3f * Math.PI / 2.0f;
        float fsin = FloatMath.sin(f);
        return fsin;
    }

    public int getDestScrollY(int page) {
        switch (page) {
            case 0:
            case 2:
                return mViewBehind.getMenuTop(mContent,page);
            case 1:
                return mContent.getTop() - mHeightOffset;
        }
        return 0;
    }


    private int getTopBound() {
        return mViewBehind.getAbsTopBound() - mDragDistance;
    }

    private int getBottomBound() {
        return mViewBehind.getAbsBottomBound() - mHeightOffset;
    }

    public int getBehindHeight() {
        if (mViewBehind == null) {
            return 0;
        } else {
            return mViewBehind.getBehindHeight();
        }
    }

    public void setContent(View v) {
        if (mContent != null)
            this.removeView(mContent);
        mContent = v;
        addView(mContent);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mScrollY = y;
        //Log.d(TAG, "getPercentOpen:" + getPercentOpen());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw) {
            // [ChrisJ] - This fixes the onConfiguration change for orientation issue..
            // maybe worth having a look why the recomputeScroll pos is screwing
            // up?
            //滚动完成
            completeScroll();
            scrollTo(getScrollX(), getDestScrollY(mCurItem));
        }
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();

                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                }

                invalidate();
                return;
            }
        }
        completeScroll();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        mContent.layout(0, 0, width, height);
    }

    public boolean isMenuOpen() {
        return mCurItem == 0 || mCurItem == 2;
    }

    private void completeScroll() {
        boolean needPopulate = mScrolling;
        if (needPopulate) {
            setScrollingCacheEnabled(false);
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
        }
        mScrolling = false;
    }

    private boolean isInIgnoredView(MotionEvent ev) {
        Rect rect = new Rect();
        for (View v : mIgnoredViews) {
            v.getHitRect(rect);
            if (rect.contains((int)ev.getX(), (int)ev.getY())) return true;
        }
        return false;
    }

    private float mScrollY = 0.0f;

    protected int mTouchMode = SlidingMenu.TOUCHMODE_MARGIN;

    public void setTouchMode(int i) {
        mTouchMode = i;
    }

    public int getTouchMode() {
        return mTouchMode;
    }

    private boolean thisTouchAllowed(MotionEvent ev) {
        int y = (int) (ev.getY() + mScrollY);
        if (isMenuOpen()) {
            return mViewBehind.menuOpenTouchAllowed(mContent, mCurItem, y);
        } else {
            switch (mTouchMode) {
                case SlidingMenu.TOUCHMODE_FULLSCREEN:
                    return !isInIgnoredView(ev);
                case SlidingMenu.TOUCHMODE_NONE:
                    return false;
                case SlidingMenu.TOUCHMODE_MARGIN:
                    return mViewBehind.marginTouchAllowed(mContent, y);
            }
        }
        return false;
    }

    private boolean thisSlideAllowed(float dy) {
        boolean allowed = false;
        if (isMenuOpen()) {
            allowed = mViewBehind.menuOpenSlideAllowed(dy);
        } else {
            allowed = mViewBehind.menuClosedSlideAllowed(dy);
        }
        return allowed;
    }

    private int getPointerIndex(MotionEvent ev, int id) {
        int activePointerIndex = MotionEventCompat.findPointerIndex(ev,id);
        if (activePointerIndex == -1)
            mActivePointerId = INVALID_POINTER;
        return activePointerIndex;
    }

    private boolean mQuickReturn = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!mEnabled)
            return false;

        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;

        if (DEBUG)
            if (action == MotionEvent.ACTION_DOWN)
                Log.v(TAG, "Received ACTION_DOWN");

        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP
                || (action != MotionEvent.ACTION_DOWN && mIsUnableToDrag)) {
            endDrag();
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                determineDrag(ev);
                break;
            case MotionEvent.ACTION_DOWN:
                int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                if (mActivePointerId == INVALID_POINTER)
                    break;
                //mLastMotionX = mInitialMotionX = MotionEventCompat.getX(ev,index);
                //mLastMotionY = MotionEventCompat.getY(ev, index);

                mLastMotionX = MotionEventCompat.getX(ev, index);
                mLastMotionY = mInitialMotionY = MotionEventCompat.getY(ev, index);
                if (thisTouchAllowed(ev)) {
                    mIsBeingDragged = false;
                    mIsUnableToDrag = false;
                    if (isMenuOpen() && mViewBehind.menuTouchInQuickReturn(mContent, mCurItem, ev.getY() + mScrollY)) {
                        mQuickReturn = true;
                    }
                } else {
                    mIsUnableToDrag = true;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        if (!mIsBeingDragged) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
        }
        return mIsBeingDragged || mQuickReturn;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!mEnabled)
            return false;
        if (!mIsBeingDragged && !thisTouchAllowed(ev))
            return false;

        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                completeScroll();
                int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                mLastMotionY = mInitialMotionY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsBeingDragged) {
                    determineDrag(ev);
                    if (mIsUnableToDrag)
                        return false;
                }
                if (mIsBeingDragged) {
                    final int activePointerIndex = getPointerIndex(ev, mActivePointerId);
                    if (mActivePointerId == INVALID_POINTER)
                        break;
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    final float deltaY = mLastMotionY - y;
                    mLastMotionY = y;
                    float oldScrollY = getScrollY();
                    float scrollY = oldScrollY + deltaY;

                    final float topBound = getTopBound();
                    final float bottomBound = getBottomBound();
                    if (scrollY < topBound) {
                        scrollY = topBound;
                    } else if (scrollY > bottomBound) {
                        scrollY = bottomBound;
                    }

                    mLastMotionY += scrollY - (int) scrollY;
                    //scrollTo((int) scrollY, getScrollY());
                    scrollTo(getScrollX(),(int) scrollY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(
                            velocityTracker, mActivePointerId);
                    final int scrollY = getScrollY();
                    final float pageOffset = (float) (scrollY - getDestScrollY(mCurItem)) / getBehindHeight();
                    final int activePointerIndex = getPointerIndex(ev, mActivePointerId);
                    if (mActivePointerId != INVALID_POINTER) {
                        final float y = MotionEventCompat.getY(ev, activePointerIndex);
                        final int totalDelta = (int) (y - mInitialMotionY);
                        int nextPage = determineTargetPage(pageOffset, initialVelocity, totalDelta);
                        setCurrentItemInternal(nextPage, true, true, initialVelocity);
                    } else {
                        setCurrentItemInternal(mCurItem, true, true, initialVelocity);
                    }
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                } else if (mQuickReturn && mViewBehind.menuTouchInQuickReturn(mContent, mCurItem, ev.getX() + mScrollY)) {
                    setCurrentItem(1);
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged) {
                    setCurrentItemInternal(mCurItem, true, true);
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int indexx = MotionEventCompat.getActionIndex(ev);
                mLastMotionY = MotionEventCompat.getY(ev, indexx);
                mActivePointerId = MotionEventCompat.getPointerId(ev, indexx);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                int pointerIndex = getPointerIndex(ev, mActivePointerId);
                if (mActivePointerId == INVALID_POINTER)
                    break;
                mLastMotionY = MotionEventCompat.getY(ev, pointerIndex);
                break;
        }
        return true;
    }

    private void determineDrag(MotionEvent ev) {
        final int activePointerId = mActivePointerId;
        final int pointerIndex = getPointerIndex(ev,activePointerId);
        if (activePointerId == INVALID_POINTER || pointerIndex == INVALID_POINTER)
            return;
        final float x = MotionEventCompat.getX(ev, pointerIndex);
        final float dx = x - mLastMotionX;
        final float xDiff = Math.abs(dx);
        final float y = MotionEventCompat.getY(ev,pointerIndex);
        final float dy = y - mLastMotionY;
        final float yDiff = Math.abs(dy);
        if (yDiff > (isMenuOpen() ? mTouchSlop / 2 : mTouchSlop) && yDiff > xDiff) {
            startDrag();
            mLastMotionX = x;
            mLastMotionY = y;
            setScrollingCacheEnabled(true);
        } else if (yDiff > mTouchSlop) {
            mIsUnableToDrag = true;
        }

    }

    private int determineTargetPage(float pageOffset, int velocity, int deltaX) {
        int targetPage = mCurItem;
        if (Math.abs(deltaX) > mFlingDistance && Math.abs(velocity) > mMinimumVelocity) {
            //Log.d(TAG, "determineTargetPage velocity" + velocity + "deltaX:" + deltaX);
            if (velocity > 0 && deltaX > 0 && !isMenuOpen()) {
                targetPage -= 1;
            } else if (velocity < 0 && deltaX < 0 && isMenuOpen()) {
                targetPage += 1;
            }
        } else {
            targetPage = (int) Math.round(mCurItem + pageOffset);
        }
        return targetPage;
    }

    protected float getPercentOpen() {
        //当前位置/总大小
        return Math.abs(mScrollY + mHeightOffset - mContent.getTop()) / (getBehindHeight() - mHeightOffset);
    }

    public float getCurrentScrollY() {
        return Math.abs(mScrollY + mHeightOffset - mContent.getTop());
    }

    // 滚动范围
    public float getScrollRange() {
        return (getBehindHeight() - mHeightOffset);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        mViewBehind.drawShadow(mContent, canvas);
        mViewBehind.drawFade(mContent, canvas, getPercentOpen());
        //mViewBehind.weatherScrollTo(fa);
        mViewBehind.weatherScrollTo(isMenuOpen(), getCurrentScrollY(),getScrollRange());
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        if (DEBUG) Log.v(TAG, "onSecondaryPointerUp called");
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = MotionEventCompat.getY(ev,newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void startDrag() {
        mIsBeingDragged = true;
        mQuickReturn = false;
    }


    private void endDrag() {
        mQuickReturn = false;
        mIsBeingDragged = false;
        mIsUnableToDrag = false;
        mActivePointerId = INVALID_POINTER;

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void setScrollingCacheEnabled(boolean enabled) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled;
            if (USE_CACHE) {
                final int size = getChildCount();
                for (int i = 0; i < size; ++i) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() != GONE) {
                        child.setDrawingCacheEnabled(enabled);
                    }
                }
            }
        }
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            final int scrollX = v.getScrollX();
            final int scrollY = v.getScrollY();
            final int count = group.getChildCount();

            for (int i = count - 1; i >= 0; i--) {
                final View child = group.getChildAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight() &&
                        y + scrollY >= child.getTop() && y + scrollY < child.getBottom() &&
                        canScroll(child, true, dx, x + scrollX - child.getLeft(),y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && ViewCompat.canScrollHorizontally(v, -dx);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }
}
