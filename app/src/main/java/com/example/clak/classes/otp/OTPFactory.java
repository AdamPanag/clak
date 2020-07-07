package com.example.clak.classes.otp;

import com.google.android.gms.tasks.Task;

public class OTPFactory {

    public static OneTimePassword generateClakOTP(String cid) {
        return new FirebaseOneTimePassword(cid, OneTimePasswordType.CLAK);
    }

    public static OneTimePassword generateConnectionOTP(String cid) {
        return new FirebaseOneTimePassword(cid, OneTimePasswordType.CONNECTION);
    }

    public static Task<OneTimePassword> getByCode(String code) {
        return FirebaseOneTimePassword.getByCode(code);
    }
}
