package com.example.sharecaring.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.sharecaring.R;
import com.example.sharecaring.model.ClusterMarker;
import com.example.sharecaring.model.GeocodingLocation;
import com.example.sharecaring.util.MyClusterManagerRenderer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "MapsActivity";
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9002;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9003;
    private static final String MAP_FRAG_TAG = "Map";
    private static final String LIST_FRAG_TAG = "List";
    private static final int CARE = -1;
    private static final int TRANSPORT = 2;
    private static final int MEDICATION = 1;
    private static final int ANIMALS = 0;
    private static final int SHOPPING = 3;


    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    boolean mLocationPermissionGranted = false;

    DatabaseReference ref;
    Geocoder geocoder;
    String addressLatLng;
    Switch offersSwitch, mapSwitch;

    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private List<ClusterMarker> mClusterMarkers = new ArrayList<>();
    Hashtable<String, String> userNames = new Hashtable<String, String>();
    MapFragment mapFragment;
    OffersFragment offersFragment;
    FragmentManager fm;

    private EditText mSearch;
    RelativeLayout searchLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
        // initiate a Switch
        offersSwitch = (Switch) v.findViewById(R.id.volunteersSwitcher);
        mapSwitch = (Switch) v.findViewById(R.id.mapSwitcher);
        offersSwitch.setChecked(false);
        mapFragment = new MapFragment();
        offersFragment = new OffersFragment();
        mapSwitch.setOnCheckedChangeListener(this);
        fm = getFragmentManager();
        mSearch = v.findViewById(R.id.inputSearch);
        searchLayout = v.findViewById(R.id.relLayout1);

        offersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {  //on means needs
                    truncateMarkers();
                    getAddresses("false");
                } else {
                    truncateMarkers();
                    getAddresses("true");
                }
            }
        });
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        return v;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        OffersFragment offersFragment = new OffersFragment();
        SupportMapFragment mMapFragment;

        if(null == fm) {return;}

        if (b) {
            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mMapFragment.getView().setVisibility(View.INVISIBLE);
            offersSwitch.setVisibility(View.INVISIBLE);
            searchLayout.setVisibility(View.INVISIBLE);

            fm.beginTransaction()
                    .add(R.id.child_fragment_container, offersFragment, LIST_FRAG_TAG)
                    .show(offersFragment)
                    .commit();
        } else {
            offersSwitch.setVisibility(View.VISIBLE);
            searchLayout.setVisibility(View.VISIBLE);

            OffersFragment offerHideFragment = (OffersFragment) fm.findFragmentByTag(LIST_FRAG_TAG);
            fm.beginTransaction()
                    .hide(offerHideFragment)
                    .commit();

            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mMapFragment.getView().setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEARCH
                        || i == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    Log.d(TAG, "onEditorAction: " + "before geolocate");
                    //execute method for searching
                    geolocate();
                }
                return false;
            }
        });
    }

    private void geolocate() {
        Log.d(TAG, "geolocate: " + "geolocating");

        String searchString = mSearch.getText().toString();
        Geocoder g = new Geocoder(getActivity());
        List<Address> addresses = new ArrayList<>();

        try {
            addresses = g.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geolocate: " + e.getMessage() );
        }

        if (addresses.size() > 0) {
            Address address = addresses.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f);
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkMapServices()) {
            if (!mLocationPermissionGranted) {
                getLocationPermission();
            }
        }
    }

    private boolean checkMapServices() {
        if (isMapsEnabled()) {
            return true;
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //user accepted the permissions?
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if (!mLocationPermissionGranted) {
                    getLocationPermission();
                }
            }
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
        init();
        //getCurrentLocation();
        geocoder = new Geocoder(getContext());
        Log.d(TAG, "onMapReady: " + geocoder.isPresent());
        getAddresses("true");
    }

    private void getCurrentLocation() {
        //We have the permission
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    LatLng latLng = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });
    }

    private void getAddresses(final String markerKinds) {
        getNamesOfAllUsers();
        ref = FirebaseDatabase.getInstance().getReference("Offers");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userId : snapshot.getChildren()) {
                    String firstName = userNames.get(userId.getKey());

                    for (DataSnapshot offerId : userId.getChildren()) {
                        String addressFromDb = offerId.child("address").getValue().toString();
                        int offerType = 0;
                        List<String> offerTypes = new ArrayList<>();
                        offerTypes.add(offerId.child("animals").getValue().toString());
                        offerTypes.add(offerId.child("medication").getValue().toString());
                        offerTypes.add(offerId.child("transport").getValue().toString());
                        offerTypes.add(offerId.child("shopping").getValue().toString());

                        int count = 0;
                        for (String offer : offerTypes) {
                            if (offer.equals("true")) {
                                count++;
                            }
                        }

                        if (count > 1) {    //if the offer has multiple types
                            offerType = CARE;  //care
                        } else {
                            for (int i = 0; i < offerTypes.size(); i++) {
                                if (offerTypes.get(i).equals("true")) {
                                    offerType = i;
                                    break;
                                }
                            }
                        }

                        if (offerId.child("isVolunteering").getValue().toString().equals(markerKinds)) {
                            GeocodingLocation locationAddress = new GeocodingLocation();
                            locationAddress.getAddressFromLocation(addressFromDb,
                                    getContext(), new GeocoderHandler(firstName, offerId.child("description").getValue().toString(), offerType));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void truncateMarkers() {
        for (ClusterMarker marker : mClusterMarkers) {
            mClusterManager.removeItem(marker);
        }
        mClusterMarkers.clear();
        mClusterManager.cluster();
    }

    private class GeocoderHandler extends Handler {
        private String description;
        private String firstName;
        private int img;

        public GeocoderHandler(String firstName, String description, int img) {
            this.firstName = firstName;
            this.description = description;
            this.img = img;
        }

        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    break;
                default:
                    locationAddress = null;
            }
            if (locationAddress != null) {
                addressLatLng = locationAddress;
                addMarkers(addressLatLng, description, firstName, img);
            }
        }

        private void addMarkers(String latLng, String description, String firstName, int img) {
            if (mMap != null) {
                String[] splitStr = latLng.trim().split("\\s+");

                if (mClusterManager == null) {
                    mClusterManager = new ClusterManager<>(getActivity().getApplicationContext(), mMap);
                }

                if (mClusterManagerRenderer == null) {
                    mClusterManagerRenderer = new MyClusterManagerRenderer(
                            getActivity(),
                            mMap,
                            mClusterManager
                    );
                    mClusterManager.setRenderer(mClusterManagerRenderer);
                }
                try {
                    String snippet = description;

                    int avatar = R.drawable.care; // set the default avatar
                    switch (img) {
                        case CARE:
                            avatar = R.drawable.care;
                            break;
                        case TRANSPORT:
                            avatar = R.drawable.car;
                            break;
                        case MEDICATION:
                            avatar = R.drawable.medicine;
                            break;
                        case ANIMALS:
                            avatar = R.drawable.dog;
                            break;
                        case SHOPPING:
                            avatar = R.drawable.groceries;
                            break;

                    }

                    try {
                        ClusterMarker newClusterMarker = new ClusterMarker(
                                new LatLng(Double.parseDouble(splitStr[0]), Double.parseDouble(splitStr[1])),
                                firstName,
                                snippet,
                                avatar
                        );
                        mClusterManager.addItem(newClusterMarker);
                        mClusterMarkers.add(newClusterMarker);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }


                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }
                mClusterManager.cluster();
            }
        }
    }
}
