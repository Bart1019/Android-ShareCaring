package com.example.sharecaring.fragment;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharecaring.R;
import com.example.sharecaring.activity.EditProfileActivity;
import com.example.sharecaring.activity.MapsActivity;
import com.example.sharecaring.activity.MyOffersActivity;
import com.example.sharecaring.activity.NotificationActivity;
import com.example.sharecaring.activity.OfferList;
import com.example.sharecaring.activity.ProfileActivity;
import com.example.sharecaring.activity.StartActivity;
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

public class ProfileFragment extends Fragment implements View.OnClickListener{

    TextView editTextFirstName, editTextEmail;
    ImageButton btnLogOut, btnEditProfile;
    Button btnMyOffers;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String firstNameFromDB, lastNameFromDB, emailFromDB;
    CircularImageView profilePic;
    StorageReference storageReference;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        editTextFirstName = (TextView) v.findViewById(R.id.editProfileTextFirstName);
        editTextEmail = (TextView) v.findViewById(R.id.editProfileTextEmail);
        btnMyOffers = (Button)v.findViewById(R.id.btnFinish);
        btnMyOffers.setOnClickListener(this);
        btnLogOut = v.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(this);
        btnEditProfile = v.findViewById(R.id.editBtn);
        btnEditProfile.setOnClickListener(this);
        profilePic = v.findViewById(R.id.profileImg);
        loadData();
        return v;
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
                }
                String name = firstNameFromDB + " " + lastNameFromDB;
                editTextFirstName.setText(name);
                editTextEmail.setText(emailFromDB);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        Uri uri = user.getPhotoUrl();
        if(uri != null) {
            Picasso.with(getActivity()).load(uri).into(profilePic);
        }

        String path = userid + "/profilePicture.jpg";
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(getActivity()).load(uri).into(profilePic);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnFinish:
                IntentOpener.openIntent(getActivity(), MyOffersActivity.class);
                break;
            case R.id.btnLogOut:
                mAuth.signOut();
                IntentOpener.openIntent(getActivity(), StartActivity.class);
                //finish();
                break;
            case R.id.editBtn:
                IntentOpener.openIntent(getActivity(), EditProfileActivity.class);
                break;
        }
    }
}
