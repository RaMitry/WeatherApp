package ua.uz.test.thinkmobiles.wethapp;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    private static final String GET_CITYWETHER_URL = "http://api.openweathermap.org/data/2.5/weather?q=";
    private static final String GET_WEATHER_ICON_URL = "http://openweathermap.org/img/w/";
    private final static String APP_ID = "49ea5f69576e5aec4e6f893c1bab8aa1";
    public static String LOG_TAG = "my_log";

    String city_name, country, name, sunrise, sunset, description, temperature, humidity, windSpeed;
    Double coordLat, coordLong;
    private EditText selectCityEditText;
    Editable typedText;
    TextView countrySHResult, citySHResult, sunriseSHResult, sunsetSHResult, descriptionSHResult, temperatureSHResult, humiditySHResult, windSpeedSHResult;
    ImageView img;
    Bitmap bitmap;
    ProgressDialog pDialog, pTDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setCustomView(R.layout.search_layout);
        getSupportActionBar().setDisplayShowCustomEnabled(true);

        final EditText searchBox = (EditText) findViewById(R.id.txt_search);
        selectCityEditText = (EditText) findViewById(R.id.txt_search);
        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    typedText = searchBox.getText();
                    checkCity();
                    handled = false;
                }
                return handled;
            }
        });

        countrySHResult = (TextView) findViewById(R.id.countrySHResult);
        citySHResult = (TextView) findViewById(R.id.citySHResult);
        sunriseSHResult = (TextView) findViewById(R.id.sunriseSHResult);
        sunsetSHResult = (TextView) findViewById(R.id.sunsetSHResult);
        descriptionSHResult = (TextView) findViewById(R.id.descriptionSHResult);
        temperatureSHResult = (TextView) findViewById(R.id.temperatureSHResult);
        humiditySHResult = (TextView) findViewById(R.id.humiditySHResult);
        windSpeedSHResult = (TextView) findViewById(R.id.windSpeedSHResult);
        img = (ImageView)findViewById(R.id.img);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkCity() {
        if (selectCityEditText != null) {
            if (selectCityEditText.getText() != null && !selectCityEditText.getText().toString().equals("")) {
                city_name = selectCityEditText.getText().toString();
                new ParseTask().execute();
            }
        }
    }

    private class ParseTask extends AsyncTask<Void, Void, String> implements OnMapReadyCallback {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pTDialog = new ProgressDialog(MainActivity.this);
            pTDialog.setMessage("Loading text data ....");
            pTDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                URL url = new URL(GET_CITYWETHER_URL + city_name + "&APPID=" + APP_ID);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultJson;
        }

        @Override
        protected void onPostExecute(String strJson) {
            super.onPostExecute(strJson);
            JSONObject dataJsonObj = null;

            try {
                dataJsonObj = new JSONObject(strJson);
                JSONObject coord = dataJsonObj.getJSONObject("coord");
                JSONObject sys = dataJsonObj.getJSONObject("sys");
                JSONObject main = dataJsonObj.getJSONObject("main");
                JSONArray weatherAr = dataJsonObj.getJSONArray("weather");
                JSONObject weather = weatherAr.getJSONObject(0);
                JSONObject wind = dataJsonObj.getJSONObject("wind");

                coordLong = coord.getDouble("lon");
                coordLat = coord.getDouble("lat");

                country = sys.getString("country");
                countrySHResult.setText(country);

                name = dataJsonObj.getString("name");
                citySHResult.setText(name);

                Integer tempRise = sys.getInt("sunrise");
                sunrise = Formules.getTime((long) tempRise);
                sunriseSHResult.setText(sunrise);

                Integer tempSet = sys.getInt("sunset");
                sunset = Formules.getTime((long) tempSet);
                sunsetSHResult.setText(sunset);

                description = weather.getString("description");
                descriptionSHResult.setText(description);

                String iconName = weather.getString("icon");

                Double temp = main.getDouble("temp");
                Double tempC = Formules.convertToCelsius(temp);
                temperature = tempC.toString() + " C";
                temperatureSHResult.setText(temperature);

                humidity = main.getString("humidity");
                humidity = humidity + "%";
                humiditySHResult.setText(humidity);
                windSpeed = wind.getString("speed");
                windSpeed = windSpeed + " meter/s";
                windSpeedSHResult.setText(windSpeed);

                new LoadImage().execute(GET_WEATHER_ICON_URL + iconName +".png");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Such City Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
            }

            pTDialog.dismiss();

            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

        @Override
        public void onMapReady(GoogleMap map) {
            LatLng city = new LatLng(coordLat, coordLong);

            map.setMyLocationEnabled(true);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(city, 9));

            map.addMarker(new MarkerOptions()
                    .title(name)
                    .snippet("Weather: " + description + ", " + temperature + " C, HUM:" + humidity + "%, wind:" + windSpeed)
                    .position(city));


        }

        protected class LoadImage extends AsyncTask<String, String, Bitmap> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(MainActivity.this);
                pDialog.setMessage("Loading Image ....");
                pDialog.show();
            }
            protected Bitmap doInBackground(String... args) {
                try {
                    bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return bitmap;
            }
            protected void onPostExecute(Bitmap image) {
                if(image != null){
                    img.setImageBitmap(image);
                    pDialog.dismiss();
                }else{
                    pDialog.dismiss();
                    Toast.makeText(MainActivity.this, "Image Does Not exist or Network Error", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
