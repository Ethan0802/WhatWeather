package com.whatweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by HYX on 2017-6-3.
 */

public class County extends DataSupport
{
    private int id;
    private String countyName;
    private int countyCode;
    private int cityId;

    public int getCityId()
    {
        return cityId;
    }

    public void setCityId(int cityId)
    {
        this.cityId = cityId;
    }

    public int getCountyCode()
    {
        return countyCode;
    }

    public void setCountyCode(int countyCode)
    {
        this.countyCode = countyCode;
    }

    public String getCountyName()
    {
        return countyName;
    }

    public void setCountyName(String countyName)
    {
        this.countyName = countyName;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
}
