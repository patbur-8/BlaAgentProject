package com.ismobile.blaagent;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by ats on 2013-07-29.
 */
public class GetDirections {
    private String key = "Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14";

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
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } finally {
            is.close();
        }
    }


}
