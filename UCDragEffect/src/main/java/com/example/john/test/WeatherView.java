package com.example.john.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by john on 2015/3/17.
 */
public class WeatherView extends View{


    private float mDensity;
    private int mWidth, mHeight;
    private TextPaint mPaint;

    private int mTextScrollX, mTextScrollY;
    private int mWeatherScrollX, mWeatherScrollY;

    private Drawable weather;

    private int margin;
    private int topMargin;
    private int bottomMargin;

    private float textSize;
    private Rect drawableRect = new Rect();
    private int weatherWidth;
    private int weatherHeight;

    private int textWidth;
    private int textHeight;
    private Bitmap textBitmap;

    private String str;

    private Paint paint = new Paint();

    private float mScale = 1.0f;
    private float mCurrentScale = 1.0f;

    public WeatherView(Context context) {
        this(context, null);
    }

    public WeatherView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mDensity = dm.density;

        mPaint = new TextPaint();
        mPaint.setColor(Color.WHITE);
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,45,dm);
        mPaint.setTextSize(textSize);
        mPaint.setAntiAlias(true);

        weather = getResources().getDrawable(R.drawable.sun);
        str = "15";

        paint.setColor(Color.BLACK);

        setWillNotDraw(false);
    }

    public void updateScroll(float translatePercent,float scalePercent) {

        int offset = (int)(10 * mDensity);
        int halfWidth = mWidth/ 2;
        mTextScrollX = (int)((halfWidth-textWidth - margin * 2 - offset) * translatePercent);
        int scrollDistance = halfWidth - margin * 2 - weatherWidth - offset;
        mWeatherScrollX = scrollDistance - (int)(scrollDistance * translatePercent) - scrollDistance;

        mTextScrollY = (int)((40.5f * mDensity) * translatePercent);
        mWeatherScrollY = (int)((40.5f * mDensity) * translatePercent);
        //Log.d("weatherView", "percent / 0.2f:" + percent / 0.2f);
        mScale = mCurrentScale + 0.3f * scalePercent;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth =  MeasureSpec.getSize(widthMeasureSpec);
        //mHeight = MeasureSpec.getSize(widthMeasureSpec);
        margin = (int)(10 * mDensity);
        topMargin = (int)(20 * mDensity);
        bottomMargin = (int)(60 * mDensity);
        mHeight = (int)(55 * mDensity) + topMargin + bottomMargin;

        weatherWidth = (int)(55 * mDensity);
        weatherHeight = (int)(55 * mDensity);

/*        textWidth = (int)mPaint.measureText(str);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        textHeight = (int) (Math.ceil(fm.descent - fm.ascent));*/
        textWidth = (int)(55 * mDensity);
        textHeight = (int)(55 * mDensity);
        textBitmap = Bitmap.createBitmap(textWidth,textHeight, Bitmap.Config.ARGB_4444);

        Canvas textCanvas = new Canvas(textBitmap);
        int textY = (int)(45 * mDensity);
        //textCanvas.drawColor(Color.BLACK);
        textCanvas.drawText(str,  0,   textY + mTextScrollY, mPaint);

        setMeasuredDimension(mWidth, mHeight);
    }

    public void drawTestDebug(Canvas canvas) {
        paint.setColor(Color.BLACK);
        canvas.drawRect(drawableRect,paint);
        paint.setColor(Color.RED);
        canvas.drawRect( margin + mTextScrollX,
                mTextScrollY,
                mTextScrollX + textWidth ,
                mTextScrollY + textHeight, paint);
    }

    public void drawTextPaintDebug(Canvas canvas) {
        Paint textPaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setColor( Color.WHITE);

        // FontMetrics对象
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        String text = "15";



        // 计算每一个坐标
        float baseX = 0;
        float baseY = 100;
        float topY = baseY + fontMetrics.top;
        float ascentY = baseY + fontMetrics.ascent;
        float descentY = baseY + fontMetrics.descent;
        float bottomY = baseY + fontMetrics.bottom;
        float leading = baseY + fontMetrics.leading;

        Log.d("fontMetrics", "fontMetrics.top     is:" + fontMetrics.top);
        Log.d("fontMetrics", "fontMetrics.ascent:" + fontMetrics.ascent);
        Log.d("fontMetrics", "fontMetrics.descent  is:" + fontMetrics.descent);
        Log.d("fontMetrics", "fontMetrics.bottom is:" + fontMetrics.bottom);
        Log.d("fontMetrics", "fontMetrics.leading  is:" + fontMetrics.leading);
        Log.d("fontMetrics", "fontMetrics text width  is:" + (fontMetrics.descent - fontMetrics.ascent));

        Log.d("fontMetrics", "baseX    is:" + 0);
        Log.d("fontMetrics", "baseY    is:" + 100);
        Log.d("fontMetrics", "topY     is:" + topY);
        Log.d("fontMetrics", "ascentY  is:" + ascentY);
        Log.d("fontMetrics", "descentY is:" + descentY);
        Log.d("fontMetrics", "bottomY  is:" + bottomY);
        Log.d("fontMetrics", "leading  is:" + leading);

        // 绘制文本
        canvas.drawText(text, baseX, baseY, textPaint);

        // BaseLine描画
        Paint baseLinePaint = new Paint( Paint.ANTI_ALIAS_FLAG);

        baseLinePaint.setColor( Color.RED);
        canvas.drawLine(0, baseY, canvas.getWidth(), baseY, baseLinePaint);

        // Base描画
        canvas.drawCircle( baseX, baseY, 5, baseLinePaint);

        // TopLine描画
        Paint topLinePaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        topLinePaint.setColor( Color.LTGRAY);
        canvas.drawLine(0, topY, canvas.getWidth(), topY, topLinePaint);

        // AscentLine描画
        Paint ascentLinePaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        ascentLinePaint.setColor( Color.GREEN);
        canvas.drawLine(0, ascentY, canvas.getWidth(), ascentY, ascentLinePaint);

        // DescentLine描画
        Paint descentLinePaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        descentLinePaint.setColor( Color.BLACK);
        canvas.drawLine(0, descentY, canvas.getWidth(), descentY + 5, descentLinePaint);

        // ButtomLine描画
        Paint bottomLinePaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        bottomLinePaint.setColor( Color.MAGENTA);
        canvas.drawLine(0, bottomY, canvas.getWidth(), bottomY, bottomLinePaint);

        float width = textPaint.measureText(text);
        float width1 = (int) FloatMath.ceil((Layout.getDesiredWidth(text, mPaint)));
        Log.d("fontMetrics", "width2 is:" + width1 + "width is:" + width);
        Paint widthPaint = new Paint( Paint.ANTI_ALIAS_FLAG);
        widthPaint.setColor( Color.parseColor("#330033"));
        canvas.drawLine(width, ascentY, width, descentY, widthPaint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.scale(mScale, mScale,
                margin + mTextScrollX  + textWidth / 2,
                topMargin + mTextScrollY + textHeight / 2
        );
        canvas.drawBitmap(textBitmap, mTextScrollX + margin,mTextScrollY + topMargin, mPaint);
        canvas.restore();

        canvas.save();
        int left = mWidth - weatherWidth  + mWeatherScrollX - margin;
        int top = mWeatherScrollY + topMargin;
        int right = mWidth + mWeatherScrollX - margin;
        int bottom = weatherHeight + mWeatherScrollY + topMargin;
        canvas.scale(mScale, mScale, left + weatherWidth / 2, top + weatherHeight / 2);
        drawableRect.set(left, top, right, bottom);
        weather.setBounds(drawableRect);
        weather.draw(canvas);
        canvas.restore();

    }
}
