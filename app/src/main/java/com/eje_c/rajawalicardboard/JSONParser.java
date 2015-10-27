package com.eje_c.rajawalicardboard;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONParser {

    public JSONParser() { }

    public static JSONArray getJSONFromUrl(String urlString) throws IOException {
        StringBuilder builder = new StringBuilder();

            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }


        JSONArray jarray = null;

        try {
            jarray = new JSONArray( builder.toString());
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } // return JSON Object

        return jarray;
    }
}
