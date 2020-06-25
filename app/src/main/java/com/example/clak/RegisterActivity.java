package com.example.clak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity {

    private String TAG = "TAG_Register";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private Button sgnUpAsCustomerBtn;
    private Button sgnUpAsOrganizationBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUIComponents();
    }

    private void initUIComponents() {
        sgnUpAsCustomerBtn = (Button) findViewById(R.id.sgnUpAsCustomerBtn);
        sgnUpAsOrganizationBtn = (Button) findViewById(R.id.sgnUpAsOrganizationBtn);

        // go to customer register activity
        sgnUpAsCustomerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCustomerRegisterActivity();
            }
        });

        //go to organization register activity
        sgnUpAsOrganizationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToOrganizationRegisterActivity();
            }
        });
    }

    private void goToCustomerRegisterActivity() {
        startActivity(new Intent(this, CustomerRegisterActivity.class));
    }

    private void goToOrganizationRegisterActivity() {
        startActivity(new Intent(this, OrganizationRegisterActivity.class));
    }
}