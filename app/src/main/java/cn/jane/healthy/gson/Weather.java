package cn.jane.healthy.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Jane on 2018/1/10.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;


}
