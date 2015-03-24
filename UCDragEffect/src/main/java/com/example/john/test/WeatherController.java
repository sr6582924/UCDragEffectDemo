package com.example.john.test;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by john on 2015/3/17.
 */
public class WeatherController{

    private WeatherView weatherView;
    private RelativeLayout weatherDesc_rl;
    private RelativeLayout wind_desc_rl;
    private RelativeLayout temperature_rl;
    private View view;
    private static final String TAG = WeatherController.class.getSimpleName();
    private float mDensity;

    public WeatherController(View weatherView) {

        this.view = weatherView;
        this.weatherView = (WeatherView) view.findViewById(R.id.weatherid);
        this.weatherDesc_rl = (RelativeLayout) view.findViewById(R.id.weather_desc_rl);
        wind_desc_rl = (RelativeLayout) view.findViewById(R.id.wind_desc_rl);
        temperature_rl = (RelativeLayout) view.findViewById(R.id.temperature_rl);
        mDensity = weatherView.getResources().getDisplayMetrics().density;
    }

    public void weatherSmoothScrollTo(boolean isMenuOpen, float currScrollY, float scrollRange) {

        final float firstRange = scrollRange * 0.3f;
        final float secondRange = scrollRange - firstRange;
        final float thirdRange = scrollRange * 0.3f;

        float percent = currScrollY / firstRange;
        percent = Math.max(0, percent);
        percent = Math.min(1, percent);
        Log.d(TAG, "firstpercent:" + percent);
        int distance = (int)(mDensity * 10);
        weatherDesc_rl.scrollTo(weatherDesc_rl.getScrollX(),(int)(distance -  percent * distance) - distance);
        weatherDesc_rl.setAlpha(1 - percent);

        float percentSecond = (currScrollY - firstRange) / secondRange;
        float scalePercent = (currScrollY - firstRange) / secondRange;
        scalePercent = Math.max(0,scalePercent);
        percentSecond = Math.max(0,percentSecond);
        percentSecond = Math.min(1,percentSecond);
        Log.d(TAG, "secondpercent:" + percentSecond);
        weatherView.updateScroll(percentSecond,scalePercent);

        float percentThird = (scrollRange - currScrollY)  / thirdRange;
        percentThird = Math.max(0,percentThird);
        percentThird = Math.min(1,percentThird);
        wind_desc_rl.setAlpha(1.0f - percentThird);
        temperature_rl.setAlpha(1.0f - percentThird);
        temperature_rl.scrollTo(temperature_rl.getScrollX(),(int)( percentThird * distance));
    }


}
