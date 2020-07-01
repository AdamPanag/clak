package com.example.clak;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import static android.widget.Toast.makeText;

public class OrganizationMainActivity extends AppCompatActivity {

    private final static String TAG = "_MAIN_";
    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private Toolbar toolbar;

    private TextView name;
    private TextView email;

    private Button logout;
    private Button myClientsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Clak");
        setSupportActionBar(toolbar);
        initFirebaseAuth();
        initUIComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        user = mAuth.getCurrentUser();
        updateUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organization_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_toolbar:
                logoutUser();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
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
                }
            }
        };

    }

    private void initUIComponents() {
        name = (TextView) findViewById(R.id.name);
        email = (TextView) findViewById(R.id.email);

        logout = (Button) findViewById(R.id.logout);
        myClientsBtn = (Button) findViewById(R.id.my_clients_btn);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        myClientsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMyClients();
            }
        });

    }

    private void logoutUser() {
        startActivity(new Intent(OrganizationMainActivity.this, LoginActivity.class));
        FirebaseAuth.getInstance().signOut();
        makeText(OrganizationMainActivity.this, R.string.see_you_soon, Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        FirebaseUser u = mAuth.getCurrentUser();
        if (u != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("organizations").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                name.setText(document.getData().get("orgName").toString());
                                email.setText(document.getData().get("email").toString());
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void showMyClients() {
        startActivity(new Intent(this, MyClientsActivity.class));
    }
}