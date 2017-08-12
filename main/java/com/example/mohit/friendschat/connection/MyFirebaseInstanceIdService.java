package com.example.mohit.friendschat.connection;

import com.example.mohit.friendschat.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by mohit on 13/6/17.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String refreshToken= FirebaseInstanceId.getInstance().getToken();

        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().
                child("users").child(MainActivity.mUser.getUid().toString()).child("meta").child("firebase_token");

        databaseReference.setValue(refreshToken);
    }
}
