package com.example.mohit.friendschat;

    /**
 * Created by mohit on 28/5/17.
 */

public class FriendlyMessage {

    private String sendersUid;
    private String text;
    private String sendersName;
    private String photoUrl;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String uid,String text, String name, String photoUrl) {
        this.sendersUid=uid;
        this.text = text;
        this.sendersName = name;
        this.photoUrl = photoUrl;

    }

    public String getSendersUid(){return sendersUid;}
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return sendersName;
    }

    public void setName(String name) {
        this.sendersName = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}