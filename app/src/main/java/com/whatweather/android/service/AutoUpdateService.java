package com.whatweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.whatweather.android.WeatherActivity;
import com.whatweather.android.gson.Bing;
import com.whatweather.android.gson.Weather;
import com.whatweather.android.util.HttpUtil;
import com.whatweather.android.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service
{
    public AutoUpdateService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        updateWeather();
        myUpdateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hours = 8 * 60 * 60 * 1000;//8小时
        long triggerAtTime = SystemClock.elapsedRealtime() + hours;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气
     */
    private void updateWeather()
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null)
        {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + weatherId + "&key=8457263873be4fbbad126a465286866b";
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);

                    if (weather != null && "ok".equals(weather.status))
                    {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }

    /**
     * 更新必应每日一图(调用郭霖接口)
     */
//    private void updateBingPic()
//    {
//        String requestBingPic = "http://guolin.tech/api/bing_pic";
//        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback()
//        {
//            @Override
//            public void onFailure(Call call, IOException e)
//            {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException
//            {
//                String bingPic = response.body().string();
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
//                editor.putString("bing_pic", bingPic);
//                editor.apply();
//            }
//        });
//    }

    /**
     * 更新必应每日一图(解析必应每日图片JSON数据)
     */
    private void myUpdateBingPic()
    {
        String jsonBingPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";//Bing中文网提供的每日图片的JSON数据Url
        HttpUtil.sendOkHttpRequest(jsonBingPic, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();
                try
                {
                    String bingContent = new JSONObject(responseText).toString();
                    Bing bing = new Gson().fromJson(bingContent, Bing.class);
                    final String bingPic = "http://cn.bing.com" + bing.imgs.get(0).url;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
