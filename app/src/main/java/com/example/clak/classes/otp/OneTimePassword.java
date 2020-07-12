package com.example.clak.classes.otp;

import com.google.android.gms.tasks.Task;

public interface OneTimePassword {


    // Get the code
    public String getCode();
    // Get the Id of the user who generated this code
    public String getUid();

    // Saves to persistent storage
    public Task write();
    // Removed from persistent storage
    public Task remove();


}

