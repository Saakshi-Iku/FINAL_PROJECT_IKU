package com.iku.models;

import com.google.firebase.database.PropertyName;

public class CommentModel {

    @PropertyName("comment")
    private String comment;
    @PropertyName("uid")
    private String uid;
    @PropertyName("commenterName")
    private String commenterName;
    @PropertyName("timestamp")
    private long timestamp;

    private CommentModel() {

    }

    public CommentModel(String comment, String uid, long timestamp) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getCommenterName() {
        return commenterName;
    }

}

