package com.example.sharecaring.activity;

import android.annotation.SuppressLint;
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
import com.example.sharecaring.model.User;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int RC_SIGN_IN = 120;  //can be different number

    private Dialog optionsDialog;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(StartActivity.this, gso);

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        Button logInFb = findViewById(R.id.fbBtn);
        logInFb.setOnClickListener(this);

        //initialize google login button
        Button logInGoogle = findViewById(R.id.googleBtn);
        logInGoogle.setOnClickListener(this);

        optionsDialog = new Dialog(this);

        Button optionsBtn = findViewById(R.id.optionsBtn);
        optionsBtn.setOnClickListener(this);
    }

    @Override //if the user is signed in, automatically redirects to maps
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        user = mAuth.getCurrentUser();
        updateUI(user, MapsActivity.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { //this enables execution of handleFacebook and receives a call back form signInGoogle
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if(task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("StartActivity", e);
                }
            } else {
                Log.w("StartActivity", task.getException());
            }

        }
    }

    private void handleFacebookAccessToken(AccessToken token) { //called when the user gives the permission to access their data
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        credentialSignIn(credential);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        credentialSignIn(credential);
    }

    private void credentialSignIn(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            user = mAuth.getCurrentUser();
                            updateUI(user, InformationActivity.class);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(StartActivity.this, getResources().getText(R.string.toast_auth),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null, null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user, Class c) {
        if (user != null) {
            IntentOpener.openIntent(StartActivity.this, c);
            finish();
        }
    }

    private void signInFb() {
        LoginManager.getInstance()
                .logInWithReadPermissions(StartActivity.this, Arrays.asList("email", "public_profile"));
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

    private void signInGoogle() { //sign in for google
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void showPopUp() {
        optionsDialog.setContentView(R.layout.popup);

        ImageView closePopUp = optionsDialog.findViewById(R.id.closePopUp);
        Button logIn = optionsDialog.findViewById(R.id.logInBtn);
        Button logInEmail = optionsDialog.findViewById(R.id.emailBtn);

        closePopUp.setOnClickListener(this);
        logIn.setOnClickListener(this);
        logInEmail.setOnClickListener(this);

        optionsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        optionsDialog.show();
    }

    private void handlePopUpActions(Class c) {
        IntentOpener.openIntent(StartActivity.this, c);
        optionsDialog.dismiss();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.fbBtn:
                signInFb();
                break;
            case R.id.googleBtn:
                signInGoogle();
                break;
            case R.id.optionsBtn:
                showPopUp();
                break;
            case R.id.closePopUp:
                optionsDialog.dismiss();
                break;
            case R.id.logInBtn:
                handlePopUpActions(LoginActivity.class);
                break;
            case R.id.emailBtn:
                handlePopUpActions(RegisterActivity.class);
                break;
        }
    }
}