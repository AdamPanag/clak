package com.example.clak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.clak.classes.Organization;
import com.example.clak.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class OrganizationRegisterActivity extends AppCompatActivity {

    private String TAG = "TAG_Register";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private Button registerButton;
    private EditText email;
    private EditText password;
    private EditText password2;
    private EditText orgName;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_register);
        mAuth = FirebaseAuth.getInstance();

        initUIComponents();
    }

    private void initUIComponents() {
        registerButton = findViewById(R.id.signUpButton);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        password2 = findViewById(R.id.passwordInput2);
        orgName = findViewById(R.id.orgNameInput);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                registerUser(email.getText().toString(), password.getText().toString(),
                        orgName.getText().toString());
            }
        });
    }

    private void registerUser(final String email, String password, final String orgName) {
        //https://firebase.google.com/docs/auth/android/start/
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(OrganizationRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            writeNewUser(email, orgName, userId); //Realtime Database
                            writeUserToFirestore(email, orgName, userId); // Firestore
                            Log.d(TAG, "createUserWithEmail:success");
                            dismissLoading();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            dismissLoading();
                            Toast.makeText(OrganizationRegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Write the new user to the real time database of Firebase
     * @param email
     * @param orgName
     */
    private void writeNewUser(String email, String orgName, String userId) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        User organization = new Organization(email, orgName);
        mDatabase.child("organizations").child(userId).setValue(organization);
    }

    /**
     * Write the new user to the Cloud Firestore
     * @param email
     * @param orgName
     */
    private void writeUserToFirestore(String email, String orgName, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("orgName", orgName);

        // Add a new document with a given ID
        db.collection("organizations").document(userId)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot added.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void updateUI() {
        startActivity(new Intent(this, OrganizationMainActivity.class));
        finish();
    }

    private void showLoading() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissLoading() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}