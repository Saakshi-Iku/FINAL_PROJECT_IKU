package com.iku.models;

import com.google.firebase.Timestamp;
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
    @PropertyName("messageUpdateTime")
    private long messageUpdateTime;
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
    @PropertyName("spamReportedBy")
    private ArrayList<String> spamReportedBy;
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
    @PropertyName("readableMessageUpdateTime")
    private Timestamp readableMessageUpdateTime;
    @PropertyName("topComment")
    private String topComment;
    @PropertyName("topCommenterImageUrl")
    private String topCommenterImageUrl;
    @PropertyName("topCommenterName")
    private String topCommenterName;
    @PropertyName("linkPreview")
    private int linkPreview;
    @PropertyName("linkPreviewUrl")
    private String linkPreviewUrl;
    @PropertyName("linkPreviewTitle")
    private String linkPreviewTitle;
    @PropertyName("linkPreviewDesc")
    private String linkPreviewDesc;
    @PropertyName("linkPreviewImageUrl")
    private String linkPreviewImageUrl;
    @PropertyName("deleted")
    private boolean deleted;
    @PropertyName("deletedBy")
    private String deletedBy;
    @PropertyName("postCommentCount")
    private int postCommentCount;

    private ChatModel() {
    }

    public ChatModel(String message, String UID, String userName, String type, String imageUrl, int upvoteCount, long timestamp, long messageUpdateTime, ArrayList<String> upvoters, ArrayList<String> emoji1, ArrayList<String> emoji2, ArrayList<String> emoji3, ArrayList<String> emoji4, ArrayList<String> downvoters, ArrayList<String> spamReportedBy, int downvoteCount, boolean edited, boolean spam, int spamCount, String originalImageUrl, Timestamp readableMessageUpdateTime, String topComment, String topCommenterImageUrl, String topCommenterName, int linkPreview, String linkPreviewUrl, String linkPreviewTitle, String linkPreviewDesc, String linkPreviewImageUrl, boolean deleted, String deletedBy, int postCommentCount) {
        this.message = message;
        this.UID = UID;
        this.userName = userName;
        this.type = type;
        this.imageUrl = imageUrl;
        this.upvoteCount = upvoteCount;
        this.timestamp = timestamp;
        this.messageUpdateTime = messageUpdateTime;
        this.upvoters = upvoters;
        this.emoji1 = emoji1;
        this.emoji2 = emoji2;
        this.emoji3 = emoji3;
        this.emoji4 = emoji4;
        this.downvoters = downvoters;
        this.spamReportedBy = spamReportedBy;
        this.downvoteCount = downvoteCount;
        this.edited = edited;
        this.spam = spam;
        this.spamCount = spamCount;
        this.originalImageUrl = originalImageUrl;
        this.readableMessageUpdateTime = readableMessageUpdateTime;
        this.topComment = topComment;
        this.topCommenterImageUrl = topCommenterImageUrl;
        this.topCommenterName = topCommenterName;
        this.linkPreview = linkPreview;
        this.linkPreviewUrl = linkPreviewUrl;
        this.linkPreviewTitle = linkPreviewTitle;
        this.linkPreviewDesc = linkPreviewDesc;
        this.linkPreviewImageUrl = linkPreviewImageUrl;
        this.deleted = deleted;
        this.deletedBy = deletedBy;
        this.postCommentCount = postCommentCount;
    }

    public ChatModel(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getUID() {
        return UID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }

    public String getimageUrl() {
        return imageUrl;
    }

    public int getUpvoteCount() {
        return upvoteCount;
    }

    public ArrayList<String> getupvoters() {
        return upvoters;
    }

    public ArrayList<String> getEmoji1() {
        return emoji1;
    }

    public ArrayList<String> getEmoji2() {
        return emoji2;
    }

    public ArrayList<String> getEmoji3() {
        return emoji3;
    }

    public ArrayList<String> getEmoji4() {
        return emoji4;
    }

    public ArrayList<String> getDownvoters() {
        return downvoters;
    }

    public int getDownvoteCount() {
        return downvoteCount;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isSpam() {
        return spam;
    }

    public int getSpamCount() {
        return spamCount;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public long getMessageUpdateTime() {
        return messageUpdateTime;
    }

    public ArrayList<String> getSpamReportedBy() {
        return spamReportedBy;
    }

    public Timestamp getReadableMessageUpdateTime() {
        return readableMessageUpdateTime;
    }

    public String getTopComment() {
        return topComment;
    }

    public String getTopCommenterName() {
        return topCommenterName;
    }

    public String getTopCommenterImageUrl() {
        return topCommenterImageUrl;
    }

    public int getLinkPreview() {
        return linkPreview;
    }

    public String getLinkPreviewUrl() {
        return linkPreviewUrl;
    }

    public String getLinkPreviewTitle() {
        return linkPreviewTitle;
    }

    public String getLinkPreviewDesc() {
        return linkPreviewDesc;
    }

    public String getLinkPreviewImageUrl() {
        return linkPreviewImageUrl;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public int getPostCommentCount() {
        return postCommentCount;
    }
}
