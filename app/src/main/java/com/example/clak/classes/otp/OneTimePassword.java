package com.example.clak.classes.otp;

import com.google.android.gms.tasks.Task;

public interface OneTimePassword {


    public String getCode();
    public String getCid();

    // Saves to persistent storage
    public Task write();
    public Task remove();


}

