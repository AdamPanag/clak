package com.example.clak.classes.otp;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FirebaseOneTimePassword implements OneTimePassword {

    private String code;
    private String uid;
    private OneTimePasswordType type;
    private String dbId;

    public FirebaseOneTimePassword(String uid, OneTimePasswordType type) {
        this.uid = uid;
        this.code = generateCode();
        this.type = type;
        this.dbId = null;
    }

    public FirebaseOneTimePassword(String uid, OneTimePasswordType type, String code, String dbId) {
        this.uid = uid;
        this.type = type;
        this.code = code;
        this.dbId = dbId;
    }

    public String getCode() {
        return code;
    }


    public String getUid() {
        return uid;
    }

    @Override
    public Task write() {
        CollectionReference databaseOTP = FirebaseFirestore.getInstance().collection("otp");

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_id", this.uid);
        map.put("code", this.code);
        map.put("type", this.type);
        return databaseOTP.add(map);
    }

    @Override
    public Task remove() {
        CollectionReference databaseOTP = FirebaseFirestore.getInstance().collection("otp");
        return databaseOTP.document(dbId).delete();
    }

    private String generateCode(){
        Random random = new Random();
        String clak = String.format("%04d", random.nextInt(10000));;
        while((clak.charAt(0)) == ('0')){
            clak = String.format("%04d", random.nextInt(10000));
        }

        return clak;
    }

    public static Task<OneTimePassword> getByCode(String code) {

        CollectionReference databaseOTP = FirebaseFirestore.getInstance().collection("otp");
        return databaseOTP.whereEqualTo("code", code).get().continueWith(new Continuation<QuerySnapshot, OneTimePassword>() {
             @Override
             public OneTimePassword then(@NonNull Task<QuerySnapshot> task) throws Exception {
                 DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                 return new FirebaseOneTimePassword(doc.getString("user_id"),
                         OneTimePasswordType.valueOf(doc.getString("type")),
                         doc.getString("code"),
                         doc.getId()
                 );
             }
         }
        );
    }

}
