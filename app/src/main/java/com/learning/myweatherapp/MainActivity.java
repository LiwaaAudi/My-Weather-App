package com.learning.myweatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureVTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureVTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRvWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        String provider = locationManager.getBestProvider(criteria, true);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
        Location location = locationManager.getLastKnownLocation(provider);
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        cityName = getCityName(lat, lng);

        getWeatherInfo(cityName);

        searchIV.setOnClickListener(v -> {
            String city = Objects.requireNonNull(cityEdt.getText()).toString();
            if(city.isEmpty()) {
                Toast.makeText(MainActivity.this,"User City Not Found...", Toast.LENGTH_SHORT).show();
            } else {
                cityNameTV.setText(cityName);
                getWeatherInfo(city);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted...", Toast.LENGTH_SHORT).show();
            } else{
                Toast.makeText(this, "Please Provide the Permission...", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude){
        String cityName = "Not found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);

            for(Address adr: addresses){
                if(adr!=null) {
                    String city = adr.getLocality();
                    if(city!=null && !city.equals("")){
                        cityName = city;
                    }else {
                        Log.d("TAG","CITY NOT FOUND");
                        Toast.makeText(this, "User City Not Found...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "http://api.weatherapi.com/v1/forecast.json?key=dd09bac83c634a57859184927211812&q="+ cityName +"&days=1&aqi=yes&alerts=yes\n";

        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            loadingPB.setVisibility(View.GONE);
            homeRL.setVisibility(View.VISIBLE);
            weatherRVModalArrayList.clear();

            try {
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureVTV.setText(temperature +"°c");
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                conditionTV.setText(condition);
                if(isDay==1){
                    Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fiphone7wallpapers.co%2Fsky-bright-shiny-morning-nature%2F&psig=AOvVaw3LhQzQFpo0yoJsqt0OMpK5&ust=1639999431615000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCNDW45Hg7_QCFQAAAAAdAAAAABAD").into(backIV);
                }else {
                    Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.pinterest.com%2Fpin%2F780178335425507443%2F&psig=AOvVaw3HOVdylUH2Vge1QzMkRlf5&ust=1639999474961000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCJj87KPg7_QCFQAAAAAdAAAAABAD").into(backIV);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecastO.getJSONArray("hour");

                for(int i=0; i<hourArray.length(); i++){
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind));
                }
                weatherRVAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(MainActivity.this,"Please Enter a valid city Name...", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);

    }
}