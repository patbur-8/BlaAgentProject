package com.ismobile.blaagent;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ats on 2013-07-29.
 */
public class GetDirections {
    private String key = "Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14";
    //http://www.mapquestapi.com/directions/v1/route?key=Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14&from=Lancaster,PA&to=York,PA&callback=renderNarrative
    //http://www.mapquestapi.com/directions/v1/route?key=Fmjtd%7Cluub20012d%2Cbl%3Do5-9ura14&from=40.080,-76.31&to=40.019,-76.73&callback=renderNarrative
    String from = "40.080,-76.31"; //"Lancaster,PA";
    String to = "40.019,-76.73"; //"York,PA";

    public void getJSON(String from, String to) {
        try {
            URL url = new URL("http://www.mapquestapi.com/directions/v1/optimizedroute?key="+key+"&from="+from+"&to="+to+"&callback=renderNarrative");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            //System.out.println("Output from Server .... \n");
            Log.d("XML:","Output from Server ....");
            while ((output = br.readLine()) != null) {
                //System.out.println(output);
                Log.d("XML:",output);
            }

            conn.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

}
