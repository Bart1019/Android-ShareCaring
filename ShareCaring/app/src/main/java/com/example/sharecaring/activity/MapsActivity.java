package com.example.sharecaring.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sharecaring.R;
import com.example.sharecaring.model.ChatConfig;
import com.example.sharecaring.model.IntentOpener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    public static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private FusedLocationProviderClient mFusedLocationClient;
    DatabaseReference ref;
    Geocoder geocoder;
    List<Address> addressList = new ArrayList<>();
    LatLng addressLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button buttonProfile = (Button)findViewById(R.id.btnProfile);
        buttonProfile.setOnClickListener(this);
        Button buttonOffersList = (Button)findViewById(R.id.btnOfferList);
        buttonOffersList.setOnClickListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //initialize comet chat
        ChatConfig.initCometChat(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ChatConfig.createUser(user.getUid(), user.getDisplayName());
        ChatConfig.loginToChat(user.getUid());
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnProfile) {
            IntentOpener.openIntent(MapsActivity.this, ProfileActivity.class);
        } else if(v.getId() == R.id.btnOfferList) {
            IntentOpener.openIntent(MapsActivity.this, OfferList.class);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        getCurrentLocation();
        //geocoder = new Geocoder(MapsActivity.this);
        getAddresses();
    }

    private void getAddresses() {
        /*ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userId : snapshot.getChildren()) {
                    for(DataSnapshot offerId : userId.getChildren()) {
                        String addressFromDb = offerId.child("address").getValue().toString();
                        try {
                            addressList = geocoder.getFromLocationName(addressFromDb,1);
                        } catch (IOException e) {
                            e.printStackTrace();
                            //Log.e(TAG, "getAddresses: IOException: " + e.getMessage());
                        }

                        if(addressList.size() > 0) {
                            Address address = addressList.get(0);
                            addressLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                            mMap.addMarker(new MarkerOptions()
                                .position(addressLatLng)
                                .title(offerId.child("description").getValue().toString())
                            );
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        */
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    private void getCurrentLocation() {
        //We have the permission
        Task<Location> locationTask = mFusedLocationClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }

}