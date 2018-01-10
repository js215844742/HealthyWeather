package cn.jane.healthy.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.jane.healthy.db.City;
import cn.jane.healthy.db.County;
import cn.jane.healthy.db.Provice;
import cn.jane.healthy.gson.Weather;

/**
 * Created by Jane on 2018/1/9.
 */

public class Utility {
    /**
     * 解析省级数据
     * @param response
     * @return
     */
    public static boolean handleProviceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvice = new JSONArray(response);
                for (int i = 0; i < allProvice.length(); i++) {
                    JSONObject proviceObject = allProvice.getJSONObject(i);
                    Provice provice = new Provice();
                    provice.setProviceName(proviceObject.getString("name"));
                    provice.setProviceCode(proviceObject.getInt("id"));
                    provice.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析市级数据
     * @param response
     * @param proviceId
     * @return
     */
    public static boolean handleCityResponse(String response, int proviceId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProviceId(proviceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析县级数据
     * @param response
     * @param cityId
     * @return
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成Weather实体类
     * @param response
     * @return
     */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
