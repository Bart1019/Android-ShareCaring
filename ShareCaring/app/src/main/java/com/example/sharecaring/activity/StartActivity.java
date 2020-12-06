package com.example.sharecaring.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.sharecaring.model.IntentOpener;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sharecaring.R;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class StartActivity extends AppCompatActivity {
    private Dialog optionsDialog;
    private ImageView closePopUp;
    private Button optionsBtn, logInEmail, logIn, logInFb;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //initialize Facebook SDK
        FacebookSdk.sdkInitialize(StartActivity.this);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        logInFb = findViewById(R.id.fbBtn);

        logInFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {}

                    @Override
                    public void onError(FacebookException error) {}
                });
            }
        });

        optionsDialog = new Dialog(this);

        optionsBtn = findViewById(R.id.optionsBtn);
        optionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUp();
            }
        });
    }

    /*@Override this is commented because it will remeber the fb signed in user - need to add sign out possibility in profile
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //this enables execution of handleFacebook..
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) { //called when the user gives the permission to access their data

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(StartActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            IntentOpener.openIntent(StartActivity.this, InformationActivity.class);
        } else {
            Toast.makeText(this, getResources().getString(R.string.toast_fb_firebase), Toast.LENGTH_LONG).show();
        }
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
                IntentOpener.openIntent(StartActivity.this, LoginActivity.class);
                optionsDialog.dismiss();
            }
        });

        logInEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentOpener.openIntent(StartActivity.this, RegisterActivity.class);
                optionsDialog.dismiss();
            }
        });

        optionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        optionsDialog.show();
    }
}