package com.jonathan.idpdemo2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ExtendActitvity2 extends AppCompatActivity {
    String meterId;
    Button addButton, minusButton, confirmExtendButton, exitButton;
    TextView carSpotText, extendPeriodText, remainingTimeText, warningExtendText;
    String startTimeText;
    int duration;
    int secondDifference;
    private DatabaseReference mDatabase, accountDatabase;
    int remaining;
    int balance;



    String[] durationTime = {"00:00", "00:15", "00:30", "00:45", "01:00", "01:15", "01:30", "01:45", "02:00"};
    int durationTimeSelected = 0;
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend_actitvity2);
        addButton = (Button) findViewById(R.id.addTimeButton);
        minusButton = (Button) findViewById(R.id.minusTimeButton);
        confirmExtendButton = (Button) findViewById(R.id.confrimExtendButton);
        exitButton = (Button) findViewById(R.id.exitButton);

        addButton.setOnClickListener(addTime);
        minusButton.setOnClickListener(minusTime);
        confirmExtendButton.setOnClickListener(confirmExtend);
        exitButton.setOnClickListener(exit);

        extendPeriodText = (TextView) findViewById(R.id.extendPeriodText);
        carSpotText = (TextView) findViewById(R.id.carSpotText);
        remainingTimeText = (TextView) findViewById(R.id.remainingTimeText);
        warningExtendText = (TextView) findViewById(R.id.warningExtendText);

    }

    View.OnClickListener addTime = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            durationTimeSelected= (durationTimeSelected + 1 ) % 9;
            extendPeriodText.setText(durationTime[durationTimeSelected]);
        }
    };

    View.OnClickListener minusTime = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            durationTimeSelected--;
            if(durationTimeSelected < 0){
                durationTimeSelected = 8;
            }
            extendPeriodText.setText(durationTime[durationTimeSelected]);

        }
    };

    View.OnClickListener exit = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
            SharedPreferences.Editor editor = setting.edit();
            editor.putString(MainActivity.meterIDField, "");
            editor.putBoolean(MainActivity.hasLoggedInField, false);
            editor.commit();

            Intent intent = new Intent();
            intent.setClass(ExtendActitvity2.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    };

    View.OnClickListener confirmExtend = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
            accountDatabase = FirebaseDatabase.getInstance().getReference("account");

            if(MainActivity.hasAccountLoggedIn){
                Log.d("Duration time selected", durationTimeSelected + "");
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("duration").getValue() != null){
                            if(!dataSnapshot.child("duration").getValue().toString().equals("0")){
                                int tryremaining = 0;
                                Log.d("remaining", remaining + "");
                                if(remaining <= 15){
                                    tryremaining = durationTimeSelected * 15;
                                }
                                else{
                                    tryremaining = remaining + durationTimeSelected * 15;
                                }
                                Log.d("Tryremaining", tryremaining + "");

                                if(tryremaining != 0) {

                                    if (tryremaining <= 120) {
                                        remaining = tryremaining;
                                    } else {
                                        remaining = 120;
                                    }
                                }

                                try{
                                    final Date currentTime = Calendar.getInstance().getTime();
                                    final String currentTimeText = format.format(currentTime);


                                    accountDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            balance = Integer.parseInt(dataSnapshot.child(MainActivity.accountNumber).child("balance").getValue().toString());
                                            balance = balance - (durationTimeSelected*15);
                                            if(balance < 0){
                                                Toast.makeText(ExtendActitvity2.this, "Not enough money", Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                                SharedPreferences.Editor editor = setting.edit();
                                                editor.putString(MainActivity.meterIDField, "");
                                                editor.putBoolean(MainActivity.hasLoggedInField, false);
                                                editor.commit();

                                                accountDatabase.child(MainActivity.accountNumber).child("balance").setValue(balance);
                                                Log.d("balance1", balance + "");
                                                Toast.makeText(ExtendActitvity2.this, "Extend successfully, You have paid $" + (durationTimeSelected*15), Toast.LENGTH_SHORT).show();
                                                mDatabase.child("verification").setValue(0);
                                                mDatabase.child("date").setValue(currentTimeText);
                                                mDatabase.child("duration").setValue(remaining);
                                                mDatabase.child("extended").setValue(1);

                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(currentTime);
                                                calendar.add(Calendar.SECOND, remaining);
                                                Date endTime = calendar.getTime();

                                                accountDatabase.child(MainActivity.accountNumber).child("currentParkingRecord").setValue("Meter ID: " + meterId + " Will be expired on: " + format2.format(endTime));

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    Log.d("balance2", balance + "");





                                }
                                catch(Exception e){
                                    Toast.makeText(ExtendActitvity2.this, "Please check your Internet Connection", Toast.LENGTH_LONG).show();
                                    Log.d("Access", "Fail");
                                }
                            }
                            else{
                                warningExtendText.setText("The parking period is currently expired, Click back to exit");

                                SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putBoolean(MainActivity.hasLoggedInField, false);
                                editor.putString(MainActivity.meterIDField, "");
                                editor.commit();
                                confirmExtendButton.setEnabled(false);

                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            else{
                Toast.makeText(ExtendActitvity2.this, "Please log in your account", Toast.LENGTH_SHORT).show();
            }

            Intent intent = new Intent();
            intent.setClass(ExtendActitvity2.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(ExtendActitvity2.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onResume(){
        super.onResume();
        confirmExtendButton.setEnabled(true);
        Intent intent = getIntent();
        meterId = intent.getStringExtra("METER_ID");
        carSpotText.setText("Car Spot ID: " + meterId);
        try{
            mDatabase = FirebaseDatabase.getInstance().getReference(meterId);
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child("date").getValue() != null){
                        startTimeText = dataSnapshot.child("date").getValue().toString();
                        Log.d("startTime", startTimeText);
                        duration = Integer.parseInt(dataSnapshot.child("duration").getValue().toString());
                        Log.d("Counter", duration + "");
                        Date startTime;
                        try{
                            startTime = format.parse(startTimeText);

                        }
                        catch(Exception ParseException){
                            startTime =Calendar.getInstance().getTime();
                            Log.d("Parsing Error", "Unable to parse the value");
                        }
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(startTime);
                        calendar.add(Calendar.SECOND, duration);
                        Date endTime = calendar.getTime();
                        Date currentTime = Calendar.getInstance().getTime();
                        Log.d("Current Time", format.format(currentTime));
                        Log.d("EndTime", format.format(endTime));
                        long difference = endTime.getTime() - currentTime.getTime();
                        secondDifference = (int) (difference/1000);
                        Log.d("Time Difference", secondDifference + "");
                        new CountDownTimer(secondDifference*1000, 1000) {
                            @Override
                            public void onTick(long l) {
                                remaining = (int) l/1000;
                                remainingTimeText.setText("Remaining: " + (l/1000));


                                if(remaining == 15){
                                    NotificationManager mNotificationManager
                                            = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                                    Intent notifyIntent = new Intent(ExtendActitvity2.this, MainActivity.class);
                                    notifyIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
                                    PendingIntent appIntent = PendingIntent.getActivity(ExtendActitvity2.this, 0, notifyIntent, 0);

                                    Notification notification
                                            = new Notification.Builder(ExtendActitvity2.this)
                                            .setContentIntent(appIntent)
                                            .setSmallIcon(R.drawable.ic_stat_access_alarm)
                                            .setTicker("notification on status bar.")
                                            .setWhen(System.currentTimeMillis())
                                            .setAutoCancel(false)
                                            .setContentTitle("Your car spot is about to be expired")
                                            .setContentText("Click there to extend your parking period ")
                                            .setOngoing(true)
                                            .setVibrate(new long[]{100, 200, 100, 500})
                                            .setDefaults(Notification.DEFAULT_VIBRATE).build();
                                    notification.flags = Notification.FLAG_AUTO_CANCEL;


                                    mNotificationManager.notify(0, notification);
                                }



                            }

                            @Override
                            public void onFinish() {
                                SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                                SharedPreferences.Editor editor = setting.edit();
                                editor.putBoolean(MainActivity.hasLoggedInField, false);
                                editor.putString(MainActivity.meterIDField, "");
                                editor.commit();
                                warningExtendText.setText("The parking period is currently expired, Click back to exit");
                                confirmExtendButton.setEnabled(false);
                            }
                        }.start();


                    }

                    if(dataSnapshot.child("duration").getValue().toString().equals("0")){
                        warningExtendText.setText("The parking period is currently expired, Click back to exit");
                        confirmExtendButton.setEnabled(false);
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Log.d("Remaining: ", remaining + "");

    }


}
