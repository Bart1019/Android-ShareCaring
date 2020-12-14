package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.Offer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyOffersActivity extends AppCompatActivity {

    Button btnAddOffer;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String description, address, medication, animals, shopping, transport, offerId;
    LinearLayout layoutList;
    ImageView imageClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = findViewById(R.id.layout_list);

        btnAddOffer = (Button)findViewById(R.id.btnAddOffer);
        btnAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentOpener.openIntent(MyOffersActivity.this, AddOfferActivity.class);
            }
        });
        getMyOffers();
    }


    public void getMyOffers() {
        String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    offerId = postSnapshot.getKey();
                    System.out.println(offerId);
                    address = postSnapshot.child("address").getValue().toString();
                    description = postSnapshot.child("description").getValue().toString();
                    animals = postSnapshot.child("animals").getValue().toString();
                    medication = postSnapshot.child("medication").getValue().toString();
                    shopping = postSnapshot.child("shopping").getValue().toString();
                    transport = postSnapshot.child("transport").getValue().toString();
                    putDataToTextView();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void putDataToTextView() {
        final View myOfferView = getLayoutInflater().inflate(R.layout.offer, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewSingleOffer);
        myOfferTextView.setText(address + ' ' + description + ' ' + animals + ' ' + medication + ' ' + shopping + ' ' + medication);

        imageClose = (ImageView)myOfferView.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(myOfferView);
            }
        });

        layoutList.addView(myOfferView);
    }

    private void removeView(View view) {
        layoutList.removeView(view);
        //String userId = user.getUid();
        //ref = FirebaseDatabase.getInstance().getReference("Offers/"+ userId).child(offerId);
        //ref.removeValue();
    }

}