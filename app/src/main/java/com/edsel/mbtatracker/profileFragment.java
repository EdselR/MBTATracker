package com.edsel.mbtatracker;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class profileFragment extends Fragment implements fragmentInterface{

    private View rootView;
    public static RecyclerView recyclerView;
    public static RecyclerView.Adapter adapter;
    public List<mbtaStops> mbtaList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        rootView = inflater.inflate(R.layout.profile_fragment_layout, container, false);
        setRetainInstance(true);
        mbtaList = new ArrayList<>();

        recyclerView = (RecyclerView)rootView.findViewById(R.id.stopsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        recyclerView.setHasFixedSize(true);

        new loadDatabase().execute(false);

        for(int i = 0; i < mbtaList.size(); i++){
            new grabETATwo(i).execute();
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(
                rootView.getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                new loadDatabase().execute(true, position);

                Toast.makeText(rootView.getContext(),
                        "Stop Unsuscribed!",
                        Toast.LENGTH_SHORT).show();

                for(int i = 0; i < mbtaList.size(); i++){
                    new grabETATwo(i).execute();
                }
            }
        }));

        return rootView;
    }

    @Override
    public void fragmentBecameVisible() {
        new loadDatabase().execute(false);

        for(int i = 0; i < mbtaList.size(); i++){
            new grabETATwo(i).execute();
        }
    }


    class loadDatabase extends AsyncTask<Object, Void, List<mbtaStops>> {

        @Override
        protected void onPreExecute() {
        super.onPreExecute();
    }

        @Override
        protected List<mbtaStops> doInBackground(Object... params) {

            Boolean delete = (Boolean) params[0];
            mbtaList =
                StopsDatabase.getDatabase(rootView.getContext()).MBTAInterface().getAllItems();

            if(delete) {
                int pos = (int) params[1];
                mbtaStops stop = mbtaList.get(pos);
                StopsDatabase.getDatabase(rootView.getContext()).MBTAInterface().delete(stop);
                mbtaList =
                        StopsDatabase.getDatabase(rootView.getContext()).MBTAInterface().getAllItems();
            }


        return mbtaList;
    }

        @Override
        protected void onPostExecute(List<mbtaStops> stopsList) {

        adapter = new stopAdapter(mbtaList);
        recyclerView.setAdapter(adapter);
        }
    }

    public class grabETATwo extends AsyncTask<String, Integer, String> {

        private String stopID;
        private ArrayList<Long> etaList = null;
        int element;


        public grabETATwo(int element){
            this.element = element;
            this.stopID = mbtaList.get(element).getId();
        }


        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String mbtaJSON = null;

            if(isCancelled()){
                return null;
            }

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
                        Date currentTime = Calendar.getInstance().getTime();

                        Long difference = dateEstimated.getTime() - currentTime.getTime();
                        Long minutes = difference/(60 * 1000) % 60;
                        etaList.add(minutes);

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    if(i == 0) {
                        if (etaList.get(0) == 0) {

                            mbtaList.get(element).setEtaTime("Arriving Now");

                        } else if(etaList.get(0) > 0) {

                            mbtaList.get(element).setEtaTime(etaList.get(0) + " min");

                        }else{
                            mbtaList.get(element).setEtaTime("Not Available");
                        }
                    }else if(i == 1){
                        if (etaList.get(1) == 0) {

                            mbtaList.get(element).setNextTime("Arriving Now");

                        } else if(etaList.get(1) > 0) {

                            mbtaList.get(element).setNextTime(etaList.get(1) + " min");
                        }else{

                            mbtaList.get(element).setNextTime("Not Available");
                        }
                    }
                    i++;
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }

    }

}
