package com.whatweather.android.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by HYX on 2017-6-18.
 */

public class Bing
{
    @SerializedName("images")
    public List<Img> imgs;

    public class Img
    {
        public String url;
    }
}
