package org.cysoft.carovignobot;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;

public class WebMapActivity extends AppCompatActivity {

    private String mMapType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_map);
        setTitle(getResources().getString(R.string.map_activity_name));

        if (getIntent().getStringExtra("mapType")!=null)
            mMapType=getIntent().getStringExtra("mapType");


        ((ImageButton)findViewById(R.id.buttonSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WebMapActivity.this,SearchLocationActivity.class);
                intent.putExtra("locType",mMapType);
                startActivity(intent);
            }
        });



        WebView webMap=(WebView)findViewById(R.id.webMap);
        if (webMap!=null) {
            WebSettings webSettings = webMap.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);
            webMap.setWebChromeClient(new WebChromeClient());
            String urlMap=getIntent().getStringExtra("mapUrl");
            webMap.loadUrl(urlMap);
        }
    }
}
