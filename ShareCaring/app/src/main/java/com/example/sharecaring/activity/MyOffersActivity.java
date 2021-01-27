package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.IntentOpener;
import com.example.sharecaring.model.Offer;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyOffersActivity extends AppCompatActivity {

    FloatingActionButton btnAddOffer;
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String description, address, medication, animals, shopping, transport, offerId;
    LinearLayout layoutList;
    ImageView imageClose;
    Switch offerSwitch;
    ArrayList<String> myAcceptedOffers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_offers);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = findViewById(R.id.layout_list);
        getMyAcceptedOffers();
        btnAddOffer = (FloatingActionButton) findViewById(R.id.btnFloat);
        btnAddOffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentOpener.openIntent(MyOffersActivity.this, AddOfferActivity.class);
            }
        });

        getMyOffers("Volunteer");
        offerSwitch = findViewById(R.id.switcherMyOffers);
        offerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  //on means needs
                    layoutList.removeAllViews();
                    getMyOffers("Needs");
                } else {
                    layoutList.removeAllViews();
                    getMyOffers("Volunteer");
                }
            }
        });


    }




    public void getMyOffers(final String offerType) {
        final String userid = user.getUid();
        if(offerType.equals("Needs")) {
            ref = FirebaseDatabase.getInstance().getReference("Offers");
            ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        offerId = postSnapshot.getKey();
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
        } else if(offerType.equals("Volunteer")) {
            ref = FirebaseDatabase.getInstance().getReference("Offers");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot userIdDb : snapshot.getChildren()) {
                        System.out.println(userIdDb.getKey());
                        if(!userIdDb.getKey().equals(userid))
                            for(DataSnapshot offerIdDb : userIdDb.getChildren()) {
                                for(String offId : myAcceptedOffers) {
                                    if(offId.equals(offerIdDb.getKey())) {
                                        offerId = offerIdDb.getKey();
                                        address = offerIdDb.child("address").getValue().toString();
                                        description = offerIdDb.child("description").getValue().toString();
                                        animals = offerIdDb.child("animals").getValue().toString();
                                        medication = offerIdDb.child("medication").getValue().toString();
                                        shopping = offerIdDb.child("shopping").getValue().toString();
                                        transport = offerIdDb.child("transport").getValue().toString();
                                        putDataToTextView();
                                    }
                                }


                            }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void putDataToTextView() {
        final View myOfferView = getLayoutInflater().inflate(R.layout.offer, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewSingleOffer);
        myOfferTextView.setText(address +"\n" +description);

        imageClose = (ImageView)myOfferView.findViewById(R.id.imageClose);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(myOfferView);
            }
        });

        if(animals.equals("true")) {
            ImageView imageAnimals = (ImageView)myOfferView.findViewById(R.id.imageAnimals);
            imageAnimals.setImageResource(R.drawable.dog);
        }

        if(medication.equals("true")) {
            ImageView imageMedication = (ImageView)myOfferView.findViewById(R.id.imageMedication);
            imageMedication.setImageResource(R.drawable.medicine);
        }

        if(transport.equals("true")) {
            ImageView imageTransport = (ImageView)myOfferView.findViewById(R.id.imageTransport);
            imageTransport.setImageResource(R.drawable.car);
        }

        if(shopping.equals("true")) {
            ImageView imageShopping = (ImageView)myOfferView.findViewById(R.id.imageShopping);
            imageShopping.setImageResource(R.drawable.groceries);
        }


        myOfferView.setTag(offerId);
        layoutList.addView(myOfferView);
    }

    private void getMyAcceptedOffers() {
        String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("AcceptedOffers");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot acceptedOfferId : snapshot.getChildren()) {
                    myAcceptedOffers.add(acceptedOfferId.getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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