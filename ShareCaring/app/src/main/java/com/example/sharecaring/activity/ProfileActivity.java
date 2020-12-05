package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.service.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextFirstName, editTextLastName, editTextEmail;
    Button btnMyOffers;
    DatabaseReference ref;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editTextFirstName = (EditText)findViewById(R.id.editProfileTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editProfileTextLastName);
        editTextEmail = (EditText)findViewById(R.id.editProfileTextEmail);
        btnMyOffers = (Button)findViewById(R.id.btnMyOffers);
        btnMyOffers.setOnClickListener(this);
        loadData();
    }

   private void loadData() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstNameFromDB = snapshot.child("firstName").getValue().toString();
                String lastNameFromDB = snapshot.child("lastName").getValue().toString();
                String emailFromDB = snapshot.child("email").getValue().toString();
                editTextFirstName.setText(firstNameFromDB);
                editTextLastName.setText(lastNameFromDB);
                editTextEmail.setText(emailFromDB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnMyOffers) {
            IntentOpener.openIntent(ProfileActivity.this, MyOffersActivity.class);
        }
    }
}