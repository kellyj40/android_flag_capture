package com.joekelly.mapsandlocation;
/*
This class is incharge of user authentication of the users logining into the system
 */
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserLoginActivity extends AppCompatActivity {

    //Instance variables of user login information
    private EditText mEmail, mPassword;
    private Button mLogin;
    private TextView mRegistrationLink;

    //Listener for when auth state changes
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    protected Double mLatitudeText;
    protected Double mLongitudeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        Intent intent = getIntent();
        mLatitudeText = intent.getDoubleExtra("LAT", 0.0);
        mLongitudeText = intent.getDoubleExtra("LON", 0.0);
        final String nextActivity = intent.getStringExtra("nextActivity");

        mAuth = FirebaseAuth.getInstance();
        // User logins in and goes to the next stage if exists
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if(user!=null){
                    Intent intent;
                    if (nextActivity.equals("PublicMap")) {
                       intent =  new Intent(UserLoginActivity.this, PublicMap.class);
                    } else {
                        intent = new Intent(UserLoginActivity.this, Leaderboard.class);
                    }

                    intent.putExtra("LAT", mLatitudeText);
                    intent.putExtra("LON", mLongitudeText);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        // Fields where the user logs in
        mEmail = (EditText) findViewById(R.id.input_email);
        mPassword = (EditText) findViewById(R.id.input_password);

        mLogin = (Button) findViewById(R.id.login);
        mRegistrationLink = (TextView) findViewById(R.id.link_registration);

        // Just logging in with old info
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                if ((email.length() > 0) && (password.length() > 0)) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(UserLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(UserLoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserLoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(UserLoginActivity.this, "Please enter an email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Registration link listener
        mRegistrationLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), UserRegistrationActivity.class);
                intent.putExtra("LAT", mLatitudeText);
                intent.putExtra("LON", mLongitudeText);
                intent.putExtra("nextActivity", nextActivity);
                startActivity(intent);
                finish();
            }
        });
    }

    // When ever activity is called want to start the listener for the info
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }
    // when finished
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}
