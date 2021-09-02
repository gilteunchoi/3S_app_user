package com.example.sss;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class BusActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    String destination = "Null";
    String destinationId = "";
    boolean check = true;
    String lineNo = "";
    String min1 = "";
    String station1 = "";
    TextToSpeech textToSpeech;
    String speak;
    String messageReceived;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus);
        ImageButton fab = findViewById(R.id.fab);
        Intent intent = getIntent();
        String message = intent.getParcelableExtra("message");
        messageReceived = message;
        Pattern pattern = Pattern.compile("([0-9])");
        Matcher matcher = pattern.matcher(messageReceived);
        lineNo = matcher.toString();
        destination = messageReceived.split("\\s")[0];
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakout(speak);
                String server = "{\"String\":"+"\""+ speak + "\"" +"}";
                getbusdata("http://52.78.131.107:8000/buser",server);
            }
        });

        textToSpeech = new TextToSpeech(BusActivity.this, this);


        Button loca = findViewById(R.id.appCompatButton);
        loca.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        String Url = "";
        try {
            Url = makeUrl(destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Url = "http://61.43.246.153/openapi-data/service/busanBIMS2/stopArr?ServiceKey=&bstopid="+destinationId;
        getbusdata(Url,"");

    }

    public String makeUrl(String busstop) throws IOException {
        String busUrl = "http://61.43.246.153/openapi-data/service/busanBIMS2/busStop?ServiceKey=&pageNo=1&numOfRows=10&bstopnm="+destination;
        return busUrl;
    }

    public void getbusdata(String url, String dataToSend){
        BusDataForServer busDataForServer = new BusDataForServer(url,dataToSend);
        busDataForServer.execute();
    }

    protected class BusDataForServer extends AsyncTask<String, Void, String> {
        private String url;
        private String data;


        public BusDataForServer(String url,String data) {
            this.url = url;
            this.data = data;
        }

        @Override
        protected String doInBackground(String... urls) {
            if(check) {
                String stringUrl = url;
                String result;
                String inputLine;
                try {

                    URL myUrl = new URL(stringUrl);

                    HttpURLConnection connection = (HttpURLConnection)
                            myUrl.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setReadTimeout(15000);
                    connection.setConnectTimeout(15000);


                    connection.connect();

                    InputStreamReader streamReader = new
                            InputStreamReader(connection.getInputStream());

                    BufferedReader reader = new BufferedReader(streamReader);
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((inputLine = reader.readLine()) != null) {
                        stringBuilder.append(inputLine);
                    }

                    reader.close();
                    streamReader.close();

                    result = stringBuilder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    result = null;
                }
                return result;
            }
            else
            {
                String result;
                ConnectRequest connectRequest = new ConnectRequest();
                result = connectRequest.requestsetting(url, data);
                return result;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);


            if(check){
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = null;
                Document doc = null;

                // xml 파싱하기
                InputSource is = new InputSource(new StringReader(s));
                try {
                    builder = factory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }
                try {
                    doc = builder.parse(is);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();

                XPathExpression expr = null;
                try {
                    expr = xpath.compile("//items/item");
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
                NodeList nodeList = null;
                try {
                    nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
                Element el = null;
                NodeList sub_n_list = null;
                Element sub_el = null;

                Node v_txt = null;
                String value="";

                String[] tagList = {"lineNo", "min1", "station1"};
                int m = 0;
                for (int i = 0; i < nodeList.getLength(); i++) {
                    NodeList child = nodeList.item(i).getChildNodes();
                    el = (Element) nodeList.item(i);
                    for(int k=0; k< tagList.length; k++) {

                        sub_n_list = el.getElementsByTagName(tagList[k]);
                        for (int j = 0; j < sub_n_list.getLength(); j++) {


                            sub_el = (Element) sub_n_list.item(j);
                            v_txt = sub_el.getFirstChild();
                            value = v_txt.getNodeValue();

                            if(m==2){
                                station1 = value;
                                m++;
                            }
                            if(m ==1){
                                min1  = value;
                                m++;
                            }
                            if (value.equals(lineNo)) {
                                m++;
                            }



                            //}
                        }
                    }

                }
                String voice= lineNo+" 번 버스는 "+station1 + " 정거장 전이고"+ min1 + " 분 후에 도착합니다.";
                speak = voice;
                check =false;
            }
            else {

            }
        }
    }

    ///tts
    public void speakout(String OutMsg){
        if(OutMsg.length()<1)return;
        if(!textToSpeech.isSpeaking()) {
            textToSpeech.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null); }
    }
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.KOREAN);
            textToSpeech.setPitch(1);
        } else {
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