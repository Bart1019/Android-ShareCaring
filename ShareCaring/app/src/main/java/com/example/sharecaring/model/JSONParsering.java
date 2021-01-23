package com.example.sharecaring.model;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class JSONParsering {
    static InputStream input = null;
    static JSONObject jobj = null;
    static String json = "";

    public JSONParsering() {
    }

    public JSONObject getJSONFromUrl(String url) {

        try {
            DefaultHttpClient req = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse res = req.execute(httpPost);
            HttpEntity entity = res.getEntity();
            input = entity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader read = new BufferedReader(new InputStreamReader(
                    input, "iso-8859-1"), 8);
            StringBuilder builderonj = new StringBuilder();
            String line = null;
            while ((line = read.readLine()) != null) {
                builderonj.append(line + "\n");
            }
            input.close();
            json = builderonj.toString();
        } catch (Exception e) {
            Log.e("Buffer Reader", "Error...... " + e.toString());
        }

        try {
            jobj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("Parser", "Error while parsing... " + e.toString());
        }
        return jobj;
    }
}
