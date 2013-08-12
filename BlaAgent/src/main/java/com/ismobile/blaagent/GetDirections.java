package com.ismobile.blaagent;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by ats on 2013-07-29.
 */
public class GetDirections {
    //Cache for directions
    static HashMap<String, Integer> directionsCache = new HashMap<String, Integer>();

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Retrieves a JSON object containing directions from point A to point B.
     *
     * "lat,long"
     * @param from
     * @param to
     */
    public JSONObject getDirectionsJSON(String from, String to) throws IOException, JSONException {
        String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                 from +"&destination=" + to + "&sensor=false";
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

    /**
     * Returns the route duration
     * @param from
     * @param to
     * @return
     */
    public int getRouteDuration(String from, String to) {
        String key = from + "-" + to;

        //Tries to retrieve from cache in order to not ask google for the same directions twice.
        if(directionsCache.containsKey(key)) {
            return directionsCache.get(key);
        }
        try {
            JSONObject directions = getDirectionsJSON(from, to);

            // routesArray contains ALL routes
            JSONArray routesArray = directions.getJSONArray("routes");
            // Grab the first route
            JSONObject route = routesArray.getJSONObject(0);
            // Take all legs from the route
            JSONArray legs = route.getJSONArray("legs");
            // Grab first leg
            JSONObject leg = legs.getJSONObject(0);

            JSONObject durationObject = leg.getJSONObject("duration");
            int duration = durationObject.getInt("value");
            directionsCache.put(key,duration);
            Log.i("GetDirections", "Directions found, duration: " + duration);
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }




}