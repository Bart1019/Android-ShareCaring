package com.example.sharecaring.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.sharecaring.R;
import com.example.sharecaring.activity.EditProfileActivity;
import com.example.sharecaring.activity.MyOffersActivity;
import com.example.sharecaring.activity.StartActivity;
import com.example.sharecaring.model.IntentOpener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener{

    public static final int EDIT_STATUS_CODE = 1000;
    TextView editTextFirstName, editTextEmail;
    ImageButton btnLogOut, btnEditProfile;

    DatabaseReference ref;
    FirebaseUser user;
    FirebaseAuth mAuth;
    String firstNameFromDB, lastNameFromDB, emailFromDB;
    CircularImageView profilePic, offersProfile;
    StorageReference storageReference;

    String description, address, medication, animals, shopping, transport, offerId;
    LinearLayout layoutList;
    ImageView imageClose;
    Switch offerSwitch;
    ArrayList<String> myAcceptedOffers = new ArrayList<>();
    Hashtable<String, String> userNames = new Hashtable<String, String>();
    List<String> usersIds = new ArrayList<>();
    RadioButton myOffers, acceptedOffers;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        //initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        editTextFirstName = (TextView) v.findViewById(R.id.editProfileTextFirstName);
        editTextEmail = (TextView) v.findViewById(R.id.editProfileTextEmail);

        btnLogOut = v.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(this);
        btnEditProfile = v.findViewById(R.id.editBtn);
        btnEditProfile.setOnClickListener(this);
        profilePic = v.findViewById(R.id.profileImgOffer);

        layoutList = v.findViewById(R.id.layout_list);
        getMyAcceptedOffers();
        getOffersAcceptedByMe("true");

        myOffers = v.findViewById(R.id.myOffersBtn);
        myOffers.setOnClickListener(this);
        acceptedOffers = v.findViewById(R.id.acceptedOffersBtn);
        acceptedOffers.setOnClickListener(this);

        offerSwitch = v.findViewById(R.id.volunteersSwitcher);
        offerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (acceptedOffers.isChecked()) {
                    if (isChecked) {  //on means needs
                        layoutList.removeAllViews();
                        getOffersAcceptedByMe("false");
                    } else {
                        layoutList.removeAllViews();
                        getOffersAcceptedByMe("true");
                    }
                } else if (myOffers.isChecked()) {
                    if (isChecked) {  //on means needs
                        layoutList.removeAllViews();
                        getMyOffers("false");
                    } else {
                        layoutList.removeAllViews();
                        getMyOffers("true");
                    }
                }

            }
        });

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
            Picasso.get().load(uri).into(profilePic);
        }

        downloadProfilePic(userid, profilePic);
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

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnLogOut:
                mAuth.signOut();
                IntentOpener.openIntent(getActivity(), StartActivity.class);
                //finish();
                break;
            case R.id.editBtn:
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivityForResult(intent, EDIT_STATUS_CODE);
                //IntentOpener.openIntent(getActivity(), EditProfileActivity.class);
                break;
            case R.id.myOffersBtn:
                //IntentOpener.openIntent(getActivity(), MyOffersActivity.class);
                layoutList.removeAllViews();
                offerSwitch.setChecked(false);
                getMyOffers("true");
                break;
            case R.id.acceptedOffersBtn:
                //IntentOpener.openIntent(getActivity(), MyOffersActivity.class);
                layoutList.removeAllViews();
                offerSwitch.setChecked(false);
                getOffersAcceptedByMe("true");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == EDIT_STATUS_CODE) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.detach(this).attach(this).commit();
        }
    }

    /*private void getMyOffers(final String offerType) {
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
                    String uId = userIdDb.getValue().toString();
                    if(!userIdDb.getKey().equals(userid))
                        for(DataSnapshot offerIdDb : userIdDb.getChildren()) {
                                if (offerIdDb.child("isVolunteering").getValue().toString().equals(offerType)) {
                                    offerId = offerIdDb.getKey();
                                    address = offerIdDb.child("address").getValue().toString();
                                    description = offerIdDb.child("description").getValue().toString();
                                    animals = offerIdDb.child("animals").getValue().toString();
                                    medication = offerIdDb.child("medication").getValue().toString();
                                    shopping = offerIdDb.child("shopping").getValue().toString();
                                    transport = offerIdDb.child("transport").getValue().toString();
                                    putDataToTextView(firstName, uId);
                                }

                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }*/

    private void getOffersAcceptedByMe(final String offerType) {
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
                    String uId = userIdDb.getValue().toString();
                    if(!userIdDb.getKey().equals(userid))
                        for(DataSnapshot offerIdDb : userIdDb.getChildren()) {
                            for(String offId : myAcceptedOffers) {
                                if(offId.equals(offerIdDb.getKey())
                                        && offerIdDb.child("isVolunteering").getValue().toString().equals(offerType)) {
                                    offerId = offerIdDb.getKey();
                                    address = offerIdDb.child("address").getValue().toString();
                                    description = offerIdDb.child("description").getValue().toString();
                                    animals = offerIdDb.child("animals").getValue().toString();
                                    medication = offerIdDb.child("medication").getValue().toString();
                                    shopping = offerIdDb.child("shopping").getValue().toString();
                                    transport = offerIdDb.child("transport").getValue().toString();
                                    putDataToTextView(firstName, uId);
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


    public void getMyOffers(final String offerType) {
        getNamesOfAllUsers();
        final String userid = user.getUid();

            ref = FirebaseDatabase.getInstance().getReference("Offers");
            ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if (postSnapshot.child("isVolunteering").getValue().toString().equals(offerType)) {
                            offerId = postSnapshot.getKey();
                            address = postSnapshot.child("address").getValue().toString();
                            description = postSnapshot.child("description").getValue().toString();
                            animals = postSnapshot.child("animals").getValue().toString();
                            medication = postSnapshot.child("medication").getValue().toString();
                            shopping = postSnapshot.child("shopping").getValue().toString();
                            transport = postSnapshot.child("transport").getValue().toString();
                            putDataToTextView("", "");
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

    }

    private void putDataToTextView(String fName, String profile) {
        final View myOfferView = getLayoutInflater().inflate(R.layout.offer, null, false);
        TextView myOfferTextView = (TextView)myOfferView.findViewById(R.id.textViewAddress);
        myOfferTextView.setText(address +"\n" +description);
        TextView name = (TextView)myOfferView.findViewById(R.id.userName);
        if (fName.equals("")) {
            name.setText(firstNameFromDB);
        } else {
            name.setText(fName);
        }

        ImageView profilePhoto = (ImageView)myOfferView.findViewById(R.id.profileImgOffer);
        if (profile.equals("")) {
            downloadProfilePic(user.getUid(), profilePhoto);  //mine
        } else {
            downloadProfilePic(profile, profilePhoto);   //other
        }

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

    public void getNamesOfAllUsers() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userId : snapshot.getChildren()) {
                    if(snapshot.exists()) {
                        String fn = userId.child("firstName").getValue().toString();
                        userNames.put(userId.getKey(), fn);
                        usersIds.add(userId.getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
