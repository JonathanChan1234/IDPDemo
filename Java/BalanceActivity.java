package com.jonathan.idpdemo2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;

public class BalanceActivity extends AppCompatActivity {
    TextView balanceText, recordText;
    private DatabaseReference mDatabase;
    String balance;
    String accountNumber;
    Button depositButton;
    String record;

    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        balanceText = (TextView) findViewById(R.id.balanceText);
        recordText = (TextView) findViewById(R.id.recordText);
        depositButton = (Button) findViewById(R.id.addBalanceButton);
    }

    public void onResume(){
        super.onResume();
        SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
        accountNumber = setting.getString(MainActivity.accountField, "");
        if(accountNumber.equals("")){
            balanceText.setText("Error (Please try to log out and log in again");
        }
        mDatabase = FirebaseDatabase.getInstance().getReference("account");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                balance = dataSnapshot.child(accountNumber).child("balance").getValue().toString();
                balanceText.setText("$" + balance);
                record = dataSnapshot.child(accountNumber).child("currentParkingRecord").getValue().toString();
                recordText.setText(record);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        depositButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newBalance = Integer.parseInt(balance) + 50;
                mDatabase.child(accountNumber).child("balance").setValue(newBalance);
            }
        });
    }

    public void onBackPressed(){
        Intent intent = new Intent();
        intent.setClass(BalanceActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



}
