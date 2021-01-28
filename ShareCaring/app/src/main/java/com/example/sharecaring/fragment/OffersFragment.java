package com.example.sharecaring.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

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
    Hashtable<String, String> userNames = new Hashtable<String, String>();
    Hashtable<String,String> userPhoneNumbers = new Hashtable<>();


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
        getNamesOfAllUsers();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        final String userid = user.getUid();
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userIdDb : snapshot.getChildren()) {
                    System.out.println(userIdDb.getKey());
                    String firstName = userNames.get(userIdDb.getKey());
                    String userPhoneNumber = userPhoneNumbers.get(userIdDb.getKey());

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
                                    putDataToTextView(firstName, userPhoneNumber);
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

    private void putDataToTextView(String fName, final String phone) {
        final View myOfferView = getLayoutInflater().inflate(R.layout.alloffers, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewAddress);
        myOfferTextView.setText(address + "\n" + description);
        TextView offerAuthorName = (TextView)myOfferView.findViewById(R.id.userName);
        offerAuthorName.setText(fName);

        ImageView imageAnimals = (ImageView)myOfferView.findViewById(R.id.first);
        ImageView imageMedication = (ImageView)myOfferView.findViewById(R.id.second);
        ImageView imageTransport = (ImageView)myOfferView.findViewById(R.id.third);
        ImageView imageShopping = (ImageView)myOfferView.findViewById(R.id.fourth);

        ImageView imageCall = (ImageView)myOfferView.findViewById(R.id.imageCall);
        imageCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall(phone);
            }

        });

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

            Log.d("lalala", "putDataToTextView: " + newDrawables.size());

            for (int i = 0; i < newDrawables.size(); i++) {
                images.get(i).setImageResource(newDrawables.get(i));
            }

        }

        btnAccept = (Button)myOfferView.findViewById(R.id.acceptBtn);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptOffer(myOfferView);
            }
        });
        myOfferView.setTag(offerId);
        layoutList.addView(myOfferView);
    }

    private void makeCall(String phone) {
        System.out.println("jestem w callu");
        if(!phone.equals("No phone number")) {
            Uri number = Uri.parse("tel:"+phone);
            Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
            if(callIntent.resolveActivity(getContext().getPackageManager()) !=null){
                startActivity(callIntent);}
        } else {
            Toast.makeText(getContext(), "Wrong phone number", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isCare(List<String> offersTypes) {
        for (String offer : offersTypes) {
            if (!offer.equals("true"))
                return false;
        }
        return true;
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
                            userPhoneNumbers.put(userId.getKey(),phone);
                        } else userPhoneNumbers.put(userId.getKey(), "No phone number");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
