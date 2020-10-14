package com.iku.models;

import com.google.firebase.database.PropertyName;

import java.util.ArrayList;

public class FeatureUpvoteModel {

    @PropertyName("title")
    private String title;
    @PropertyName("description")
    private String description;
    @PropertyName("image")
    private String image;
    @PropertyName("timestamp")
    private long timestamp;
    @PropertyName("upvote_count")
    private long upvote_count;
    @PropertyName("row")
    private long row;
    @PropertyName("upVotedUser")
    private ArrayList<String> upVotedUser;

    public FeatureUpvoteModel() {
    }

    public FeatureUpvoteModel(String title, String description, String image, long timestamp, long upvote_count, long row, ArrayList<String> upVotedUser) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.timestamp = timestamp;
        this.upvote_count = upvote_count;
        this.row = row;
        this.upVotedUser = upVotedUser;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getUpvote_count() {
        return upvote_count;
    }

    public long getRow() {
        return row;
    }

    public ArrayList<String> getUpVotedUser() {
        return upVotedUser;
    }
}
