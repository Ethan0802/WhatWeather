package com.whatweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.whatweather.android.gson.Bing;
import com.whatweather.android.gson.Forecast;
import com.whatweather.android.gson.Weather;
import com.whatweather.android.service.AutoUpdateService;
import com.whatweather.android.util.HttpUtil;
import com.whatweather.android.util.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
{
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private TextView airText;
    private TextView humidityText;

    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView pm10Text;
    private TextView dressText;
    private TextView sportText;
    private TextView travelText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;

    public DrawerLayout drawerLayout;
    private Button navButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //实现背景图片和状态栏融合
        if (Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_weather);

        //初始化各控件
        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_updateTime);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);

        airText = (TextView) findViewById(R.id.weather_air_text);
        humidityText = (TextView) findViewById(R.id.weather_hum_text);

        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        pm10Text = (TextView) findViewById(R.id.pm10_text);
        dressText = (TextView) findViewById(R.id.dress_text);
        sportText = (TextView) findViewById(R.id.sport_text);
        travelText = (TextView) findViewById(R.id.travel_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null)
        {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else
        {
            //无缓存时服务器查询天气
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.VISIBLE);
            requestWeather(mWeatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                requestWeather(mWeatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null)
        {
            //如果有缓存就直接加载这张图片
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else
        {
            myLoadBingPic();
        }

        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId)
    {
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" + weatherId + "&key=8457263873be4fbbad126a465286866b";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        if (weather != null && "ok".equals(weather.status))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        myLoadBingPic();
    }

    /**
     * 处理并展示Weather实体中的数据
     */
    private void showWeatherInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1] + "更新";
        //String updateTime = weather.basic.update.updateTime + "更新";
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        String humidity = weather.now.humidity;

        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        humidityText.setText(humidity + "%");

        forecastLayout.removeAllViews();
        //for (Forecast forecast : weather.forecastList)
        String[] dateStr = {"今天", "明天", "后天"};
        for (int i = 0; i < 3; i++)
        {
            Forecast forecast = weather.forecastList.get(i);
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView scText = (TextView) view.findViewById(R.id.sc_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);

            //dateText.setText(forecast.date);
            dateText.setText(dateStr[i]);
            //白天夜晚天气转换
            String dayInfo = forecast.more.dayInfo;
            String nightInfo = forecast.more.nightInfo;
            if (dayInfo.equals(nightInfo))
                infoText.setText(dayInfo);
            else
                infoText.setText(dayInfo + "转" + nightInfo);
            scText.setText(forecast.wind.sc);//风向
            minText.setText(forecast.temperature.min);
            maxText.setText(forecast.temperature.max + "℃");
            forecastLayout.addView(view);
        }

        if (weather.aqi != null)
        {
            String quality = weather.aqi.aqiCity.quality;
            if (quality.equals("优") || quality.equals("良"))
            {
                String newQuality = "空气" + quality;
                airText.setText(newQuality);//最上方天气概况显示空气质量
            } else
            {
                airText.setText(weather.aqi.aqiCity.quality);
            }

            aqiText.setText(weather.aqi.aqiCity.aqi);
            pm25Text.setText(weather.aqi.aqiCity.pm25);
            pm10Text.setText(weather.aqi.aqiCity.pm10);
        }

        String dress = "穿衣：" + weather.suggestion.dress.info;
        String sport = "运动：" + weather.suggestion.sport.info;
        String travel = "旅行：" + weather.suggestion.travel.info;

        dressText.setText(dress);
        sportText.setText(sport);
        travelText.setText(travel);
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    /**
     * 加载必应每日一图(调用郭霖接口)
     */
//    private void loadBingPic()
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
//                final String bingPic = response.body().string();
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
//                editor.putString("bing_pic", bingPic);
//                editor.apply();
//                runOnUiThread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
//                    }
//                });
//            }
//        });
//    }

    /**
     * 加载必应每日一图(解析必应每日图片JSON数据)
     */
    private void myLoadBingPic()
    {
        final String jsonBingPic = "http://cn.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1";//Bing中文网提供的每日图片的JSON数据Url
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

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                    editor.putString("bing_pic", bingPic);
                    editor.apply();
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        }
                    });
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
