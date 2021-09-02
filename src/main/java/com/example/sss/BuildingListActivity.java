package com.example.sss;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BuildingListActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    boolean thread = false;
    String replyFromServer;
    ArrayList<dataForSound> Sounds= new ArrayList<>();
    private Gpscheck gpscheck;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    double destlong;
    double destlati;
    int i=0;
    TextToSpeech textToSpeech;
    private ImageView imageView;
    String repeat;
    String messageReceived;
    private SensorManager sensorManager;
    private Sensor Accel;
    private Sensor Magnetic;

    private float[] floatGravity = new float[3];
    private float[] floatGeoMagnetic = new float[3];

    private float[] floatOrientation = new float[3];
    private float[] floatRotationMatrix = new float[9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildinglist);
        textToSpeech = new TextToSpeech(BuildingListActivity.this, this);
        gpscheck = new Gpscheck(BuildingListActivity.this);
        destlong = gpscheck.getLongitude();
        destlati = gpscheck.getLatitude();
        String start = getCurrentAddress(gpscheck.getLongitude(), gpscheck.getLatitude());
        Intent intent = getIntent();
        String message = intent.getParcelableExtra("message");
        messageReceived = message;
        String route = null;
        //


        //
        try {
            route = "startX=" + gpscheck.getLongitude() +"&startY=" + gpscheck.getLatitude() +"&angle=1&speed=2&endX=129.08878899594518&endY=35.2307947107897&reqCoordType=WGS84GEO&startName="+start.getBytes("UTF-8")+"&endName=%EB%B6%80%EC%82%B0%EB%8C%80%EC%97%AD&searchOption=0&resCoordType=WGS84GEO";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        sendStringToServer("http://apis.openapi.sk.com/tmap/routes/pedestrian",route);
        Button loca = findViewById(R.id.location);
        loca.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(intent);
            }
        });


        Button read = findViewById(R.id.btn1);
        read.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                funcVoiceOut(repeat);
            }
        });

            Thread checkthread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if(thread) {
                            if(true) {

                            String check = Double.toString(Sounds.size());
                            Log.d("&&&", check);
                            if (checkGpsAndRoute()) {
                                Log.d("&&&", "second");
                                destlong = Sounds.get(i).getLongtitude();
                                destlati = Sounds.get(i).getLatitude();
                                //읽어주기
                                repeat = Sounds.get(i).getRead();

                                funcVoiceOut(Sounds.get(i).getRead());

                                read.setText(repeat);


                                }

                            }
                        else {

                            }
                        }
                        try {
                            Thread.sleep(25000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            };
            checkthread.start();

        imageView = findViewById(R.id.pointer);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        Accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Magnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener sensorEventListenerAccelrometer = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGravity = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                imageView.setRotation((float) (-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        SensorEventListener sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                floatGeoMagnetic = event.values;

                SensorManager.getRotationMatrix(floatRotationMatrix, null, floatGravity, floatGeoMagnetic);
                SensorManager.getOrientation(floatRotationMatrix, floatOrientation);

                imageView.setRotation((float) (-floatOrientation[0]*180/3.14159));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListenerAccelrometer, Accel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, Magnetic, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void sendStringToServer(String url, String dataToSend){
        DataForServer dataForServer = new DataForServer(url, dataToSend);
        dataForServer.execute();
    }

    public class DataForServer extends AsyncTask<Void, Void, String> {
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
            super.onPostExecute(s); //요청 결과

            JSONObject json = null;
            if(s!=null) {
                try {
                    json = new JSONObject(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONArray ObjectArray = null;
                try {
                    ObjectArray = json.getJSONArray("features");
                    Log.d("^^^","first");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject featureObject = new JSONObject();

                for (int i = 0; i < ObjectArray.length(); i++) {
                    try {
                        Log.d("^^^","second");
                        featureObject = ObjectArray.getJSONObject(i);
                        JSONObject geometryObject = new JSONObject();
                        JSONObject propertiesObject = new JSONObject();
                        try {
                            geometryObject = featureObject.getJSONObject("geometry");
                            propertiesObject = featureObject.getJSONObject("properties");
                            JSONArray coordinates = new JSONArray();
                            try {
                                coordinates = geometryObject.getJSONArray("coordinates");
                                Log.d("+++", Double.toString(coordinates.length()));
                                if (coordinates.length() == 2.0) {
                                    if(coordinates.get(0) instanceof Double){
                                        double longti = coordinates.getDouble(0);
                                        double lati = coordinates.getDouble(1);
                                        String name = propertiesObject.optString("description");
                                        Sounds.add(new dataForSound(longti, lati, name));
                                    } else {

                                        Log.d("^^^", "third");

                                        Log.d("^^^", "fourth");

                                        JSONArray jsa1 = coordinates.getJSONArray(coordinates.length() - 1);

                                        double longti = jsa1.getDouble(0);
                                        double lati = jsa1.getDouble(1);

                                        Log.d("---", Double.toString(longti));
                                        Log.d("---", Double.toString(lati));
                                        String name = propertiesObject.optString("description");
                                        Sounds.add(new dataForSound(longti, lati, name));
                                        Log.d("^^^", "first");
                                    }
                                } else {

                                    Log.d("^^^", "third");

                                    Log.d("^^^", "fourth");

                                    JSONArray jsa1 = coordinates.getJSONArray(coordinates.length() - 1);

                                    double longti = jsa1.getDouble(0);
                                    double lati = jsa1.getDouble(1);

                                    Log.d("---", Double.toString(longti));
                                    Log.d("---", Double.toString(lati));
                                    String name = propertiesObject.optString("description");
                                    Sounds.add(new dataForSound(longti, lati, name));
                                    Log.d("^^^", "first");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                thread = true;

            }
            else
                Log.d("@@@@", "noreturn");
        }
    }

    //////////////////////////////////데이터
    public class dataForSound {
        Double longtitude;
        Double latitude;
        String read;

        public dataForSound(Double longtitude, Double latitude, String read) {
            this.longtitude = longtitude;
            this.latitude = latitude;
            this.read = read;
        }

        public Double getLongtitude() {
            return longtitude;
        }

        public String getRead() {
            return read;
        }

        public Double getLatitude() {
            return latitude;
        }
    }

    ////////////////////////////////gps

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {



            boolean check_result = true;




            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {


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

    void checkRunTimePermission(){

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(BuildingListActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(BuildingListActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {





        } else {


            if (ActivityCompat.shouldShowRequestPermissionRationale(BuildingListActivity.this, REQUIRED_PERMISSIONS[0])) {




                ActivityCompat.requestPermissions(BuildingListActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {

                ActivityCompat.requestPermissions(BuildingListActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress(double latitude, double longitude){


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {

            return "no service";
        } catch (IllegalArgumentException illegalArgumentException) {

            return "no gps";

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


                if (checkServicesStatus()) {
                    if (checkServicesStatus()) {


                        checkRunTimePermission();
                        return;
                    }
                }

                break;
        }
    }

    public boolean checkServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean checkGpsAndRoute() {
        double latitude = gpscheck.getLatitude();
        double longitude = gpscheck.getLongitude();
        double mindis = Math.floor(distanceBetween(latitude,longitude,destlati,destlong));
        Log.d("***",Double.toString(mindis));
        if(mindis<20)
            return true;
        else
            return false;
    }
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


    ///tts
    public void funcVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null); }
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.KOREAN);
            textToSpeech.setPitch(1);
        }
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onDestroy();
    }

}

