package com.example.clak.classes;

public class Customer extends User {

    public String name;
    public String surname;
    public String email;

    public Customer(String email, String name, String surname) {

        this.email = email;
        this.name = name;
        this.surname = surname;

    }
}

