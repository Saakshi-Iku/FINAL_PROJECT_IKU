package com.iku.models;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;

public class ChatModel {

    @PropertyName("message")
    private String message;
    @PropertyName("uid")
    private String UID;
    @PropertyName("userName")
    private String userName;
    @PropertyName("type")
    private String type;
    @PropertyName("imageUrl")
    private String imageUrl;
    @PropertyName("upvoteCount")
    private int upvoteCount;
    @PropertyName("timestamp")
    private long timestamp;
    @PropertyName("upvoters")
    private ArrayList<String> upvoters;
    @PropertyName("emoji1")
    private ArrayList<String> emoji1;
    @PropertyName("emoji2")
    private ArrayList<String> emoji2;
    @PropertyName("emoji3")
    private ArrayList<String> emoji3;
    @PropertyName("emoji4")
    private ArrayList<String> emoji4;
    @PropertyName("downvoters")
    private ArrayList<String> downvoters;
    @PropertyName("downvoteCount")
    private int downvoteCount;
    @PropertyName("edited")
    private boolean edited;
    @PropertyName("spam")
    private boolean spam;
    @PropertyName("spamCount")
    private int spamCount;
    @PropertyName("originalImageUrl")
    private String originalImageUrl;

    private ChatModel() {
    }

    public ChatModel(String message, String UID, String userName, String type, String imageUrl, int upvoteCount, long timestamp, ArrayList<String> upvoters, ArrayList<String> emoji1, ArrayList<String> emoji2, ArrayList<String> emoji3, ArrayList<String> emoji4, ArrayList<String> downvoters, int downvoteCount, boolean edited, boolean spam, int spamCount, String originalImageUrl) {
        this.message = message;
        this.UID = UID;
        this.userName = userName;
        this.type = type;
        this.imageUrl = imageUrl;
        this.upvoteCount = upvoteCount;
        this.timestamp = timestamp;
        this.upvoters = upvoters;
        this.emoji1 = emoji1;
        this.emoji2 = emoji2;
        this.emoji3 = emoji3;
        this.emoji4 = emoji4;
        this.downvoters = downvoters;
        this.downvoteCount = downvoteCount;
        this.edited = edited;
        this.spam = spam;
        this.spamCount = spamCount;
        this.originalImageUrl = originalImageUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getimageUrl() {
        return imageUrl;
    }

    public void setimageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public void setUpvoteCount(int upvoteCount) {
        this.upvoteCount = upvoteCount;
    }


    public ArrayList<String> getupvoters() {
        return upvoters;
    }

    public void setupvoters(ArrayList<String> upvoters) {
        this.upvoters = upvoters;
    }

    public ArrayList<String> getEmoji1() {
        return emoji1;
    }

    public void setEmoji1(ArrayList<String> emoji1) {
        this.emoji1 = emoji1;
    }

    public ArrayList<String> getEmoji2() {
        return emoji2;
    }

    public void setEmoji2(ArrayList<String> emoji2) {
        this.emoji2 = emoji2;
    }

    public ArrayList<String> getEmoji3() {
        return emoji3;
    }

    public void setEmoji3(ArrayList<String> emoji3) {
        this.emoji3 = emoji3;
    }

    public ArrayList<String> getEmoji4() {
        return emoji4;
    }

    public void setEmoji4(ArrayList<String> emoji4) {
        this.emoji4 = emoji4;
    }

    public ArrayList<String> getDownvoters() {
        return downvoters;
    }

    public void setDownvoters(ArrayList<String> downvoters) {
        this.downvoters = downvoters;
    }

    public int getDownvoteCount() {
        return downvoteCount;
    }

    public void setDownvoteCount(int downvoteCount) {
        this.downvoteCount = downvoteCount;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
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

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }
}
