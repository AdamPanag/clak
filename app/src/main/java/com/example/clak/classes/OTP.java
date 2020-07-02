package com.example.clak.classes;

public class OTP {
    private int code;
    private String cid;

    public OTP(int code, String cid){
        this.code = code;
        this.cid = cid;
    }

    public OTP() {}

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

}
