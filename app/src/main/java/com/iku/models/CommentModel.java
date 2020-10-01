package com.iku.models;

import com.google.firebase.Timestamp;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;

public class CommentModel {

    @PropertyName("comment")
    private String comment;
    @PropertyName("uid")
    private String uid;
    @PropertyName("commenterName")
    private String commenterName;
    @PropertyName("timestamp")
    private long timestamp;
    @PropertyName("heartsCount")
    private int heartsCount;
    @PropertyName("heartsArray")
    private ArrayList<String> heartsArray;
    @PropertyName("readableTimestamp")
    private Timestamp readableTimestamp;

    private CommentModel() {

    }

    public CommentModel(String comment, String uid, String commenterName, long timestamp, int heartsCount, ArrayList<String> heartsArray, Timestamp readableTimestamp) {
        this.comment = comment;
        this.uid = uid;
        this.commenterName = commenterName;
        this.timestamp = timestamp;
        this.heartsCount = heartsCount;
        this.heartsArray = heartsArray;
        this.readableTimestamp = readableTimestamp;
    }

    public String getComment() {
        return comment;
    }

    public String getUid() {
        return uid;
    }

    public String getCommenterName() {
        return commenterName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getHeartsCount() {
        return heartsCount;
    }

    public ArrayList<String> getHeartsArray() {
        return heartsArray;
    }

    public Timestamp getReadableTimestamp() {
        return readableTimestamp;
    }

}

