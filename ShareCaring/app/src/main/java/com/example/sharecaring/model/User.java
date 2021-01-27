package com.example.sharecaring.model;

public class User {
    public String firstName;
    public String lastName;
    public String email;
    public String phoneNumber;

    public User() {
    }

    public User(String firstName,String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
}
