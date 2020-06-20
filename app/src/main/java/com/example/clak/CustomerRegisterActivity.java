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

import com.example.clak.classes.Customer;
import com.example.clak.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class CustomerRegisterActivity extends AppCompatActivity {

    private String TAG = "TAG_Register";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private Button registerButton;
    private EditText email;
    private EditText password;
    private EditText password2;
    private EditText firstName;
    private EditText surname;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_register);
        mAuth = FirebaseAuth.getInstance();

        initUIComponents();
    }

    private void initUIComponents() {
        registerButton = findViewById(R.id.signUpButton);

        email = findViewById(R.id.emailInput);
        firstName = findViewById(R.id.nameInput);
        surname = findViewById(R.id.surnameInput);
        password = findViewById(R.id.passwordInput);
        password2 = findViewById(R.id.passwordInput2);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                if (!checkFields()) {
                    dismissLoading();
                    return;
                }
                registerUser(email.getText().toString(), password.getText().toString(),
                             firstName.getText().toString(), surname.getText().toString());
            }
        });
    }

    private void registerUser(final String email, String password, final String name, final String surname) {
        //https://firebase.google.com/docs/auth/android/start/
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(CustomerRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            writeNewUser(email, name, surname); //Realtime Database
                            writeUserToFirestore(email, name, surname); // Firestore
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

    // Checks whether the field is empty. If empty, it also displays the error.
    private boolean isFieldEmpty(EditText text) {
        if (text.getText().toString().trim().length() == 0) {
            text.setError(getString(R.string.empty_field));
            text.requestFocus();
            return true;
        }
        return false;
    }

    private boolean checkFields() {
        // Check if any of the fields are empty
        boolean failed = false;
        failed |= isFieldEmpty(password2);
        failed |= isFieldEmpty(password);
        failed |= isFieldEmpty(email);
        failed |= isFieldEmpty(surname);
        failed |= isFieldEmpty(firstName);
        if (failed)
            return false;

        // Check whether passwords match
        if (!password.getText().toString().equals(password2.getText().toString())){
            password2.setError(getString(R.string.signup_password_mismatch));
            password2.requestFocus();
            return false;
        }
        return true;
    }

    private void handleRegistrationFailure(Exception exception) {
        try {
            throw exception;
        } catch(FirebaseAuthUserCollisionException e) {
            email.setError(getString(R.string.signup_user_exists));
            email.requestFocus();
        } catch(FirebaseAuthWeakPasswordException e) {
            password.setError(getString(R.string.signup_weak_password));
            password.requestFocus();
        } catch(FirebaseAuthInvalidCredentialsException e) {
            email.setError(getString(R.string.signup_invalid_email));
            email.requestFocus();
        }  catch(Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(CustomerRegisterActivity.this, R.string.signup_error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Write the new user to the real time database of Firebase
     * @param email
     * @param name
     * @param surname
     */
    private void writeNewUser(String email, String name, String surname) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        User customer = new Customer(email, name, surname);
        String userId = firebaseUser.getUid();
        mDatabase.child("customers").child(userId).setValue(customer);
    }

    /**
     * Write the new user to the Cloud Firestore
     * @param email
     * @param name
     * @param surname
     */
    private void writeUserToFirestore(String email, String name, String surname) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("name", name);
        user.put("surname", surname);

        // Add a new document with a generated ID
        db.collection("customers")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
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