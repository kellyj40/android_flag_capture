package com.joekelly.mapsandlocation;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by joekelly on 26/10/2017.
 */

// Requests array of flags from database - for the public game
public class PublicFlagRequest {


    public double[][] requestFlags(){

//        DownloadTask getFlags = new DownloadTask();
//        String result = null;
//        try {
//            result = getFlags.execute("https://dublinbusplanners.com/flagsGame/getAll").get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//
//        try {
//
//            JSONObject jsonObject = new JSONObject(result);
//
//            String info = jsonObject.getString("positions");
//
//            JSONArray flagArr = jsonObject.getJSONArray("positions");
//
//            double[][] arr = new double[flagArr.length()][2];
//
//            for (int i=0; i<flagArr.length(); i++){
//                JSONArray flagArrCoor = flagArr.getJSONArray(i);
//                for (int j=0; j<flagArrCoor.length(); j++){
//                    arr[i][j] = flagArrCoor.getDouble(j);
//                }
//            }
//            return arr;
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }


        double[][] arrFlags ={{53.267685, -6.119695},
                              {53.268288, -6.115382},
                              {53.270912, -6.120339},
                              {53.269751, -6.121927},
                              {53.308807, -6.215731},
                              {53.309126, -6.219639},
                              {53.307005, -6.222331}};
        return arrFlags;

    }

    // Request for flags
    public class DownloadTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;

            HttpURLConnection urlConnection = null;

            try{
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                while (data!=-1){

                    char current = (char) data;
                    result += current;
                    data = reader.read();

                }
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


    }

}
