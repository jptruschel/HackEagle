package com.hue.hackeagle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
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
import java.util.ArrayList;

/**
 * Created by João Paulo T Ruschel on 04/04/2014.
 *  Contains functions designed to help JSON operations.
 */
public class NetHelper {

    /** Downloads as a String */
    public static String DownloadString(String url) {
        InputStream is = null;
        String data = null;

        // Making HTTP request
        try {
            // defaultHttpClient
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            data = sb.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /** Downloads from a given URL as JSON.
     * CAUTION: This operation freezes the thread running it,
     *   consider calling this function on a separate thread.
     * CAUTION: This fixes a feature in the way the Closit server
     *    responds. */
    public static JSONObject getRequestsJSONFromUrl(String url) {
        JSONObject jObj = null;
        String json = DownloadString(url);

        try {
            jObj = new JSONObject("{\"reqs\": " + json + "}");
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    /** Downloads from a given URL as JSON.
     * CAUTION: This operation freezes the thread running it,
     * consider calling this function on a separate thread. */
    public static JSONObject getJSONFromUrl(String url) {
        JSONObject jObj = null;
        String json = DownloadString(url);

        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // return JSON String
        return jObj;
    }

    /** Parses a JSON object to an Array of Requests. */
    public static ArrayList<Req> parseRequestsJson(JSONObject json) {
        ArrayList<Req> dataList = new ArrayList<Req>();
        try {
            // parsing json object
            JSONArray posts = json.getJSONArray("reqs");
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = (JSONObject) posts.getJSONObject(i);

                Req req = new Req();
                req.id = post.getInt("id");
                req.price = (float)post.getDouble("price");
                req.user = post.getString("email");
                req.description = post.getString("description");
                req.category = CategoryTranslate(post.getString("categories"));
                req.date = post.getString("datetime");
                req.status = post.getString("status");
                req.finemail = post.getString("finemail");
                dataList.add(req);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i("JSON", "c:" + dataList.size());
        return dataList;
    }

    public static int CategoryTranslate(String s) {
        if (s.equals("models"))
            return 2;
        if (s.equals("textures"))
            return 1;
        if (s.equals("sprite sheets"))
            return 3;
        return 4;
    }
    public static String CategoryTranslate(int i) {
        if (i == 2)
            return "Model";
        if (i == 1)
            return "Texture";
        if (i == 3)
            return "Sprite Sheet";
        return "Other";
    }
 }
