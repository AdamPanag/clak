package com.example.clak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.clak.classes.otp.OTPFactory;
import com.example.clak.classes.otp.OneTimePassword;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QrScanActivity extends AppCompatActivity {

    SurfaceView cameraView;
    Button scanBtn;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);
        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        scanBtn = (Button) findViewById(R.id.startScan);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginScan();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 0).show();
        }
    }

    private void beginScan() {
        barcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(300, 300)
                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                cameraStart();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraSource.stop();
                            vibrate();
                            doClak(barcodes.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });
        cameraStart();
    }

    private void cameraStart() {
        if (ActivityCompat.checkSelfPermission(QrScanActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                return;
            }
        }
        try {
            cameraSource.start(cameraView.getHolder());
        } catch (IOException ie) {
            Log.e("CAMERA SOURCE", ie.getMessage());
        }
    }

    private void doClak(String code) {
        scanBtn.setText("Claking....");
        scanBtn.setEnabled(false);
        OTPFactory.getByCode(code).addOnCompleteListener(new OnCompleteListener<OneTimePassword>() {
            @Override
            public void onComplete(@NonNull Task<OneTimePassword> task) {
                if (task.isSuccessful()) {
                    OneTimePassword otp = task.getResult();
                    otp.remove();
                    writeNewClak(otp.getUid());
                } else {
                    Toast.makeText(getApplicationContext(), "Qr Code not recognized", Toast.LENGTH_SHORT).show();
                }
                scanBtn.setText(R.string.startScan);
                scanBtn.setEnabled(true);
            }
        });
    }

    private void writeNewClak(String cust_id) {
        FirebaseUser organization = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance()
                .collection("connections")
                .whereEqualTo("organization_id", organization.getUid())
                .whereEqualTo("customer_id", cust_id).limit(1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    if (qs.size() > 0) {
                        DocumentSnapshot doc = qs.getDocuments().get(0);
                        Map<String, Object> clakMap = new HashMap<String, Object>();
                        clakMap.put("customer_id", doc.get("customer_id").toString());
                        clakMap.put("organization_id", doc.get("organization_id").toString());
                        clakMap.put("timestamp", FieldValue.serverTimestamp());
                        FirebaseFirestore.getInstance().collection("claks").add(clakMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    playSound(true);
                                    Toast.makeText(getApplicationContext(), "Claked successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    playSound(false);
                                    Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        playSound(false);
                        Toast.makeText(getApplicationContext(), "Customer not recognized", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    playSound(false);
                    Toast.makeText(getApplicationContext(), "Could not connect", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Vibrate for 150 milliseconds
    private void vibrate() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
        }
    }

    private void playSound(boolean success) {
        if (success == true) {
            mediaPlayer = MediaPlayer.create(this, R.raw.accomplished);
            mediaPlayer.start();
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.failure);
            mediaPlayer.start();
        }
    }
}