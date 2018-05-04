package com.jonathan.idpdemo2;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    DatabaseReference mDatabase19, mDatabase20;
    String occupied1, occupied2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(22.283791, 114.133282);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        CameraPosition cameraPosition=
                new CameraPosition.Builder()
                        .target(sydney)
                        .zoom(17)
                        .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 4));
    }

    public void onResume(){
        super.onResume();
        mDatabase19 = FirebaseDatabase.getInstance().getReference("0019");
        mDatabase20 = FirebaseDatabase.getInstance().getReference("0020");

        mDatabase19.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                occupied1 = dataSnapshot.child("occupied").getValue().toString();
                if(occupied1.equals("0")){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(22.283655, 114.133127)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                else{
                    mMap.addMarker(new MarkerOptions().position(new LatLng(22.283655, 114.133127)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase20.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                occupied2 = dataSnapshot.child("occupied").getValue().toString();
                if(occupied2.equals("0")){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(22.283337, 114.132824)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                else{
                    mMap.addMarker(new MarkerOptions().position(new LatLng(22.283337, 114.132824)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
