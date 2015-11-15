package com.ksk.sunshine;


import android.app.ListFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ForecastFragment extends ListFragment {


    private ArrayAdapter<String> mForecastAdapter;
    private List<String> datalist;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        switch (id) {
            case R.id.action_refresh:
                loadData();
                return true;

            case R.id.map_intent:
                //暗示的intent
                //現在位置の取得を行い、文字列の生成
                //実際はpreferenceからlocationを引っ張ってきてlatlonに代入してvalueをget
                Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void loadData() {
        FetchWeatherTask ft = new FetchWeatherTask();
        //ここでPreferenceの読み込み
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //SharedPreferences.Editor editor = sharedPref.edit();
        //editor.clear().commit();
        String location = sharedPref.getString("location", "94043");
        String units = sharedPref.getString("units","metric");
        ft.execute(location,units);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        //loadData();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //default
        //String[] data = {"Today - Sunny", "Tomorrow - Rainy", "Wednesday - i don't know", "Thursday - Sunny", "Friday - -Let's drink!"};
        //get the Data using API
        loadData();
        //List<String> datalist = new ArrayList<String>(Arrays.asList(data));
        //mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, datalist);
        //setListAdapter(mForecastAdapter);

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        //intentでActivity遷移
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        //Listに保管されているデータを読み込む
        String forecast = mForecastAdapter.getItem(position);
        Log.v("ForecastFragment", forecast);
        intent.putExtra("text", forecast);
        startActivity(intent);

    }

    //コンストラクタ
    public ForecastFragment() {

    }

    //BackGround implementation
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        //裏スレッドが完了した後に呼ばれる
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            //setListAdapter(mForecastAdapter);
            if (strings != null) {
                setListAdapter(mForecastAdapter);
            }
        }

        @Override
        protected String[] doInBackground(String... urls) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            //JSON response as a String
            String forecastJsonStr = null;

            //uribuilderでURLを生成する
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http");
            builder.authority("api.openweathermap.org");
            builder.path("/data/2.5/forecast/daily");

            try {
                //param
                builder.appendQueryParameter("q", urls[0]);
                builder.appendQueryParameter("units",urls[1]);
                builder.appendQueryParameter("cnt", "7");
                builder.appendQueryParameter("APPID", "885cfd6b7ff2244bb4608152e7e54064");
                URL url = new URL(builder.build().toString());

                Log.v("MyFragment", "URL:" + url.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                //ここで最終的にリクエストを送信してデータをGETする
                urlConnection.connect();


                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer stringBuffer = new StringBuffer();
                if (inputStream == null) {
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    //文末に追加が可能
                    stringBuffer.append(line + "\n");
                }
                if (stringBuffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = stringBuffer.toString();

                Log.v("MyFragment", "Forecast JSON String" + forecastJsonStr);

            } catch (IOException e) {
                Log.e("FetchWeatherTask", "Error ", e);

                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("FetchWeatherTask", "Error closing stream", e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                //String[]でリストに挿入するデータをreturn
                String[] data = getWeatherDataFromJson(forecastJsonStr, 7);
                datalist = new ArrayList<String>(Arrays.asList(data));
                mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, datalist);

                return data;
            } catch (JSONException e) {
                Log.e("ForecastFragment", e.getMessage(), e);
                return null;
            }

        }

        private String getReadableDateString(long time) {
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        //getしたJSONから必要なデータを読み込んで表示する形式に変換する(コピペ)
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                //Log.v("MyFragment", "Forecast entry: " + s);
            }
            return resultStrs;

        }

    }

}
