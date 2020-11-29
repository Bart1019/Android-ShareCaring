package com.example.sharecaring.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.facebook.appevents.suggestedevents.ViewOnClickListener;

public class InformationActivity extends AppCompatActivity implements View.OnClickListener {

    Button gotIt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        gotIt = (Button)findViewById(R.id.btnGotIt);
        gotIt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnGotIt:
                IntentOpener.openIntent(InformationActivity.this, MapsActivity.class);
                break;

        }
    }
}