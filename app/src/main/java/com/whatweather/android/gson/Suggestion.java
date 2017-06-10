package com.whatweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by HYX on 2017-6-9.
 */

public class Suggestion
{
    @SerializedName("drsg")
    public Dress dress;

    public Sport sport;

    @SerializedName("trav")
    public Travel travel;

    public class Dress
    {
        @SerializedName("txt")
        public String info;
    }

    public class Sport
    {
        @SerializedName("txt")
        public String info;
    }

    public class Travel
    {
        @SerializedName("txt")
        public String info;
    }
}
