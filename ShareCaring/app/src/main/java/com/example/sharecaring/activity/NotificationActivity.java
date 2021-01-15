package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sharecaring.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    ArrayList<String> myOffersId = new ArrayList<>();
    DatabaseReference ref, userRef;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String offerId, userThatAccepted, nameOfUser;
    LinearLayout layoutList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = findViewById(R.id.layout_list);
        getMyOffersId();
        getAcceptedOffers();
    }

    private void getMyOffersId() {
        String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers").child(userid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    offerId = postSnapshot.getKey();
                    myOffersId.add(offerId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getAcceptedOffers() {
        final String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("AcceptedOffers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userIdDb : snapshot.getChildren()) {
                    if(!userIdDb.getKey().equals(userid))
                        for(DataSnapshot acceptedOfferIdDb : userIdDb.getChildren()) {
                            for (String myOfferId : myOffersId) {
                                if(acceptedOfferIdDb.getValue().equals(myOfferId)) {
                                    System.out.println("elooooooss");
                                    userThatAccepted = userIdDb.getKey();
                                    System.out.println(userThatAccepted);
                                    createNotification();
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


    private void createNotification() {
        final View myNotificationView = getLayoutInflater().inflate(R.layout.notification, null, false);
        TextView myNotificationTextView = (TextView)myNotificationView.findViewById(R.id.textViewSingleNotification);
        getNameOfUser();
        System.out.println("name of user outside: " + nameOfUser);
        myNotificationTextView.setText(nameOfUser + " accepted your offer");

        layoutList.addView(myNotificationView);
    }

    private void getNameOfUser() {
        //userRef = FirebaseDatabase.getInstance().getReference("Users");
        //userRef.child(userThatAccepted).g
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.child(userThatAccepted).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    nameOfUser = snapshot.child("firstName").getValue().toString();
                    System.out.println(nameOfUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

}