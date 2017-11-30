package com.joekelly.mapsandlocation;
/*
    Seperate class used for registration, when a user doesn't have an account.
    Using Firebases authentication process
 */
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserRegistrationActivity extends AppCompatActivity {
    // Instance variables for user information
    private EditText mUsername, mEmail, mPassword;
    private Button mRegisterButton;

    //Listener for when auth state changes
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    protected Double mLatitudeText;
    protected Double mLongitudeText;
    protected boolean finishedCreation = false;
    String nextActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        mAuth = FirebaseAuth.getInstance();

        mUsername = (EditText) findViewById(R.id.username);
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);

        mRegisterButton = (Button) findViewById(R.id.register);


        // Registration listener
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Going to pass the email and password up to the database
                final String username = mUsername.getText().toString();
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if ((email.length() > 0) && (password.length() > 0)) {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(UserRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(UserRegistrationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }else{
                                // If successful save same of the information to the database, user_id
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
                                current_user_db.child("name").setValue(username);
                                current_user_db.child("flags collected").setValue(0);
                                current_user_db.child("flags stolen").setValue(0);
                                launchNewActivity();
                            }
                        }
                    });
                } else {
                    Toast.makeText(UserRegistrationActivity.this, "Please enter an email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void launchNewActivity() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            Intent intent;
            if (nextActivity.equals("PublicMap")) {
                intent =  new Intent(UserRegistrationActivity.this, PublicMap.class);
            } else {
                intent = new Intent(UserRegistrationActivity.this, Leaderboard.class);
            }                    intent.putExtra("LAT", mLatitudeText);
            intent.putExtra("LON", mLongitudeText);
            startActivity(intent);
            finish();
            return;
        }
    }

    // When ever activity is called want to start the listener for the info
    @Override
    protected void onStart() {
        super.onStart();
    }
    // when finsed
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        mLatitudeText = intent.getDoubleExtra("LAT", 0.0);
        mLongitudeText = intent.getDoubleExtra("LON", 0.0);
        nextActivity = intent.getStringExtra("nextActivity");
        Toast.makeText(this, nextActivity, Toast.LENGTH_LONG).show();
    }

}
