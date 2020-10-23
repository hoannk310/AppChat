package com.nkh.appchat;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String frLocation;
    private Double frlat;
    private Double frlng;

    private DatabaseReference locationRef;
    private FirebaseAuth auth;
    private String myId;
    double mylat, mylng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        Intent intent = getIntent();
        frLocation = intent.getStringExtra("location");
        String[] str = frLocation.split(",");
        frlat = Double.parseDouble(str[0]);
        frlng = Double.parseDouble(str[1]);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationRef = FirebaseDatabase.getInstance().getReference("Locations");
        auth = FirebaseAuth.getInstance();
        myId = auth.getCurrentUser().getUid();

        checkMyLocation();

    }

    private void checkMyLocation() {

        locationRef.child(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mylat = Double.parseDouble(snapshot.child("lat").getValue().toString());
                mylng = Double.parseDouble(snapshot.child("lng").getValue().toString());


                LatLng friendLocation = new LatLng(frlat, frlng);

                Location currentUser = new Location("");
                currentUser.setLatitude(mylat);
                currentUser.setLongitude(mylng);
                Location friendLoca = new Location("");
                friendLoca.setLatitude(frlat);
                friendLoca.setLongitude(frlng);
                distance(currentUser, friendLoca);

                mMap.addMarker(new MarkerOptions()
                        .position(friendLocation).title("Nơi đến")
                        .snippet("Khoảng cách " + new DecimalFormat("#.#")
                                .format(currentUser.distanceTo(friendLoca)/1000)+" km")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mylat,mylng),12.0f));

                 LatLng currentLat = new LatLng(mylat,mylng);
                 mMap.addMarker(new MarkerOptions().position(currentLat).title("Bạn"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private double distance(Location currentUser, Location friendLoca) {
        double theta = currentUser.getLongitude() - friendLoca.getLongitude();
        double dist = Math.sin(deg2rad(currentUser.getLatitude())) *
                Math.sin(deg2rad(friendLoca.getLatitude())) * Math.cos(deg2rad(currentUser.getLatitude())) * Math.cos(deg2rad(friendLoca.getLatitude())) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return dist;

    }

    private double rad2deg(double dist) {
        return (dist * 180 / Math.PI);
    }

    private double deg2rad(double latitude) {
        return (latitude * Math.PI / 180.0);
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

    }
}