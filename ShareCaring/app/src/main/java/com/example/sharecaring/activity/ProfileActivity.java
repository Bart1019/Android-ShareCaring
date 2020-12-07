package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.User;
import com.example.sharecaring.service.DatabaseService;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextFirstName, editTextLastName, editTextEmail;
    Button btnMyOffers, btnLogOut;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String firstNameFromDB, lastNameFromDB, emailFromDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        editTextFirstName = (EditText)findViewById(R.id.editProfileTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editProfileTextLastName);
        editTextEmail = (EditText)findViewById(R.id.editProfileTextEmail);
        btnMyOffers = (Button)findViewById(R.id.btnMyOffers);
        btnMyOffers.setOnClickListener(this);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(this);
        loadData();
    }

   private void loadData() {
        String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    firstNameFromDB = snapshot.child("firstName").getValue().toString();
                    lastNameFromDB = snapshot.child("lastName").getValue().toString();
                    emailFromDB = snapshot.child("email").getValue().toString();
                } else {
                    if(user != null) {
                        String str = user.getDisplayName();
                        firstNameFromDB = str.substring(0, str.indexOf(' '));   //to first space
                        lastNameFromDB = str.substring(str.indexOf(' ') + 1); //rest
                        emailFromDB = user.getEmail();
                    }

                }
                editTextFirstName.setText(firstNameFromDB);
                editTextLastName.setText(lastNameFromDB);
                editTextEmail.setText(emailFromDB);
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnMyOffers) {
            IntentOpener.openIntent(ProfileActivity.this, MyOffersActivity.class);
        } else if (v.getId() == R.id.btnLogOut) {
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            IntentOpener.openIntent(ProfileActivity.this, StartActivity.class);
        }
    }
}