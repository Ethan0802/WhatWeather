package com.whatweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HYX on 2017-6-9.
 */

public class AQI
{
    @SerializedName("city")
    public AQICity aqiCity;

    public class AQICity
    {
        public String aqi;
        public String pm25;
        public String pm10;
        @SerializedName("qlty")
        public String quality;
    }
}
