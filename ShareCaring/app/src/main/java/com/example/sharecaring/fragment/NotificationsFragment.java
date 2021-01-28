package com.example.sharecaring.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Hashtable;

public class NotificationsFragment extends Fragment {

    ArrayList<String> myOffersId = new ArrayList<>();
    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String offerId, userThatAccepted;
    LinearLayout layoutList;
    ArrayList<String> usersThatAccepted = new ArrayList<>();
    Hashtable<String, String> userNames = new Hashtable<String, String>();
    Hashtable<String,String> userPhones = new Hashtable<>();
    ImageView imageCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notifications, container, false);
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        layoutList = v.findViewById(R.id.layout_list);
        getNamesOfAllUsers();
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
                                    userThatAccepted = userIdDb.getKey();
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


    public void createNotification() {
        final View myNotificationView = getLayoutInflater().inflate(R.layout.notification, null, false);
        TextView myNotificationTextView = (TextView)myNotificationView.findViewById(R.id.textViewSingleNotification);

        myNotificationTextView.setText(userNames.get(userThatAccepted) + " accepted your offer");

        imageCall = (ImageView)myNotificationView.findViewById(R.id.imageCall);
        imageCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall(myNotificationView);
            }

        });

        if(userPhones.get(userThatAccepted) != null) {
            myNotificationView.setTag(userPhones.get(userThatAccepted));
        } else myNotificationView.setTag("No phone number");

        layoutList.addView(myNotificationView);

    }

    public void getNamesOfAllUsers() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userId : snapshot.getChildren()) {
                    if(snapshot.exists()) {
                        String fn = userId.child("firstName").getValue().toString();
                        userNames.put(userId.getKey(), fn);
                        if(userId.child("phoneNumber").exists()) {
                            String phone = userId.child("phoneNumber").getValue().toString();
                            userPhones.put(userId.getKey(),phone);
                        }

                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void makeCall(View v) {
        String userPhone = v.getTag().toString();
        if(!userPhone.equals("No phone number")) {
            Uri number = Uri.parse("tel:"+userPhone);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            if(callIntent.resolveActivity(getContext().getPackageManager()) !=null){
                startActivity(callIntent);}
        } else {
            Toast.makeText(getContext(), "Wrong phone number", Toast.LENGTH_LONG).show();
        }
    }
}
