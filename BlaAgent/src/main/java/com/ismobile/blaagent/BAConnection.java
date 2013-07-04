package com.ismobile.blaagent;

import android.*;
import android.R;
import android.os.Environment;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by pbm on 2013-06-25.
 */
public class BAConnection {
    String nodesToRetrieve = "//assignment/start | //assignment/stop | //assignment/uid | //assignment/workorder/booked | //assignment/workorder/title | //assignment/workorder/location/latitude | //assignment/workorder/location/longitude";
    private Vector<Assignment> assignments = new Vector<Assignment>();
    private int nodePerAssignment;
    private String xml = "";
    private String events = "";
    private File getPortfile() {
        File f = new File(new File(Environment.getExternalStorageDirectory(), "BlaAndroid"), "port.txt");
        f.mkdirs();
        return f;
    }
    public BAConnection() {
        calculateNodePerAssignment();
    }

    public void calculateNodePerAssignment() {
        nodePerAssignment = nodesToRetrieve.length() - nodesToRetrieve.replaceAll("\\|", "").length() + 1;
    }
    public void startRetrieval() {
        Log.i("Progress", "Starting retrieval");
        final File p = getPortfile();
        if (! p.exists()) {
            Log.e("Progress","Did not find " + p.getAbsolutePath());
            return;
        }
        int port = -1;
        String sp = "";
        try {
            sp = readFileAsString(p);
            if (sp != null) port = Integer.parseInt(sp);
        } catch (Exception e) {
            Log.e("Progress","File " + p.getAbsolutePath() + " did not contain a number: " + sp);
            return;
        }
        if (port <= 0) {
            Log.e("Progress","Invalid port: " + port);
            return;
        }

        getXml(port, false, false);
    }

    private void copyStream(InputStream is, OutputStream os) throws IOException {
        Log.i("Progress", "Copying stream");
        int i;
        byte b[] = new byte[4096];
        while ((i = is.read(b)) > 0)
            os.write(b, 0, i);
    }

    private String readFileAsString(File file) {
        Log.i("Progress", "Reading file as string");
        FileInputStream ifs = null;
        String ret = "";
        if (! file.exists()) return ret;
        try {
            ifs = new FileInputStream(file);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            copyStream(ifs, stream);
            ret = new String(stream.toByteArray(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (ifs != null) ifs.close(); } catch (Exception e) {}
        }
        return ret;
    }

    private ByteArrayOutputStream getUrlData(String url) {
        HttpURLConnection uc = null;
        InputStream is = null;
        ByteArrayOutputStream ret = null;
        try {
            uc = (HttpURLConnection)(new URL(url)).openConnection();
            HttpURLConnection.setFollowRedirects(true);
            uc.connect();
            is = uc.getInputStream();
            ret = new ByteArrayOutputStream();
            copyStream(is, ret);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (uc != null) uc.disconnect();
                if (is != null) is.close();
            } catch (Exception e) {}
        }
        return ret;
    }

    private void appendText(String msg, boolean NL) {
        xml = xml + msg;
        if(NL) {
            xml = xml +"\n";
        }
    }

    private void appendTextNL(String msg) {
        appendText(msg, true);
    }

    private void getXml(final int port, final boolean incCustomXML, final boolean incReports) {
        Log.i("Progress", "Getting XML");
        xml = "";
        events = "";
        new Thread(new Runnable() {
            public void run() {
                try {
                    String url = "http://127.0.0.1:" + port
                            + "/Micro?sub=exportxml";
                    if (! incCustomXML) url += "&customxml=false";
                    if (! incReports) url += "&reports=false";
                    ByteArrayOutputStream b = getUrlData(url);
                    if (b != null) xml = b.toString("UTF-8");
                    if (xml != null && xml.length() > 0) parseXML();
                    Log.w("xml", " " + xml.charAt(xml.length()-3));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                }
            }

        }).start();
    }

    private Document docFromString(String v) {
        Document doc = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(v));
            doc = db.parse(is);
            Log.i("Progress","WIN");
        } catch (Exception e) {
            Log.i("Progress","Failed");
            Log.e("Progress","ParseXML error: " + e);
            e.printStackTrace();
        }
        return doc;
    }

    private void parseXML() {
        Document doc = docFromString(xml);
        if (doc == null) return;
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp1 = xpf.newXPath();
        NodeList nl;
        try {
            //String title, String uid, boolean booked, String startTime, String stopTime, float latitude, float longitude
            // <title> <uid>  <booked> <start> <stop> <latitude> <longitude>
            Log.i("Progress","Try");
            nl = (NodeList) xp1.evaluate(nodesToRetrieve, doc, XPathConstants.NODESET);
            Log.i("Progress","Created Node list");
            events = "";
            for (int m = 0; m < nl.getLength(); m=m+nodePerAssignment) {

                String startTime = nl.item(m).getTextContent();
                String stopTime = nl.item(m+1).getTextContent();
                String uid = nl.item(m+2).getTextContent();
                boolean booked = Boolean.parseBoolean(nl.item(m + 3).getTextContent());
                String title = nl.item(m+4).getTextContent();
                float latitude = Float.parseFloat(nl.item(m+5).getTextContent());
                float longitude = Float.parseFloat(nl.item(m+6).getTextContent());
                Assignment a = new Assignment(title, uid, booked, startTime, stopTime, latitude, longitude);
                assignments.add(a);
            }
            //sort
            Log.d("events",assignments.firstElement().getTitle());
            Log.d("events",assignments.lastElement().getTitle());
        } catch (XPathExpressionException e) {
            Log.e("Progress","XPath error: " + e);
            e.printStackTrace();
        }
    }
}
