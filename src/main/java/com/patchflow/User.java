package com.patchflow;

public class User {
    public String uid;
    public String email;
    public String idToken;

    public User(String uid, String email, String idToken) {
        this.uid = uid;
        this.email = email;
        this.idToken = idToken;
    }
}