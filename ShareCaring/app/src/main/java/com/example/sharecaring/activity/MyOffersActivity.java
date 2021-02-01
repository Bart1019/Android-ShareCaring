package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.Offer;
import com.example.sharecaring.model.UserCallback;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyOffersActivity extends AppCompatActivity {

    FloatingActionButton btnAddOffer;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String description, address, medication, animals, shopping, transport, offerId;
    LinearLayout layoutList;
    ImageView imageClose;
    Switch offerSwitch;
    ArrayList<String> myOffers = new ArrayList<>();
    TextView noOffers;
    RadioButton pendingOffers, acceptedOffers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = findViewById(R.id.layout_list);
        pendingOffers = findViewById(R.id.pendingOffersBtn);

        acceptedOffers = findViewById(R.id.acceptedOffersBtn);

        noOffers = findViewById(R.id.noOffers);
        getMyOffers("true", "true");

        offerSwitch = findViewById(R.id.volunteersSwitcher);

        offerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                layoutList.removeAllViews();
                if (acceptedOffers.isChecked()) {
                    if (isChecked) {  //on means needs
                        getMyOffers("false", "true");
                    } else {
                        getMyOffers("true", "true");
                    }
                } else if (pendingOffers.isChecked()) {
                    if (isChecked) {  //on means needs
                        getMyOffers("false", "false");
                    } else {
                        getMyOffers("true", "false");
                    }
                }
            }
        });

        RadioGroup radioGroup = findViewById(R.id.rdogrp);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                layoutList.removeAllViews();
                if (acceptedOffers.isChecked()) {
                    if (offerSwitch.isChecked()) {  //on means needs
                        getMyOffers("false", "true");
                    } else {
                        getMyOffers("true", "true");
                    }
                } else if (pendingOffers.isChecked()) {
                    if (offerSwitch.isChecked()) {  //on means needs
                        getMyOffers("false", "false");
                    } else {
                        getMyOffers("true", "false");
                    }
                }
            }
        });
    }

    public void getMyOffers(final String offerType, final String isAccepted) {
        final String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean exists = false;
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    if (postSnapshot.child("isVolunteering").getValue().toString().equals(offerType)
                             && postSnapshot.child("isAccepted").getValue().toString().equals(isAccepted)) {
                        offerId = postSnapshot.getKey();
                        address = postSnapshot.child("address").getValue().toString();
                        description = postSnapshot.child("description").getValue().toString();
                        animals = postSnapshot.child("animals").getValue().toString();
                        medication = postSnapshot.child("medication").getValue().toString();
                        shopping = postSnapshot.child("shopping").getValue().toString();
                        transport = postSnapshot.child("transport").getValue().toString();
                        exists = true;
                        putDataToTextView();
                    }
                }
                Log.d("TAG", "onDataChange: " + exists);
                if (exists) {
                    noOffers.setVisibility(View.INVISIBLE);
                } else {
                    noOffers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private void putDataToTextView() {
        final View myOfferView = getLayoutInflater().inflate(R.layout.offer, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewAddress);
        myOfferTextView.setText(address +"\n" + description);
        TextView name = (TextView)myOfferView.findViewById(R.id.userName);
        name.setText("Me");

        ImageView profilePhoto = (ImageView)myOfferView.findViewById(R.id.profileImgOffer);
        downloadProfilePic(user.getUid(), profilePhoto);  //mine

        ImageView imageAnimals = (ImageView)myOfferView.findViewById(R.id.first);
        ImageView imageMedication = (ImageView)myOfferView.findViewById(R.id.second);
        ImageView imageTransport = (ImageView)myOfferView.findViewById(R.id.third);
        ImageView imageShopping = (ImageView)myOfferView.findViewById(R.id.fourth);
        List<ImageView> images = new ArrayList<>();
        List<String> offersTypes = new ArrayList<>();

        images.add(imageAnimals);
        images.add(imageMedication);
        images.add(imageTransport);
        images.add(imageShopping);
        offersTypes.add(animals);
        offersTypes.add(medication);
        offersTypes.add(transport);
        offersTypes.add(shopping);

        List<Integer> drawables = new ArrayList<>();
        drawables.add(R.drawable.dog);
        drawables.add(R.drawable.medicine);
        drawables.add(R.drawable.car);
        drawables.add(R.drawable.groceries);

        List<Integer> newDrawables = new ArrayList<>();

        if (isCare(offersTypes)) {
            imageAnimals.setImageResource(R.drawable.care);
        } else {
            for (int i = 0; i < offersTypes.size(); i++) {
                if (offersTypes.get(i).equals("true")) {
                    newDrawables.add(drawables.get(i));
                } else {
                    newDrawables.add(-1);
                }
            }

            for (Iterator<Integer> iter = newDrawables.listIterator(); iter.hasNext(); ) {
                int a = iter.next();
                if (a == -1) {
                    iter.remove();
                }
            }

            for (int i = 0; i < newDrawables.size(); i++) {
                images.get(i).setImageResource(newDrawables.get(i));
            }

        }

        imageClose = (ImageView)myOfferView.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(myOfferView);
            }
        });

        myOfferView.setTag(offerId);
        layoutList.addView(myOfferView);
    }

    private boolean isCare(List<String> offersTypes) {
        for (String offer : offersTypes) {
            if (!offer.equals("true"))
                return false;
        }
        return true;
    }

    private void downloadProfilePic(String userId, final ImageView profile) {
        String path = userId + "/profilePicture.jpg";
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profile);
            }
        });
    }

    private void removeView(View view) {
        layoutList.removeView(view);
        offerId = view.getTag().toString();
        String userId = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers/"+ userId).child(offerId);
        ref.removeValue();
    }
}