package com.example.clak.classes;

public class Organization extends User {

    private String orgName;
    private String email;
    private String id;

    public Organization() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Organization(String id, String email, String orgName) {
        this.id = id;
        this.email = email;
        this.orgName = orgName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
