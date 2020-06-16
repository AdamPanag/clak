package com.example.clak.classes;

public class Organization extends User {

    public String orgName;
    public String email;

    public Organization() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Organization(String email, String orgName) {
        this.email = email;
        this.orgName = orgName;
    }
}
