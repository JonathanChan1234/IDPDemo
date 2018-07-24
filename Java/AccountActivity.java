package com.jonathan.idpdemo2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AccountActivity extends AppCompatActivity {
    Button signInButton, signUpButton;
    EditText accountText, passwordId;
    private DatabaseReference mDatabase;
    public static String accountField = "ACCOUNT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        signInButton = (Button) findViewById(R.id.SignInButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        accountText = (EditText) findViewById(R.id.AccountNumberText);
        passwordId = (EditText) findViewById(R.id.PasswordText);

        signInButton.setOnClickListener(signIn);
        signUpButton.setOnClickListener(signUp);

    }

    public void onResume(){
        super.onResume();
        mDatabase = FirebaseDatabase.getInstance().getReference("account");
    }

    View.OnClickListener signIn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final String account = accountText.getText().toString();
            final String password = passwordId.getText().toString();
            if(account.equals("") || password.equals("")){
                Toast.makeText(AccountActivity.this, "Account/Password cannot be leave back", Toast.LENGTH_SHORT).show();
            }
            else{
                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(account).getValue() == null){ //No account found
                            Toast.makeText(AccountActivity.this, "No such account", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(dataSnapshot.child(account).child("password").getValue().toString().equals(password)){    //Correct Password
                            Log.d("Account", "Log In");
                            mDatabase.child(account).child("loggedIn").setValue(1);
                            Intent intent = new Intent();
                            intent.setClass(AccountActivity.this, BalanceActivity.class);
                            SharedPreferences setting = getApplication().getSharedPreferences(MainActivity.data,0);
                            SharedPreferences.Editor editor = setting.edit();
                            editor.putString(MainActivity.accountField, account);
                            editor.putBoolean(MainActivity.accountLogInField, true);
                            editor.commit();
                            startActivity(intent);
                            Log.d("Hello", "It is an unreachable code")
                            }
                            else{   //Wrong Password
                                Toast.makeText(AccountActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    };

    View.OnClickListener signUp = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };


}
