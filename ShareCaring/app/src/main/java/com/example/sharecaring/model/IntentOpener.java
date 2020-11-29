package com.example.sharecaring.model;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

public class IntentOpener extends AppCompatActivity {
    public static void openIntent(Context context, Class activityName) {
        Intent intent = new Intent(context, activityName);
        context.startActivity(intent);
    }
}
