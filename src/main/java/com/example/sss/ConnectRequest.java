package com.example.sss;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ConnectRequest {

    public String requestsetting(String serverUrl, String dataToSend){
        HttpURLConnection server = null;
        StringBuffer buffer = new StringBuffer();

        if (dataToSend == null)
            buffer.append("No data");
        else
                    buffer.append(dataToSend);

        try{
            URL url = new URL(serverUrl);
            server = (HttpURLConnection) url.openConnection();

            server.setConnectTimeout(16000);
            server.setReadTimeout(10000);
            server.setDoInput(true);
            server.setDoOutput(true);
            server.setUseCaches(false);
            server.setRequestMethod("POST");
            server.setRequestProperty("Context_Type", "application/x-www-form-urlencode");
            server.setRequestProperty("appkey", "l7xxa7619246ec224970a14441f3386b2a10");

            String strring = buffer.toString(); //toString wifi1&wifi2
            OutputStream os = server.getOutputStream();

            os.write(strring.getBytes("UTF-8"));
            os.flush();
            os.close();

            if (server.getResponseCode() != HttpURLConnection.HTTP_OK){
            Log.d("#######",Integer.toString(server.getResponseCode()));
                return null;}

            BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream(), "UTF-8"));
            String line;
            String data = "";


            while ((line = reader.readLine()) != null){
                data += line;
            }
            return data;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) server.disconnect();
        } return null;
    }
}

