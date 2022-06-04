package com.startup.paynchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.startup.paynchat.R;
import com.startup.paynchat.utils.PreferenceConnector;

public class SelectLoginType extends AppCompatActivity {
    CardView userLogin, counLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startscreen);

        userLogin = findViewById(R.id.lay_user);
        counLogin = findViewById(R.id.lay_coun);

        counLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SelectLoginType.this, ConsultantSignInActivity.class);
                startActivity(i);
            }
        });
        userLogin.setOnClickListener(v -> {
            if (PreferenceConnector.readBoolean(SelectLoginType.this, PreferenceConnector.ISINTROSHOWED, false)) {
                Intent i = new Intent(SelectLoginType.this, SignInActivity.class);
                startActivity(i);
            }else {
                Intent i = new Intent(SelectLoginType.this, ActivityIntro.class);
                startActivity(i);
            }
        });
    }
}
