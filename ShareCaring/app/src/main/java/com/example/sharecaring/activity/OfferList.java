package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class OfferList extends AppCompatActivity {

    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;

    String description, address, medication, animals, shopping, transport,offerId;
    Button btnAccept;
    LinearLayout layoutList;
    Button chatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);
        layoutList = findViewById(R.id.layout_list);
        getOfferList();
    }

    private void getOfferList() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        final String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot userIdDb : snapshot.getChildren()) {
                        System.out.println(userIdDb.getKey());
                        if(!userIdDb.getKey().equals(userid))
                            for(DataSnapshot offerIdDb : userIdDb.getChildren()) {
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void putDataToTextView() {
        final View myOfferView = getLayoutInflater().inflate(R.layout.alloffers, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewSingleOfferList);
        myOfferTextView.setText(address + ' ' + description + ' ' + animals + ' ' + medication + ' ' + shopping + ' ' + medication);

        btnAccept = (Button)myOfferView.findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOffer(myOfferView);
            }
        });
        myOfferView.setTag(offerId);
        layoutList.addView(myOfferView);

        chatBtn = findViewById(R.id.btnChat);
        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OfferList.this, ChatActivity.class);
                intent.putExtra("receiverUid", user.getUid()); //im getting my id, insted of the offers author id
                startActivity(intent);
            }
        });

    }

    private void acceptOffer(View view) {
        layoutList.removeView(view);
    }
}