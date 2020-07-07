package com.example.clak;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.clak.classes.otp.OTPFactory;
import com.example.clak.classes.otp.OneTimePassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class SendClakActivity extends Activity {

    private String TAG = "TAG_Login";
    private FirebaseAuth mAuth;
    private String customer_id;

    private TextView code;
    private Button resendPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_clak_password);
        setPopup();
        initUIComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.customer_id = user.getUid();
        generatePassword();
    }
    private void initUIComponents(){
        code = (TextView) findViewById(R.id.generatedPassword);
        resendPassword = (Button) findViewById(R.id.verifyButton);

        resendPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePassword();
            }
        });
    }

    public void generatePassword(){
        OneTimePassword otp = OTPFactory.generateConnectionOTP(customer_id);
        code.setText(otp.getCode());
        otp.write().addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SendClakActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SendClakActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    public void setPopup(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.4));
    }
}
