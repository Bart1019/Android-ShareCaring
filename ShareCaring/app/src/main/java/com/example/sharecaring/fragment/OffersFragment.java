package com.example.sharecaring.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sharecaring.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OffersFragment extends Fragment {

    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String description, address, medication, animals, shopping, transport,offerId;
    Boolean isAccepted;
    Button btnAccept;
    LinearLayout layoutList;
    Button chatBtn;
    Switch offersSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_offers, container, false);
        layoutList = v.findViewById(R.id.layout_list);

        offersSwitch = (Switch) v.findViewById(R.id.volunteersSwitcher);

        offersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  //on means needs
                    layoutList.removeAllViews();
                    getOfferList("false");
                } else {
                    layoutList.removeAllViews();
                    getOfferList("true");
                }
            }
        });

        offersSwitch.setChecked(false);
        getOfferList("true");
        return v;
    }

    private void getOfferList(final String offerType) {
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
                            if(offerIdDb.child("isAccepted").getValue().toString().equals("false")) {
                                if (offerIdDb.child("isVolunteering").getValue().toString().equals(offerType)) {
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

    private void putDataToTextView() {
        final View myOfferView = getLayoutInflater().inflate(R.layout.alloffers, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewSingleOfferList);
        myOfferTextView.setText(address + "\n" + description);

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


        btnAccept = (Button)myOfferView.findViewById(R.id.btnAccept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOffer(myOfferView);
            }
        });
        myOfferView.setTag(offerId);
        layoutList.addView(myOfferView);
    }

    private void acceptOffer(View view) {
        layoutList.removeView(view);
        offerId = view.getTag().toString();

        markIsAccepted();
        //String userId = user.getUid();
        //ref = FirebaseDatabase.getInstance().getReference("Offers/"+ userId + "/" + offerId).child("isAccepted");
        //ref.setValue(true);
    }

    private void markIsAccepted() {
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userIdDb : snapshot.getChildren()) {
                    for(DataSnapshot offerIdDb : userIdDb.getChildren()) {
                        if(offerIdDb.getKey().equals(offerId)) {
                            DatabaseReference offerRef = offerIdDb.child("isAccepted").getRef();
                            offerRef.setValue(true);

                            //offerRef = FirebaseDatabase.getInstance().getReference("AcceptedOffers");
                            FirebaseDatabase.getInstance().getReference("AcceptedOffers")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    //.push() allows to save data without overriding
                                    .push().setValue(offerId).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Offer has been accepted", Toast.LENGTH_LONG).show();
                                    }else {
                                        Toast.makeText(getActivity(), "Failed to accept offer", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
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
