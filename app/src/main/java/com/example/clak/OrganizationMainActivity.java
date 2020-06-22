package com.example.clak;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;


import static android.widget.Toast.makeText;

public class OrganizationMainActivity extends AppCompatActivity {

    private final static String TAG = "_MAIN_";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private TextView userInfoDisplay;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_main);
        initFirebaseAuth();
        initUIComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        FirebaseUser u = mAuth.getCurrentUser();
        updateUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    updateUI();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    userInfoDisplay.setText("No user");
                }
            }
        };

    }

    private void initUIComponents() {
        //toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        userInfoDisplay = (TextView) findViewById(R.id.userInfoDisplay);
        logout= (Button) findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    private void logoutUser() {
        startActivity(new Intent(OrganizationMainActivity.this, LoginActivity.class));
        FirebaseAuth.getInstance().signOut();
        makeText(OrganizationMainActivity.this,"See you soon!", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        FirebaseUser u = mAuth.getCurrentUser();
        if (u != null) {
            userInfoDisplay.setText(u.getEmail());
        }
    }
}