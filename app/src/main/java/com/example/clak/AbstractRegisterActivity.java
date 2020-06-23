package com.example.clak;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRegisterActivity extends AppCompatActivity {

    private String TAG = "TAG_Register";

    private Button registerButton;
    private EditText email;
    private EditText password;
    private EditText password2;
    private ProgressDialog progressDialog;

    protected Map<String, String> registrationInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registrationInfo = new HashMap<String, String>();

        initUIComponents();
    }

    // When overriding this, do setContentView and call super.initUIComponents
    protected void initUIComponents() {
        registerButton = findViewById(R.id.signUpButton);

        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        password2 = findViewById(R.id.passwordInput2);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkFields()) {
                    createRegistrationInfo();
                    registerUser(registrationInfo);
                }
            }
        });
    }

    // Add email and password to registration info. Override to add more...
    // Perform checks before running this
    protected void createRegistrationInfo() {
        registrationInfo.put("email", email.getText().toString());
        registrationInfo.put("password", password.getText().toString());
    }

    // Registration info will have email and password. Check createRegistrationInfo()
    protected abstract void registerUser(Map<String, String> registrationInfo);

    // Checks whether the field is empty. If empty, it also displays the error.
    protected boolean isFieldEmpty(EditText text) {
        if (text.getText().toString().trim().length() == 0) {
            text.setError(getString(R.string.empty_field));
            text.requestFocus();
            return true;
        }
        return false;
    }

    /* Checks if email or password are empty and whether passwords match
     * Override to add more fields.
     */
    protected boolean checkFields() {
        // Check if any of the fields are empty
        boolean ok = true;
        ok = !isFieldEmpty(password2) && ok;
        ok = !isFieldEmpty(password) && ok;
        ok = !isFieldEmpty(email) && ok;
        if (!ok)
            return false;

        // Check whether passwords match
        if (!password.getText().toString().equals(password2.getText().toString())){
            password2.setError(getString(R.string.signup_password_mismatch));
            password2.requestFocus();
            return false;
        }
        return true;
    }

    // Hightlight corresponding field, in case registration fails
    protected void handleRegistrationFailure(Exception exception) {
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
            Toast.makeText(AbstractRegisterActivity.this, R.string.signup_error, Toast.LENGTH_SHORT).show();
        }
    }

    protected void showLoading() {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void dismissLoading() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }
}