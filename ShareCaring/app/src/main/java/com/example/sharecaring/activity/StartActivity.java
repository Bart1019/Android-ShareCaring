package com.example.sharecaring.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sharecaring.R;

public class StartActivity extends AppCompatActivity {
    private Dialog optionsDialog;
    private ImageView closePopUp;
    private Button optionsBtn, logInEmail, logIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        optionsDialog = new Dialog(this);

        optionsBtn = findViewById(R.id.optionsBtn);
        optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUp();
            }
        });
    }

    private void showPopUp() {
        optionsDialog.setContentView(R.layout.popup);

        closePopUp = optionsDialog.findViewById(R.id.closePopUp);
        logIn = optionsDialog.findViewById(R.id.logInBtn);
        logInEmail = optionsDialog.findViewById(R.id.emailBtn);

        closePopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsDialog.dismiss();
            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect(true);
            }
        });

        logInEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirect(false);
            }
        });

        optionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        optionsDialog.show();
    }

    private void redirect(boolean login) {
        Intent intent;
        if(login) {
            intent = new Intent(getApplicationContext(), LoginActivity.class);
        } else {
            intent = new Intent(getApplicationContext(), RegisterActivity.class);
        }

        startActivity(intent);
        optionsDialog.dismiss();
    }
}