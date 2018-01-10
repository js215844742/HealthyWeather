package cn.jane.healthy.activity.area.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.jane.healthy.R;
import cn.jane.healthy.activity.weather.WeatherActivity;
import cn.jane.healthy.db.City;
import cn.jane.healthy.db.County;
import cn.jane.healthy.db.Provice;
import cn.jane.healthy.util.HttpUtil;
import cn.jane.healthy.util.Utility;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Jane on 2018/1/9.
 */

public class ChooseAreaFragment extends Fragment {
    private static final String URL = "http://guolin.tech/api/china";
    private static final int LEVEL_PROVICE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ImageView backButton;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Provice> proviceList;//省列表
    private List<City> cityList;//市列表
    private List<County> countyList;//显列表

    private Provice selectedProvice;//选中的省份
    private City selectedCity;//选中的城市
    private County selectedCounty;//选中的县/区

    private int currentLevel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.choose_area, container, false);
        titleText  = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVICE){
                    selectedProvice = proviceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCounties();
                }else if (currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id", weatherId);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                } else if (currentLevel == LEVEL_CITY){
                    queryProvices();
                }
            }
        });
        queryProvices();
    }

    /**
     * 加载省份列表，优先数据库查询
     */
    private void queryProvices() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        proviceList = DataSupport.findAll(Provice.class);
        if (proviceList.size()>0){
            dataList.clear();
            for (Provice provice : proviceList){
                dataList.add(provice.getProviceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVICE;
        }else{
            String address = URL;
            queryFromService(address, "provice");
        }
    }

    /**
     * 查询选中省份的所有城市，有限从数据库查询
     */
    private void queryCities() {
        titleText.setText(selectedProvice.getProviceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("proviceId = ?", String.valueOf(selectedProvice.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else{
            int proviceCode = selectedProvice.getProviceCode();
            String address = URL + "/" + proviceCode;
            queryFromService(address, "city");
        }
    }

    /**
     * 查询选中城市中的所有县区，优先从数据库中查询
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityId = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if (countyList.size()>0){
            dataList.clear();
            for (County county : countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvice.getProviceCode();
            int cityCode = selectedCity.getCityCode();
            String address = URL + "/" + provinceCode + "/" + cityCode;
            queryFromService(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器查询省/市/县数据
     * @param address
     * @param type
     */
    private void queryFromService(String address, final String type){
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载"+ (type.equals("provice")?"省份":(type.equals("city")?"城市":"县区")) +"失败", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string().toString();
                Log.i("----responseText----",responseText);
                boolean result = false;
                if (type.equals("provice")){
                    result = Utility.handleProviceResponse(responseText);
                }else if (type.equals("city")){
                    result = Utility.handleCityResponse(responseText, selectedProvice.getId());
                }else if (type.equals("county")){
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if (type.equals("provice")){
                                queryProvices();
                            }else if (type.equals("city")){
                                queryCities();
                            }else if (type.equals("county")){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }else{
            progressDialog.show();
        }
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog!=null){
            progressDialog.dismiss();
        }
    }
}
