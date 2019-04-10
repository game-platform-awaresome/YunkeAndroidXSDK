package com.shykad.yunke.sdk.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.shykad.yunke.sdk.R;

import androidx.annotation.Nullable;

/**
 * Create by wanghong.he on 2019/3/8.
 * description：
 */
public class SplashActivity extends PermissionActivity {

    private Button showSplashBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.yunke_activity_splash);

        init();
    }

    private void init() {
        showSplashBtn = findViewById(R.id.show_splash);
        showSplashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SplashActivity.this,SplashAdActivity.class));
                finish();
            }
        });
    }
}
