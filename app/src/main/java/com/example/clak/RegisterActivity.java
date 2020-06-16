package com.example.clak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RegisterActivity extends AppCompatActivity {

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