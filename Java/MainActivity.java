package com.jonathan.idpdemo2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    Button extendButton, checkEmptySpotButton, accountButton, signOutButton;
    TextView accountText;

    public SharedPreferences setting;
    public static String data = "DATA";
    public static String hasLoggedInField = "HASLOGGEDIN";
    public static String accountField = "ACCOUNT";
    public static String accountLogInField = "ACCOUNT LOGGED";
    public static String meterIDField = "METER ID";

    public static boolean hasAccountLoggedIn;
    boolean hasLoggedIn;
    boolean occupied;

    public static String accountNumber;
    private DatabaseReference mDatabase, nDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        extendButton = (Button) findViewById(R.id.extendButton);
        checkEmptySpotButton = (Button) findViewById(R.id.checkEmptySpotButton);
        accountButton = (Button) findViewById(R.id.accountButton);
        signOutButton = (Button) findViewById(R.id.signOutButton);
        accountText = (TextView) findViewById(R.id.accountText);

        extendButton.setOnClickListener(extend);
        checkEmptySpotButton.setOnClickListener(checkEmptySpot);
        accountButton.setOnClickListener(account);
        signOutButton.setOnClickListener(signOut);
        //hasAccountLoggedIn = false;

//        setting = getApplication().getSharedPreferences(data, 0);
//        SharedPreferences.Editor editor = setting.edit();
//        editor.putBoolean(MainActivity.hasLoggedInField,  false);
//        editor.commit();


    }

    View.OnClickListener extend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            setting = getApplication().getSharedPreferences(data, 0);
            hasLoggedIn =  setting.getBoolean(hasLoggedInField, false);


            Log.d("Logged In", hasLoggedIn + "");


            if(!hasLoggedIn){
                intent.setClass(MainActivity.this, ExtendActivity.class);
            }
            else if (hasLoggedIn){
                intent.putExtra("METER_ID",setting.getString(MainActivity.meterIDField,""));
                intent.setClass(MainActivity.this, ExtendActitvity2.class);
            }
            startActivity(intent);
        }
    };

    View.OnClickListener checkEmptySpot = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MapsActivity.class);
            startActivity(intent);

        }
    };

    View.OnClickListener account = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            Log.d("hasLoggedIn", hasAccountLoggedIn + "");
            if(hasAccountLoggedIn ){
                intent.setClass(MainActivity.this, BalanceActivity.class);
            }
            else{
                intent.setClass(MainActivity.this, AccountActivity.class);
            }
            startActivity(intent);

        }
    };

    View.OnClickListener signOut = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mDatabase.child(accountNumber).child("loggedIn").setValue(0);
            SharedPreferences.Editor editor = setting.edit();
            editor.putString(accountField, "");
            editor.commit();
            Toast.makeText(MainActivity.this, "Sign out", Toast.LENGTH_SHORT).show();
        }
    };

    public void onResume(){
        super.onResume();
        setting = getApplication().getSharedPreferences(data, 0);
        accountNumber = setting.getString(accountField, "");
        Log.d("Account Number", accountNumber);
        mDatabase = FirebaseDatabase.getInstance().getReference("account");
        if(!accountNumber.equals("")){
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(accountNumber).child("loggedIn").getValue().toString().equals("1")){
                        hasAccountLoggedIn = true;
                        accountText.setText("Welcome ! " + accountNumber);
                    }
                    else{
                        hasAccountLoggedIn = false;
                        accountText.setText("");
                    }

                    Log.d("hasLoggedIn", hasAccountLoggedIn + "");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }
}
