package com.example.sharecaring.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.sharecaring.model.UserCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    private static final int RC_CODE = 1001;
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
    List<String> myAcceptedOffers = new ArrayList<>();
    Hashtable<String, String> userNames = new Hashtable<String, String>();
    List<String> usersIds = new ArrayList<>();
    RadioButton myOffers, acceptedOffers;
    TextView noOffers;
    private ProgressDialog loadingBar;
    Button uploadBtn;

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
        uploadBtn = v.findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(this);
        profilePic = v.findViewById(R.id.profileImgOffer);

        layoutList = v.findViewById(R.id.layout_list);
        myOffers = v.findViewById(R.id.myOffersBtn);
        myOffers.setOnClickListener(this);
        acceptedOffers = v.findViewById(R.id.acceptedOffersBtn);
        acceptedOffers.setOnClickListener(this);
        noOffers = v.findViewById(R.id.noOffers);
        loadingBar = new ProgressDialog(getContext());

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
                    layoutList.removeAllViews();
                    offerSwitch.setVisibility(View.INVISIBLE);
                }
            }
        });

        loadData();
        getOffersAcceptedByMe("true");
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
                getActivity().finish();
                break;
            case R.id.uploadBtn:
                uploadFromGallery();
                break;
            case R.id.myOffersBtn:
                layoutList.removeAllViews();
                IntentOpener.openIntent(getActivity(), MyOffersActivity.class);
                acceptedOffers.setChecked(true);
                offerSwitch.setChecked(false);
                getOffersAcceptedByMe("true");
                break;
            case R.id.acceptedOffersBtn:
                layoutList.removeAllViews();
                offerSwitch.setChecked(false);
                getOffersAcceptedByMe("true");
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                //profilePhoto.setImageURI(imageUri);
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                uploadImageToDb(imageUri);
            }
        }
    }

    private void uploadImageToDb(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        String path = userId + "/profilePicture.jpg";
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference(path);
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Picasso.get().load(task.getResult()).into(profilePic);
                        Toast.makeText(getContext(), "Profile Image stored successfully...", Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Image upload failed, try again", Toast.LENGTH_LONG).show();
                loadingBar.dismiss();
            }
        });
    }

    private void uploadFromGallery() {
        //returns the particular image uri from the gallery that the user clicked on
        Intent openGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGallery, RC_CODE);
    }

    private void getOffersAcceptedByMe(final String offerType) {
        getNamesOfAllUsers();
        getMyAcceptedOffers(new UserCallback() {
            @Override
            public void onCallback(List<String> users) {
                final String userid = user.getUid();
                ref = FirebaseDatabase.getInstance().getReference("Offers");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = false;
                        for(DataSnapshot userIdDb : snapshot.getChildren()) {
                            System.out.println(userIdDb.getKey());
                            String firstName = userNames.get(userIdDb.getKey());
                            String uId = userIdDb.getValue().toString();
                            if(!userIdDb.getKey().equals(userid)) {

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
                                            exists = true;
                                            putDataToTextView(firstName, uId);
                                        }
                                    }
                                }

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
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
                    boolean exists = false;
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if (postSnapshot.child("isVolunteering").getValue().toString().equals(offerType)) {
                            offerId = postSnapshot.getKey();
                            address = postSnapshot.child("address").getValue().toString();
                            description = postSnapshot.child("description").getValue().toString();
                            animals = postSnapshot.child("animals").getValue().toString();
                            medication = postSnapshot.child("medication").getValue().toString();
                            shopping = postSnapshot.child("shopping").getValue().toString();
                            transport = postSnapshot.child("transport").getValue().toString();
                            exists = true;
                            putDataToTextView("", "");
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

    private void getMyAcceptedOffers(final UserCallback myCallback) {
        final String userid = user.getUid();

        ref = FirebaseDatabase.getInstance().getReference("AcceptedOffers");
        ref.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot acceptedOfferId : snapshot.getChildren()) {
                    if (!myAcceptedOffers.contains(acceptedOfferId.getValue().toString()))
                        myAcceptedOffers.add(acceptedOfferId.getValue().toString());
                }
                myCallback.onCallback(myAcceptedOffers);
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
