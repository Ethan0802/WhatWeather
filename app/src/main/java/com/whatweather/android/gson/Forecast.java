package com.whatweather.android.gson;

import android.view.Window;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HYX on 2017-6-9.
 */

public class Forecast
{
    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature
    {
        public String max;
        public String min;
    }

    public class More
    {
        @SerializedName("txt_d")
        public String dayInfo;
        @SerializedName("txt_n")
        public String nightInfo;
    }

    public Wind wind;

    public class Wind
    {
        public String sc;
    }
}
