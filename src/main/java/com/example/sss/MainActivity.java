package com.example.sss;

import android.Manifest; import android.content.Context; import android.content.Intent; import android.os.Build; import android.os.Bundle; import android.speech.RecognitionListener; import android.speech.RecognizerIntent; import android.speech.SpeechRecognizer; import android.speech.tts.TextToSpeech; import android.util.Log; import android.view.View; import android.widget.Button;
import android.widget.ImageButton;
 import android.widget.Toast; import androidx.appcompat.app.AppCompatActivity; import androidx.core.app.ActivityCompat; import java.util.ArrayList; import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    Intent intent;
    public static Context Context;
    SpeechRecognizer Recognize;


    TextToSpeech textToSpeech;
    final int permission = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Context = MainActivity.this;
        if ( Build.VERSION.SDK_INT >= 23 ){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, permission);
        }

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
        textToSpeech = new TextToSpeech(MainActivity.this, this);
        Button btn3 = findViewById(R.id.appCompatButton3);

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                speechStart();
            }
        });
        ImageButton btn1 = findViewById(R.id.btn1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v){
                speechStart();
            }
        });

        funcVoiceOut("???????????? ???????????? ??????????????????.");
    }

    public void speechStart() {
        Recognize = SpeechRecognizer.createSpeechRecognizer(Context);
        Recognize.setRecognitionListener(listen);
        Recognize.startListening(intent);
    }

    private RecognitionListener listen = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }
        @Override
        public void onBeginningOfSpeech() {}
        @Override
        public void onRmsChanged(float rmsdB) {}
        @Override
        public void onBufferReceived(byte[] buffer) {}
        @Override public void onEndOfSpeech() {}
        @Override public void onError(int error) {
            String message;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO: message = "?????????";
                break;
                case SpeechRecognizer.ERROR_CLIENT: message = "???????????????";
                break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "?????????";
                break;
                case SpeechRecognizer.ERROR_NETWORK: message = "????????????";
                break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "????????????";
                break;
                case SpeechRecognizer.ERROR_NO_MATCH: message = "??????";
                break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: message = "RECOGNIZER??? ??????";
                break;
                case SpeechRecognizer.ERROR_SERVER: message = "????????? ?????????";
                break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "????????????";
                break;
                default: message = "??? ??? ?????? ?????????";
                break;
            }
        }
        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String resultStr = "";
            for (int i = 0; i < matches.size(); i++) {
                resultStr += matches.get(i);
            } if(resultStr.length() < 1)
                return;
            resultStr = resultStr.replace(" ", "");
            moveActivity(resultStr);
        }
        @Override
        public void onPartialResults(Bundle partialResults) { }
        @Override
        public void onEvent(int eventType, Bundle params) { }


    };
    public void moveActivity(String resultStr) {
        if(resultStr.indexOf("?????????") > -1) {
            String guideStr = "?????????????????? ?????????????????????.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_SHORT).show();
            funcVoiceOut(guideStr);
            Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
            intent.putExtra("message",resultStr);
            startActivity(intent);
        }

        if(resultStr.indexOf("??????") > -1) {
            String guideStr = "?????? ????????? ?????????????????????.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_SHORT).show();
            funcVoiceOut(guideStr);
            Intent intent = new Intent(getApplicationContext(), BusActivity.class);
            intent.putExtra("message",resultStr);
            startActivity(intent);
        }

        if(resultStr.indexOf("??????") > -1) {
            String guideStr = "????????? ????????? ?????????????????????.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_SHORT).show();
            funcVoiceOut(guideStr);
            Intent intent = new Intent(getApplicationContext(), BuildingListActivity.class);
            intent.putExtra("message",resultStr);
            startActivity(intent);
        }

    }
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
        if(Recognize !=null){
            Recognize.destroy();
            Recognize.cancel();
            Recognize =null;
        }
        super.onDestroy();
    }
}