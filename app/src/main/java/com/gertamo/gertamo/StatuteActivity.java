package com.gertamo.gertamo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


public class StatuteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statute);
        TextView linkTextView = findViewById(R.id.regulations_par_10_hyperlink);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}