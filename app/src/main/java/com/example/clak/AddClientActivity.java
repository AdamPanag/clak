package com.example.clak;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clak.classes.OTP;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AddClientActivity extends Activity {

    private static final String TAG = "AddClientActivity";
    private FirebaseAuth mAuth;

    DatabaseReference databaseOTP;
    private List<OTP> otpList;
    private TextView code;
    private Button verify;

    String otpCid;
    int otpCode;
    String bountiedCid;
    int bountiedCode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enter_clak_password);
        mAuth = FirebaseAuth.getInstance();

        otpCid = "";
        otpCode = 0;
        bountiedCid = "No cid found";
        bountiedCode = 0;

        setPopup();
        databaseOTP = FirebaseDatabase.getInstance().getReference("otp");
        initUIComponents();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initUIComponents() {
        verify = (Button) findViewById(R.id.verify);
        code = (TextView) findViewById(R.id.firstPinView);
        otpList = new ArrayList<>();

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
    }

    public void verifyCode(){


        databaseOTP.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otpList.clear();

                //otpSS stands for otpSnapShot
                for(DataSnapshot otpSS : dataSnapshot.getChildren()){
                    OTP otp = otpSS.getValue(OTP.class);
                        otpCid = otp.getCid();
                        otpCode = otp.getCode();

                        if(code.getText().toString().equals(String.valueOf(otpCode))){
                            bountiedCid = otp.getCid();
                            bountiedCode = otp.getCode();
                            writeToFirestore(bountiedCid);
                            deleteFromRealtime(bountiedCid);
                        }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "find id:onCancelled", databaseError.toException());
            }
        });
    }

    public void writeToFirestore(String cid) {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference orgRef = db.collection("organizations").document(user.getUid());
        orgRef.update("clients", FieldValue.arrayUnion(cid));
    }

    public void deleteFromRealtime(String cid) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        Query query = ref.child("otp").orderByChild("cid").equalTo(cid); //Filtering in order to delete the field we want

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot cidSnapshot: dataSnapshot.getChildren()) {
                    cidSnapshot.getRef().removeValue();
                    Toast.makeText(AddClientActivity.this, "Claked succesfully!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void setPopup(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.7), (int)(height*.6));
    }
}

