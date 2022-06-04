package com.startup.paynchat.activities;

import android.content.Intent;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.startup.paynchat.R;
import com.startup.paynchat.utils.Helper;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        final Helper helper = new Helper(this);
        new Handler().postDelayed(() -> {
            if (!Helper.DISABLE_SPLASH_HANDLER) {
                if (helper.getLoggedInUser() != null) {
                    if (helper.getLoggedInUser().isCounsellor()){
                        startActivity(new Intent(SplashActivity.this, MainActivityCounsellor.class));
                    }else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                }else{
                    startActivity(new Intent(SplashActivity.this, SignInActivity.class));
                }
            }
            Helper.DISABLE_SPLASH_HANDLER = false;
            finish();
        }, 1500);
    }
}
