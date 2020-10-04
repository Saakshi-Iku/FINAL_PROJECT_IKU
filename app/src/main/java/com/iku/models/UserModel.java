package com.iku.models;

import com.google.firebase.firestore.PropertyName;

public class UserModel {

    @PropertyName("firstName")
    private String firstName;
    @PropertyName("lastName")
    private String lastName;
    @PropertyName("uid")
    private String uid;
    @PropertyName("points")
    private int points;

    private UserModel() {
    }

    public UserModel(String firstName, String lastName, int points, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.points = points;
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}

