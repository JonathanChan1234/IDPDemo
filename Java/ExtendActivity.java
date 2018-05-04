package com.jonathan.idpdemo2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExtendActivity extends AppCompatActivity {

    Button submitButton, scanQRButton;
    EditText MeterIDText;
    EditText verificationCodeText;
    TextView warningText;
    TextView warningDialogText;

    final Context context = this;
    Activity activity;

    private DatabaseReference mDatabase, accountDatabase;

    String meterId = "0";
    String accountNumber;
    int balance;
    int remaining;
    String startTimeText;
    String resultText;
    String verificationText;

    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend);
        activity = this;

        submitButton = (Button) findViewById(R.id.SubmitButton);
        submitButton.setOnClickListener(submit);

        scanQRButton = (Button) findViewById(R.id.scanQRButton);
        scanQRButton.setOnClickListener(scanQR);

        MeterIDText = (EditText) findViewById(R.id.MeterIDText);
        warningText = (TextView) findViewById(R.id.warningText);
    }

    View.OnClickListener scanQR = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            intentIntegrator.setPrompt("Scan");
            intentIntegrator.setCameraId(0);
            intentIntegrator.setBeepEnabled(false);
            intentIntegrator.setBarcodeImageEnabled(false);
            intentIntegrator.initiateScan();
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode, data);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            }
            else{
                //Get meter Id and verification code
                resultText = result.getContents();
                meterId = resultText.substring(0,4);
                verificationText = resultText.substring(5, 8);

                //Initialize database
                mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
                accountDatabase = FirebaseDatabase.getInstance().getReference("account");

                //Retrieve data from the meter
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        remaining = Integer.parseInt(dataSnapshot.child("duration").getValue().toString());
                        startTimeText = dataSnapshot.child("date").getValue().toString();

                        accountDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                balance = Integer.parseInt(dataSnapshot.child(MainActivity.accountNumber).child("balance").getValue().toString());
                                balance = balance - remaining;
                                if(balance >= 0){
                                    Toast.makeText(ExtendActivity.this,"You have paid $" + remaining + " Account Remaining: $" + balance, Toast.LENGTH_SHORT).show();

                                    //Update app internal data
                                    SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                    SharedPreferences.Editor editor = setting.edit();
                                    editor.putBoolean(MainActivity.hasLoggedInField, true);
                                    editor.putString(MainActivity.meterIDField, meterId);
                                    editor.commit();

                                    //Update server
                                    mDatabase.child("verification").setValue(0);
                                    mDatabase.child("paid").setValue(1);

                                    //Update balance information
                                    Date startTime = Calendar.getInstance().getTime();
                                    try{
                                        startTime = format.parse(startTimeText);
                                    }
                                    catch(Exception ParseException){
                                        ParseException.printStackTrace();
                                    }
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTime(startTime);
                                    calendar.add(Calendar.SECOND, remaining);
                                    Date endTime = calendar.getTime();
                                    String endTimeText = format2.format(endTime);
                                    accountDatabase.child(MainActivity.accountNumber).child("balance").setValue(balance);
                                    accountDatabase.child(MainActivity.accountNumber).child("currentParkingRecord").setValue("Meter Id: " + meterId + " Will be expired on: " + endTimeText);

                                    //Jump to next activity
                                    Intent intent = new Intent();
                                    intent.putExtra("METER_ID", meterId);
                                    intent.setClass(ExtendActivity.this, ExtendActitvity2.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }

                                else{ //Not enough money
                                    Toast.makeText(ExtendActivity.this, "Not enough Money in the account, please try again", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.setClass(ExtendActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    View.OnClickListener submit = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
                    meterId = MeterIDText.getText().toString();
                    try{
                        mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
                        accountDatabase = FirebaseDatabase.getInstance().getReference("account");
                        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                if(dataSnapshot.child("duration").getValue() != null){  // Meter Id is found
                                    if(dataSnapshot.child("verification").getValue().toString() == "0"){    //The parking period is currently expired
                                        warningText.setText("The parking period is currently expired");
                                    }
                                    else{   //The parking period is not expired
                                        Log.d("Firebase", dataSnapshot.child("duration").getValue().toString() + "");
                                        final Dialog dialog = new Dialog(context);
                                        //dialog.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
                                        dialog.setContentView(R.layout.dialoglayout);
                                        dialog.setTitle(R.string.dialogTitle);
                                        verificationCodeText = (EditText) dialog.findViewById(R.id.verificationCodeText);
                                        warningDialogText = (TextView) dialog.findViewById(R.id.warningTextDialog);
                                        Button confirmButton = (Button) dialog.findViewById(R.id.confirmButton);
                                        confirmButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String verificationCode =  verificationCodeText.getText().toString();
                                                if(TextUtils.isEmpty(verificationCode)){
                                                    warningDialogText.setText("Verification Code cannot be leave blank");
                                                }
                                                else{
                                                    if(verificationCode.equals(dataSnapshot.child("verification").getValue().toString())){
                                                        Log.d("Verification", "Success");
                                                        dialog.dismiss();

                                                        remaining = Integer.parseInt(dataSnapshot.child("duration").getValue().toString());
                                                        startTimeText = dataSnapshot.child("date").getValue().toString();

                                                        accountDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                balance = Integer.parseInt(dataSnapshot.child(MainActivity.accountNumber).child("balance").getValue().toString());
                                                                balance = balance - remaining;
                                                                if(balance >= 0){
                                                                    Toast.makeText(ExtendActivity.this,"You have paid $" + remaining + " Account Remaining: $" + balance, Toast.LENGTH_SHORT).show();

                                                                    //Update app internal data
                                                                    SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                                                    SharedPreferences.Editor editor = setting.edit();
                                                                    editor.putBoolean(MainActivity.hasLoggedInField, true);
                                                                    editor.putString(MainActivity.meterIDField, meterId);
                                                                    editor.commit();

                                                                    //Update server
                                                                    mDatabase.child("verification").setValue(0);
                                                                    mDatabase.child("paid").setValue(1);

                                                                    //Update balance information
                                                                    Date startTime = Calendar.getInstance().getTime();
                                                                    try{
                                                                        startTime = format.parse(startTimeText);
                                                                    }
                                                                    catch(Exception ParseException){
                                                                        ParseException.printStackTrace();
                                                                    }
                                                                    Calendar calendar = Calendar.getInstance();
                                                                    calendar.setTime(startTime);
                                                                    calendar.add(Calendar.SECOND, remaining);
                                                                    Date endTime = calendar.getTime();
                                                                    String endTimeText = format2.format(endTime);
                                                                    accountDatabase.child(MainActivity.accountNumber).child("balance").setValue(balance);
                                                                    accountDatabase.child(MainActivity.accountNumber).child("currentParkingRecord").setValue("Meter Id: " + meterId + " Will be expired on: " + endTimeText);

                                                                    //Jump to next activity
                                                                    Intent intent = new Intent();
                                                                    intent.putExtra("METER_ID", meterId);
                                                                    intent.setClass(ExtendActivity.this, ExtendActitvity2.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    startActivity(intent);
                                                                }

                                                                else{ //Not enough money
                                                                    Toast.makeText(ExtendActivity.this, "Not enough Money in the account, please try again", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent();
                                                                    intent.setClass(ExtendActivity.this, MainActivity.class);
                                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    startActivity(intent);
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }
                                                    else{
                                                        warningDialogText.setText("Wrong Verification Code");
                                                        Log.d("Verification", "Fail");
                                                    }
                                                }
                                            }
                                        });

                                        dialog.show();
                                    }
                                }

                                else{   //Meter Id is not found
                                    Toast.makeText(getApplicationContext(), "No Meter with this ID found", Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {  //Access Error
                                warningText.setText("Database Access Error (Proposed Solution: Check your Internet Connection");
                                Log.d("Firebase", "Fail");
                            }
                        });
                    }
                    catch(Exception e){ //Access Error
                        warningText.setText("Database Access Error (Proposed Solution: Check your Internet Connection");
                        Log.d("Firebase", "Fail");
                    }



        }


    };
}
