package com.example.clak;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

public class SendClakActivity extends Activity {

    private String TAG = "TAG_Login";
    private FirebaseAuth mAuth;

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
        String clak = String.format("%04d", random.nextInt(10000));
        code.setText(String.valueOf(clak));
    }

    public void setPopup(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.4));
    }
}
