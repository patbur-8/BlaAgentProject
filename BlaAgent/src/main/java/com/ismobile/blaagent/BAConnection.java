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
    private String xml = "";
    private String events = "";
    private File getPortfile() {
        File f = new File(new File(Environment.getExternalStorageDirectory(), "BlaAndroid"), "port.txt");
        f.mkdirs();
        return f;
    }
    public BAConnection() {

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
        Log.i("Progress", "Byte array stream");
        HttpURLConnection uc = null;
        InputStream is = null;
        ByteArrayOutputStream ret = null;
        try {
            uc = (HttpURLConnection)(new URL(url)).openConnection();
            Log.i("ByteArrayOutputStream", "uc = (HttpURLConnection)(new URL(url)).openConnection();");
            HttpURLConnection.setFollowRedirects(true);
            Log.i("ByteArrayOutputStream", "HttpURLConnection.setFollowRedirects(true);");
            uc.connect();
            Log.i("ByteArrayOutputStream", "uc.connect();");
            is = uc.getInputStream();
            Log.i("ByteArrayOutputStream", "is = uc.getInputStream();");
            ret = new ByteArrayOutputStream();
            Log.i("ByteArrayOutputStream", "ret = new ByteArrayOutputStream();");
            copyStream(is, ret);
            Log.i("ByteArrayOutputStream", "copyStream(is, ret);");
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
        Log.i("Progress", "Port: " + port);
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
        Log.i("Progress", "Doc from String");
        Log.i("Progress", ""+v.length());
        Log.i("Progress", ""+xml.length());
        Document doc = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            Log.i("Progress","Trying 1...");
            DocumentBuilder db = dbf.newDocumentBuilder();
            Log.i("Progress","Trying 2...");
            InputSource is = new InputSource();
            Log.i("Progress","Trying 3...");
            is.setCharacterStream(new StringReader(v));
            Log.i("Progress","Trying 4...");
            Log.d("XML",xml);
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
        Log.i("Progress","här?1");
        Log.i("Progress", "sanning? " + (doc == null));
        if (doc == null) return;
        Log.i("Progress","här?");
        XPathFactory xpf = XPathFactory.newInstance();
        XPath xp1 = xpf.newXPath();
        NodeList nl;
        try {
            // <start> <stop> <booked> <title> <location>
            Log.i("Progress","Try");
            String nodesToRetrieve = "//assignment/uid | //assignment/start | //assignment/stop | //assignment/workorder/booked | //assignment/workorder/title | //assignment/workorder/location/latitude | //assignment/workorder/location/longitude";
            nl = (NodeList) xp1.evaluate(nodesToRetrieve, doc, XPathConstants.NODESET);
            Log.i("Progress","Created Node list");
            events = "";
            for (int m = 0; m < nl.getLength(); m++) {
                Node trnode = nl.item(m);
                events = events + trnode.getTextContent() + "\n";
            }
            Log.d("events",events);
        } catch (XPathExpressionException e) {
            Log.e("Progress","XPath error: " + e);
            e.printStackTrace();
        }
    }
}
