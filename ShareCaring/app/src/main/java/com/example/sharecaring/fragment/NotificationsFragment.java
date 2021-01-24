package com.example.sharecaring.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharecaring.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    ArrayList<String> myOffersId = new ArrayList<>();
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String offerId, userThatAccepted, nameOfUser;
    LinearLayout layoutList;
    ArrayList<String> usersThatAccepted = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = v.findViewById(R.id.layout_list);
        getMyOffersId();
        getAcceptedOffers();
        return v;
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
                                    usersThatAccepted.add(userThatAccepted);
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
        //getNameOfUser();
        System.out.println("name of user outside: " + userThatAccepted);
        myNotificationTextView.setText(userThatAccepted + " accepted your offer");

        layoutList.addView(myNotificationView);

    }

    private void getNameOfUser() {
        //userRef = FirebaseDatabase.getInstance().getReference("Users");
        //userRef.child(userThatAccepted).g
        ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(userThatAccepted).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                nameOfUser = snapshot.child("firstName").getValue().toString();
                System.out.println(nameOfUser);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }
}
