package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    EditText editTextFirstName, editTextLastName, editTextEmail;
    Button btnMyOffers, btnLogOut, btnEditProfile;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String firstNameFromDB, lastNameFromDB, emailFromDB;
    CircularImageView profilePic;
    StorageReference storageReference;
    BottomNavigationView bottomNavigationView;
    FloatingActionButton fabBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackgroundColor(Color.TRANSPARENT);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
        fabBtn = findViewById(R.id.fab);
        fabBtn.setOnClickListener(this);


        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        editTextFirstName = (EditText)findViewById(R.id.editProfileTextFirstName);
        editTextLastName = (EditText)findViewById(R.id.editProfileTextLastName);
        editTextEmail = (EditText)findViewById(R.id.editProfileTextEmail);
        btnMyOffers = (Button)findViewById(R.id.btnFinish);
        btnMyOffers.setOnClickListener(this);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(this);
        btnEditProfile = findViewById(R.id.editBtn);
        btnEditProfile.setOnClickListener(this);
        profilePic = findViewById(R.id.profileImg);
        loadData();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.map:
                            IntentOpener.openIntent(ProfileActivity.this, MapsActivity.class);
                            break;
                        case R.id.profile:
                            IntentOpener.openIntent(ProfileActivity.this, ProfileActivity.class);
                            break;
                        case R.id.chat:
                            IntentOpener.openIntent(ProfileActivity.this, OfferList.class);
                            break;
                        case R.id.notifications:
                            IntentOpener.openIntent(ProfileActivity.this, NotificationActivity.class);
                            break;
                    }

                    return false;
                }
            };

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
                }
                editTextFirstName.setText(firstNameFromDB);
                editTextLastName.setText(lastNameFromDB);
                editTextEmail.setText(emailFromDB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Uri uri = user.getPhotoUrl();
        if(uri != null) {
            Picasso.with(ProfileActivity.this).load(uri).into(profilePic);
        }
       
       String path = userid + "/profilePicture.jpg";
       final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
       storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
           @Override
           public void onSuccess(Uri uri) {
               Picasso.with(ProfileActivity.this).load(uri).into(profilePic);
           }
       });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnFinish:
                IntentOpener.openIntent(ProfileActivity.this, MyOffersActivity.class);
                break;
            case R.id.btnLogOut:
                mAuth.signOut();
                IntentOpener.openIntent(ProfileActivity.this, StartActivity.class);
                finish();
                break;
            case R.id.editBtn:
                IntentOpener.openIntent(ProfileActivity.this, EditProfileActivity.class);
                break;
            case R.id.fab:
                break;
        }
    }
}

