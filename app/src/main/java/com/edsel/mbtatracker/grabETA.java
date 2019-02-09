package com.edsel.mbtatracker;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Calendar;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class grabETA extends AsyncTask<String, Integer, String> {

    private TextView etaField;
    private TextView nextField;
    private String stopID;
    private ArrayList<Long> etaList = null;

    public grabETA(TextView etaField, TextView nextField, String stopID){
        this.etaField = etaField;
        this.nextField = nextField;
        this.stopID = stopID;
    }


    @Override
    protected String doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String mbtaJSON = null;

        try{
            final String MBTA_URL_API = "https://api-v3.mbta.com/predictions?sort=arrival_time";

            Uri buildURI = Uri.parse(MBTA_URL_API).buildUpon()
                    .appendQueryParameter("filter[stop]", stopID)
                    .appendQueryParameter("api_key", "40b53b82791340e692ec1230aa8d3dc3")
                    .build();

            URL requestURL = new URL(buildURI.toString());

            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            StringBuilder builder = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;

            while((line = reader.readLine()) != null){

                builder.append(line + "\n");
                publishProgress();
            }

            if(builder.length() == 0){
                return null;
            }

            mbtaJSON = builder.toString();

        }catch(IOException e){
            e.printStackTrace();
        }finally{

            if(urlConnection != null){
                urlConnection.disconnect();
            }
            if(reader != null){
                try{
                    reader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }

        return mbtaJSON;
    }

    @Override
    protected void onPostExecute(String s){
        super.onPostExecute(s);
        etaList = new ArrayList<Long>();

        try{

            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = jsonObject.getJSONArray("data");

            int i = 0;
            String arrivalTime = null;

            while(i < jsonArray.length() || i < 3){

                JSONObject stop = jsonArray.getJSONObject(i);
                JSONObject attributes = stop.getJSONObject("attributes");

                try {
                    arrivalTime = attributes.getString("arrival_time");
                    SimpleDateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

                    Date dateEstimated = inFormat.parse(arrivalTime);
                    Long estimatedTime = dateEstimated.getTime();
                    Date currentTime = Calendar.getInstance().getTime();

                    Long difference = dateEstimated.getTime() - currentTime.getTime();
                    Long minutes = difference/(60 * 1000) % 60;
                    etaList.add(minutes);

                }catch(Exception e){
                    e.printStackTrace();
                }

                if(i == 0) {
                    if (etaList.get(0) == 0) {
                        etaField.setText("Arrival Time: Arriving Now");
                    } else if(etaList.get(0) > 0) {
                        etaField.setText("Arrival Time: " + etaList.get(0) + " min");
                    }else{
                        etaField.setText("Arrival Time: Not Available");

                    }
                }else if(i == 1){
                    if (etaList.get(1) == 0) {
                        nextField.setText("Next Arrival: Arriving Now");
                    } else if(etaList.get(1) > 0) {
                        nextField.setText("Next Arrival: " + etaList.get(1) + " min");
                    }
                    else {
                        nextField.setText("Next Arrival: Not Available");
                    }
                }
                i++;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
