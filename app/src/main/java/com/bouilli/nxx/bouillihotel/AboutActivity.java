package com.bouilli.nxx.bouillihotel;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity {
    private WebView user_help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        user_help = (WebView) findViewById(R.id.user_help);

        user_help.getSettings().setJavaScriptEnabled(true);
        user_help.getSettings().setAllowFileAccess(true);
        CookieManager.getInstance().setAcceptCookie(true);
        user_help.getSettings().setNeedInitialFocus(false);
        user_help.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        user_help.loadUrl("http://139.224.25.220/BouilliHotelServer/toUserHelp");
    }
}
