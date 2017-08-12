package com.example.mohit.friendschat;

import com.firebase.ui.auth.ui.User;

/**
 * Created by mohit on 7/6/17.
 */

public class UserListItem {
    private String mUsername;
    private String mUid;

    public UserListItem(String username,String uid){
        mUsername=username;
        mUid=uid;
    }

    public String getUserName(){
        return mUsername;
    }
    public String getUserUid(){
        return mUid;
    }
}
