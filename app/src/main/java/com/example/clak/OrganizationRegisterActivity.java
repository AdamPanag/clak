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

public class OrganizationRegisterActivity extends AbstractRegisterActivity {

    private String TAG = "TAG_Register";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    private EditText orgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void initUIComponents() {
        setContentView(R.layout.activity_organization_register);
        super.initUIComponents();
        orgName = findViewById(R.id.orgNameInput);
    }

    @Override
    protected void createRegistrationInfo() {
        super.createRegistrationInfo();
        registrationInfo.put("orgName", orgName.getText().toString());
    }

    @Override
    protected boolean checkFields() {
        boolean ok = super.checkFields();
        ok = !isFieldEmpty(orgName) && ok;

        return ok;
    }

    @Override
    protected void registerUser(Map<String, String> registrationInfo) {
        //https://firebase.google.com/docs/auth/android/start/
        final String email = registrationInfo.get("email");
        final String password = registrationInfo.get("password");
        final String orgName = registrationInfo.get("orgName");
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
                            handleRegistrationFailure(task.getException());
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

}