package com.gertamo.gertamo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        TextView linkTextView = findViewById(R.id.privacy_policy_par_8_hyperlink);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}