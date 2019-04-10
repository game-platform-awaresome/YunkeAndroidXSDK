package com.shykad.yunke.sdk;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shykad.yunke.sdk.ui.activity.BannerActivity;
import com.shykad.yunke.sdk.ui.activity.InfoFlowActivity;
import com.shykad.yunke.sdk.ui.activity.InterstitialActivity;
import com.shykad.yunke.sdk.ui.activity.SplashActivity;
import com.shykad.yunke.sdk.ui.activity.TemplateActivity;
import com.shykad.yunke.sdk.ui.activity.VideoAdActivity;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button bannerBtn,insertScreenBtn,openScreenBtn,nativeTemplateBtn,
            nativeImageBtn,nativeVideoBtn,informationFlowBtn,excitationVideoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_main);

        bannerBtn = this.findViewById(R.id.banner);
        bannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(BannerActivity.class);
            }
        });
        openScreenBtn = findViewById(R.id.openScreen);
        openScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(SplashActivity.class);
            }
        });
        insertScreenBtn = findViewById(R.id.insertScreen);
        insertScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(InterstitialActivity.class);
            }
        });
        nativeImageBtn = findViewById(R.id.nativeTemplate);
        nativeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(TemplateActivity.class);
            }
        });
        informationFlowBtn = findViewById(R.id.informationFlow);
        informationFlowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(InfoFlowActivity.class);
            }
        });
        excitationVideoBtn = findViewById(R.id.excitationVideo);
        excitationVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adInit(VideoAdActivity.class);
            }
        });

    }

    private void adInit(Class<?> cls){

        startActivity(new Intent(MainActivity.this, cls));
    }
}
