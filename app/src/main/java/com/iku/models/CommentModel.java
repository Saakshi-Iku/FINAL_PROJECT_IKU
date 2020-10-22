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
    @PropertyName("spam")
    private boolean spam;
    @PropertyName("spamCount")
    private int spamCount;
    @PropertyName("spamReportedBy")
    private ArrayList<String> spamReportedBy;
    @PropertyName("deleted")
    private boolean deleted;
    @PropertyName("deletedBy")
    private String deletedBy;
    @PropertyName("edited")
    private boolean edited;
    @PropertyName("commentUpdateTime")
    private long commentUpdateTime;
    @PropertyName("readableCommentUpdateTime")
    private Timestamp readableCommentUpdateTime;

    private CommentModel() {

    }


    public CommentModel(String comment, String uid, String commenterName, long timestamp, int heartsCount, ArrayList<String> heartsArray, Timestamp readableTimestamp, boolean spam, int spamCount, ArrayList<String> spamReportedBy, boolean deleted, String deletedBy, boolean edited, long commentUpdateTime, Timestamp readableCommentUpdateTime) {
        this.comment = comment;
        this.uid = uid;
        this.commenterName = commenterName;
        this.timestamp = timestamp;
        this.heartsCount = heartsCount;
        this.heartsArray = heartsArray;
        this.readableTimestamp = readableTimestamp;
        this.spam = spam;
        this.spamCount = spamCount;
        this.spamReportedBy = spamReportedBy;
        this.deleted = deleted;
        this.deletedBy = deletedBy;
        this.edited = edited;
        this.commentUpdateTime = commentUpdateTime;
        this.readableCommentUpdateTime = readableCommentUpdateTime;
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

    public boolean isSpam() {
        return spam;
    }

    public void setSpam(boolean spam) {
        this.spam = spam;
    }

    public int getSpamCount() {
        return spamCount;
    }

    public void setSpamCount(int spamCount) {
        this.spamCount = spamCount;
    }

    public ArrayList<String> getSpamReportedBy() {
        return spamReportedBy;
    }

    public void setSpamReportedBy(ArrayList<String> spamReportedBy) {
        this.spamReportedBy = spamReportedBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public long getCommentUpdateTime() {
        return commentUpdateTime;
    }

    public void setCommentUpdateTime(long commentUpdateTime) {
        this.commentUpdateTime = commentUpdateTime;
    }

    public Timestamp getReadableCommentUpdateTime() {
        return readableCommentUpdateTime;
    }

    public void setReadableCommentUpdateTime(Timestamp readableCommentUpdateTime) {
        this.readableCommentUpdateTime = readableCommentUpdateTime;
    }

}

