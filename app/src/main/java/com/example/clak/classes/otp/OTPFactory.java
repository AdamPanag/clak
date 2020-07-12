package com.example.clak.classes.otp;

import com.google.android.gms.tasks.Task;

public class OTPFactory {

    public static OneTimePassword generateClakOTP(String uid) {
        return new FirebaseOneTimePassword(uid, OneTimePasswordType.CLAK);
    }

    public static OneTimePassword generateConnectionOTP(String uid) {
        return new FirebaseOneTimePassword(uid, OneTimePasswordType.CONNECTION);
    }

    public static Task<OneTimePassword> getByCode(String code) {
        return FirebaseOneTimePassword.getByCode(code);
    }
}
