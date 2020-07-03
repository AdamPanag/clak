package com.example.clak;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.example.clak.classes.Customer;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomerRegisterActivity extends AbstractRegisterActivity {

    private String TAG = "TAG_Register";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private EditText firstName;
    private EditText surname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        initUIComponents();
    }

    @Override
    protected void initUIComponents() {
        setContentView(R.layout.activity_customer_register);
        super.initUIComponents();
        firstName = findViewById(R.id.nameInput);
        surname = findViewById(R.id.surnameInput);
    }

    @Override
    protected void createRegistrationInfo() {
        super.createRegistrationInfo();
        registrationInfo.put("name", firstName.getText().toString());
        registrationInfo.put("surname", surname.getText().toString());
    }

    @Override
    protected void registerUser(Map<String, String> registrationInfo) {
        showLoading();
        //https://firebase.google.com/docs/auth/android/start/
        final String email = registrationInfo.get("email");
        final String password = registrationInfo.get("password");
        final String name = registrationInfo.get("name");
        final String surname = registrationInfo.get("surname");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(CustomerRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String userId = firebaseUser.getUid();

                            writeUserToFirestore(email, name, surname, userId); // Firestore
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

    @Override
    protected boolean checkFields() {
        boolean ok = super.checkFields();
        ok = !isFieldEmpty(surname) && ok;
        ok = !isFieldEmpty(firstName) && ok;

        return ok;
    }

    /**
     * Write the new user to the Cloud Firestore
     * @param email
     * @param name
     * @param surname
     */
    private void writeUserToFirestore(String email, String name, String surname, String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        ArrayList<String> connections = new ArrayList<String>();
        user.put("connections", connections);
        user.put("email", email);
        user.put("name", name);
        user.put("surname", surname);

        // Add a new document with a given ID
        db.collection("customers").document(userId)
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
        startActivity(new Intent(this, CustomerMainActivity.class));
        finish();
    }

}