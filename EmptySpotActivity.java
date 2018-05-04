package com.jonathan.idpdemo2;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class EmptySpotActivity extends AppCompatActivity {
    Fragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_spot);

        fragment = new MapFragment();
//        FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.add(R.id.fragmentId, fragment, "first");
//        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        ft.commit();
    }
}
