package com.example.clak;

import androidx.annotation.NonNull;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Path;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private String TAG = "TAG_Login";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private Button loginButton;
    private Button signUpButton;
    private Button showLoginButton;
    private TextInputLayout email;
    private TextInputLayout password;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        initUIComponents();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    private void initUIComponents() {
        loginButton = (Button) findViewById(R.id.loginButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);
        showLoginButton = (Button) findViewById(R.id.showLoginButton);
        email = (TextInputLayout) findViewById(R.id.emailInput);
        password = (TextInputLayout) findViewById(R.id.passwordInput);

        showLoginButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (email.getVisibility() != View.VISIBLE) {
                   email.setVisibility(View.VISIBLE);
                   password.setVisibility(View.VISIBLE);
                   loginButton.setVisibility(View.VISIBLE);
               } else {
                   email.setVisibility(View.GONE);
                   password.setVisibility(View.GONE);
                   loginButton.setVisibility(View.GONE);
               }
           }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (TextUtils.isEmpty(email.getEditText().getText().toString()))
                    Toast.makeText(LoginActivity.this, "Email cannot be empty", Toast.LENGTH_LONG).show();
                else if (TextUtils.isEmpty(password.getEditText().getText().toString()))
                    Toast.makeText(LoginActivity.this, "Password cannot be empty", Toast.LENGTH_LONG).show();
                else {
                    showLoading();
                    loginUser(email.getEditText().getText().toString(), password.getEditText().getText().toString());
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegisterActivity();
            }
        });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            dismissLoading();
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Go to Customer or Organization main screen
                            goToMainActivity(user.getUid());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            dismissLoading();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    /**
     * Checks if the current user's ID exists in the 'customers' collection.
     * If it does, redirects the user to customer's main activity-screen.
     * If it doesn't, redirects the user to organization's main activity-screen
     * (since there is no other type of user).
     *
     * P.S. It is not expandable, but it works.
     * @param id
     */
    private void goToMainActivity(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("customers").document(id);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Enter here only when the user has loged in and his ID is in customers' collection
                        goToCustomerMainActivity(); //Customer
                    } else {
                        //Enter here only when the user has loged in and his ID is NOT in customers' collection
                        goToOrganizationMainActivity(); //Organization
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void goToCustomerMainActivity() {
        startActivity(new Intent(this, CustomerMainActivity.class));
        finish();
    }

    public void goToOrganizationMainActivity() {
        startActivity(new Intent(this, OrganizationMainActivity.class));
        finish();
    }

    private void goToRegisterActivity() {
        startActivity(new Intent(this, RegisterActivity.class));
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
