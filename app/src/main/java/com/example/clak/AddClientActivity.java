package com.example.clak;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clak.classes.otp.OTPFactory;
import com.example.clak.classes.otp.OneTimePassword;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class AddClientActivity extends Activity {

    private static final String TAG = "AddClientActivity";
    private FirebaseAuth mAuth;

    DatabaseReference databaseOTP;
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

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyCode();
            }
        });
    }

    public void verifyCode(){

        OTPFactory.getByCode(code.getText().toString()).addOnCompleteListener(new OnCompleteListener<OneTimePassword>() {
            @Override
            public void onComplete(@NonNull Task<OneTimePassword> task) {
                if (task.isSuccessful()) {
                    final OneTimePassword otp = task.getResult();
                    Toast.makeText(AddClientActivity.this, "Success", Toast.LENGTH_SHORT).show();
                    writeToFirestore(otp.getUid()).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                otp.remove();
                            } else {
                                Toast.makeText(AddClientActivity.this, "Could not add customer", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(AddClientActivity.this, "Failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Task writeToFirestore(String cid) {
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, String> connection = new HashMap<>();
        connection.put("customer_id", cid);
        connection.put("organization_id", user.getUid());
        return db.collection("connections").add(connection);

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

        getWindow().setLayout((int)(width*.7), (int)(height*.5));
    }
}

