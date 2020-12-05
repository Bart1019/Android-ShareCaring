package com.example.sharecaring.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;

public class MyOffersActivity extends AppCompatActivity {

    Button btnAddOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        btnAddOffer = (Button)findViewById(R.id.btnAddOffer);
        btnAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentOpener.openIntent(MyOffersActivity.this, AddOfferActivity.class);
            }
        });
    }
}