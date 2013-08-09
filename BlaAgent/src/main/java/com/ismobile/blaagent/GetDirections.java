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
    HashMap<String, Integer> directions = new HashMap<String, Integer>();
    private String key = "Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14";
    //private String key = "AIzaSyDbRT8PQsIIGJriCrD80lF8hnlmPvizAac";
    //http://www.mapquestapi.com/directions/v1/route?key=Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14&from=Lancaster,PA&to=York,PA&callback=renderNarrative
    //http://www.mapquestapi.com/directions/v1/route?key=Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14&from=40.080,-76.31&to=40.019,-76.73&callback=renderNarrative
    String from = "40.080,-76.31"; //"Lancaster,PA";
    String to = "40.019,-76.73"; //"York,PA";


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
         String url = "http://www.mapquestapi.com/directions/v1/optimizedroute?key="+key+"&from="
                +from+"&to="+to;
        //String url = "http://maps.googleapis.com/maps/api/directions/json?origin=" +
                 //from +"&destination=" + to + "&sensor=false";
        Log.d("URLLLL",url);
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }

    public int getRouteDuration(String from, String to) {
        String key = from + "-" + to;
        if(directions.containsKey(key)) {
            return directions.get(key);
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
            directions.put(key,duration);
            return duration;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }




}