package com.example.sss;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;


public class SearchActivity extends AppCompatActivity implements TextToSpeech.OnInitListener
{
    String messageReceived;
    SpeechRecognizer mRecognizer;
    private Gpscheck gpscheck;
    boolean thread = false;
    boolean isthread = false;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    ArrayList<building> buildingArrayList = new ArrayList<>();
    private WifiManager wifiManager;
    MyHandler handler = null;
    String wifiList;
    String replyFromServer =null;
    TextToSpeech tts;
    boolean wifiSendCheck = true;

    public class building{
        double latitude ;
        double longitude ;
        String name;
        double distance;
        Boolean pended = false;
        public building(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public building(double latitude, double longitude, String name) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.name = name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
        public double getDistance(){
            return distance;
        }
        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getName() {
            return name;
        }
    }



    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {


            boolean check_ = true;



            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_ = false;
                    break;
                }
            }


            if (check_) {


                ;
            } else {


                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                   finish();


                } else {


                }
            }

        }
    }

    void RunTime(){






        int hasFineLocationPermission = ContextCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(SearchActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {




        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SearchActivity.this, REQUIRED_PERMISSIONS[0])) {


                ActivityCompat.requestPermissions(SearchActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {

                ActivityCompat.requestPermissions(SearchActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getAddress(double latitude, double longitude){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {

            return "no geocoder";
        } catch (IllegalArgumentException illegalArgumentException) {

            return "wrong gps";

        }



        if (addresses == null || addresses.size() == 0) {
            return "no address";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }











   














    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:


                if (checkservice()) {
                    if (checkservice()) {

                        RunTime();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkservice() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //distance checking by 위도, 경도
    public float distanceBetween(double myLat, double myLong, double destLat, double destLong){

        Location me   = new Location("");
        Location dest = new Location("");

        me.setLatitude(myLat);
        me.setLongitude(myLong);

        dest.setLatitude(destLat);
        dest.setLongitude(destLong);

        float dist = me.distanceTo(dest);

        return dist;
    }

    class DistanceComparator implements Comparator<building> {
        @Override
        public int compare(building b1, building b2) {
            if (b1.distance > b2.distance) {
                return 1;
            } else if (b1.distance < b2.distance) {
                return -1;
            }
            return 0;
        }
    }

    public double checkGps(){
        double latitude = gpscheck.getLatitude();
        double longitude = gpscheck.getLongitude();
        double mindis = 1000;
        for(building building:buildingArrayList){
            double buildingLatitude =building.getLatitude();
            double buildingLongitude = building.getLongitude();
            double distance = Math.floor(distanceBetween(latitude,longitude,buildingLatitude,buildingLongitude));
            building.setDistance(distance);
            if(mindis > building.getDistance())
                mindis = building.getDistance();
        }
        return mindis;
    }

    public void getWifi(){

        boolean success;
        success = wifiManager.startScan();

        if (!success) {
            handler.sendEmptyMessage(2);
        }

    }








    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }


    class MyHandler extends Handler {
        int count;
        @Override
        public void handleMessage(@NonNull Message handlerMessage) {
            switch (handlerMessage.what) {
                case 1:
                    List<ScanResult> results = wifiManager.getScanResults();
                    String tempText = "";
                    String textToShow = "{\"ID\":\"f96ecf5a-0020-11ec-9a03-0242ac130003\""+","+"\"latitude\""+":"+ "\"35.23082530901348\""+","+"\"longtitude\""+":"+"\"129.08862925183374\"";
                    count = 0;

                    for (final ScanResult result : results) {
                        if(count<10) {
                            String SSID = result.SSID;
                            if (SSID.contains("AndroidHotspot")) {        //핫스팟 제거
                                continue;
                            }
                            String BSSID = result.BSSID;
                            int RSSI = result.level;

                            tempText = "," + "\"" + "BSSID" + count + "\"" + ":" + "\"" + BSSID + "\"" + "," + "\"" + "RSSI" + count + "\"" + ":" + "\"" + RSSI + "\"";
                            textToShow += tempText;
                            count++;
                        }
                    }
                    wifiList=textToShow;
                    textToShow+="}";

                    sendStringToServer("http://52.78.131.107:8000/user", textToShow);

                    break;
                case 2:

                    break;
            }
            ;
        }
    }

    public void showMessage(String entrance){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("안내");
        String destination = buildingArrayList.get(0).getName();
        builder.setMessage(destination + entrance + "번 출구에 가십니까?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String YesHeWillGo = wifiList;
                YesHeWillGo+=","+"\"Answer\""+":"+"\"Y\"}";
                sendStringToServer("http://52.78.131.107:8000/user", YesHeWillGo);
                funcVoiceOut("역무원에게 신호를 전달했습니다. 잠시만 기다려주세요.");
            }
        });

        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildingArrayList.get(0).pended = true;
                wifiSendCheck = true;
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    protected void sendStringToServer(String url, String dataToSend){
        DataForServer dataForServer = new DataForServer(url, dataToSend);
        dataForServer.execute();
    }

    protected class DataForServer extends AsyncTask<Void, Void, String> {
        private String url;
        private String data;

        public DataForServer(String url, String data) {
            this.url = url;
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result;
            ConnectRequest connectRequest = new ConnectRequest();
            result = connectRequest.requestsetting(url, data);
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            JSONObject json = null;
            if(s!=null) {
                try {
                    json = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    replyFromServer = json.getString("location");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (wifiSendCheck) {
                    showMessage(replyFromServer);


                    wifiSendCheck = false;
                }
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //와이파이 //////////////////////
        if (!checkservice()) {


        } else {

            RunTime();
        }
        handler = new MyHandler();
        Intent intent = getIntent();
        String message = intent.getParcelableExtra("message");
        messageReceived = message;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

                if (success) {
                    handler.sendEmptyMessage(1);
                } else {
                    handler.sendEmptyMessage(2);
                }
            }
        };


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        ///////////////////////////////////
        Button but = findViewById(R.id.appCompatButton2);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speak = "부산대역 " + replyFromServer + "번 출구에 가십니까?";
                funcVoiceOut(speak);
                wifiSendCheck = false;
            }
        });

        tts = new TextToSpeech(SearchActivity.this, this);
        ImageButton fab = findViewById(R.id.btn1);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String YesHeWillGo = wifiList;
                YesHeWillGo+=","+"\"Answer\""+":"+"\"Y\"}";
                sendStringToServer("http://52.78.131.107:8000/user", YesHeWillGo);
                String speak = "지하철역에 전달했습니다. 역무원을 기다려주세요.";
                funcVoiceOut(speak);
            }
        });

        buildingArrayList.add(new building(35.23229121181535, 129.08424463249256));
        buildingArrayList.add(new building(35.22999008510681, 129.08928967644493));
        buildingArrayList.add(new building(35.233731364872796, 129.08099396903663));
        gpscheck = new Gpscheck(SearchActivity.this);
        StringBuilder addresstext = new StringBuilder("근처 건물들 \n");
        double latitude = gpscheck.getLatitude();
        double longitude = gpscheck.getLongitude();
        int i = 0;
        for (building building : buildingArrayList) {
            double buildingLatitude = building.getLatitude();
            double buildingLongitude = building.getLongitude();
            double distance = Math.floor(distanceBetween(latitude, longitude, buildingLatitude, buildingLongitude));
            building.setDistance(distance);
            String address = getAddress(buildingLatitude, buildingLongitude);
            building.setName(address);
        }
        Collections.sort(buildingArrayList, new DistanceComparator());
        for (building building : buildingArrayList) {
            if (i == 0) {
                String one = building.getName() + "    " + building.getDistance() + "m";
            }
            if (i == 1) {
                String two = building.getName() + "    " + building.getDistance() + "m";
            }
            if (i == 2) {
                String three = building.getName() + "    " + building.getDistance() + "m";
            }
            i++;
            double distance = building.getDistance();
            String address = building.getName();
            addresstext.append(address).append(distance).append('\n');
        }
        //textview_address.setText(addresstext.toString());


        if (thread) {
            thread = false;

        } else {
            thread = true;

            Thread checkthread = new Thread() {
                long minimumThreadTime = 100000;

                @Override
                public void run() {
                    while (thread) {
                        double mindistance = checkGps();
                        mindistance = Math.floor(mindistance);
                        minimumThreadTime = (new Double(mindistance)).longValue();
                        minimumThreadTime = minimumThreadTime/6 * 10000;

                        if (mindistance < 20 && (buildingArrayList.get(0).pended == false)) {
                            getWifi();
                            buildingArrayList.get(0).pended = true;
                            Timer pendTimer = new Timer();
                            TimerTask pendTask = new TimerTask() {
                                @Override
                                public void run() {
                                    buildingArrayList.get(0).pended = false;
                                }
                            };
                            pendTimer.schedule(pendTask,minimumThreadTime);
                            buildingArrayList.get(0).pended = true;
                            thread =false;
                        }
                        try {
                            Thread.sleep(minimumThreadTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            };
            checkthread.start();
        }
    }
    public void funcVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;
        if(!tts.isSpeaking()) {
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null); }
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setPitch(1);
        } else {
            }
    }
    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
        super.onDestroy();
    }
}

//최소 거리 고치기 & 서버통신 형식 + 답변정하기 &&