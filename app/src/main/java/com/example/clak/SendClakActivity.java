package com.example.clak;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.clak.classes.OTP;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SendClakActivity extends Activity {

    private String TAG = "TAG_Login";
    private FirebaseAuth mAuth;
    DatabaseReference databaseOTP;

    private TextView code;
    private Button resendPassword;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.generate_clak_password);

        databaseOTP = FirebaseDatabase.getInstance().getReference("otp");

        setPopup();
        initUIComponents();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        Random random = new Random();
        String clak = String.format("%04d", random.nextInt(10000));;
        while((clak.charAt(0)) == ('0')){
            clak = String.format("%04d", random.nextInt(10000));
        }
        code.setText(clak);

        writeOTP(clak);
    }

    protected void writeOTP(String clak) {
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
        String cid = currentFirebaseUser.getUid();

        OTP otp = new OTP(Integer.parseInt(clak), cid);

        databaseOTP.child(cid).setValue(otp);
        Toast.makeText(this, clak, Toast.LENGTH_LONG).show();
    }


    public void setPopup(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.4));
    }
}
