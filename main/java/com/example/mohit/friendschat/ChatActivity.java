package com.example.mohit.friendschat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String LOG_TAG =ChatActivity.class.getName();

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN=1;
    public static final int RC_PHOTO_PICKER=2;

    //referencing to all the views in activity_main.xml
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;


    //username of the signed in user
    //which will be set once the user signs in
    String mUsername=MainActivity.mUsername;
    FirebaseUser mUser=null;


    //firebase instance variables
    //this is the entry point for our app to access the database
    private FirebaseDatabase mFirebaseDatabase;
    //is a class that references to a specific part of the database
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get the key value of the thread on which we want to write all the messages
        Bundle extras=getIntent().getExtras();
        if(extras==null){
            return;
        }
        String threadId=extras.getString("thread");
        Log.v(LOG_TAG,"the value of the storageId is --------------------"+threadId);

        //initially we dont know the user name , and we will set this up once the user signs in

        //Initialize firebase components
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseStorage=FirebaseStorage.getInstance();


        //getting reference to a particular node in database
        //mFirebaseDatabase.getReference refers to the root node of the database
        mMessagesDatabaseReference=mFirebaseDatabase.getReference().child("threads").child(threadId).child("messages");
        mChatPhotosStorageReference=mFirebaseStorage.getReference().child("chat_photos");


        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent,RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //send messages on click to the firebase database
                FriendlyMessage friendlyMessage=new FriendlyMessage(mUser.getUid().toString(),mMessageEditText.getText().toString(),mUsername,null);
                Log.v(LOG_TAG,"the uid is--------------------------"+mUser.getUid().toString());
                mMessagesDatabaseReference.push().setValue(friendlyMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        //created the authentication state listener
        //and we will attach it in onResume
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();

                if(user!=null){
                    mUser=user;
                    //user is signed in
                    onSignedInInitialize(user);
                }
                else{
                    //user is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    /*
    * we need to call this to exit from the app if the user is not logged in
    * because when the user hits the back button, then authListener will send us back to mainactivity
    * which in case will cause again calling the authlistener and loop will continue endlessly
    * */
    //it expects the output from the startActivityforResult which was called in onResume
    //and thus expects a output from the authapi that was called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_OK){
                Toast.makeText(this,"Signed In !!",Toast.LENGTH_SHORT).show();
            }else if(resultCode==RESULT_CANCELED){
                Toast.makeText(this,"Sign in canceled",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if(requestCode==RC_PHOTO_PICKER&& resultCode==RESULT_OK){
            Uri selectedImgaeUri=data.getData();
            //get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef=mChatPhotosStorageReference.child(selectedImgaeUri.getLastPathSegment());

            //upload file to firebase storage
            //and add on success listeners so that we can refer to the stored uri and store
            //it in our database
            photoRef.putFile(selectedImgaeUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    @SuppressWarnings("VisibleForTests")  Uri downloadurl=taskSnapshot.getDownloadUrl();
                    FriendlyMessage friendlyMessage=new FriendlyMessage(mUser.getUid().toString(),null,mUsername,downloadurl.toString());
                    Log.v(LOG_TAG,"the uid is--------------------------"+mUser.getUid().toString());
                    //take this friendly message object and store it in the database
                    mMessagesDatabaseReference.push().setValue(friendlyMessage);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        //we need to detach the databaseReadListener when the activity gets paused
        detachDatabaseReadListener();
        //and clear the message adapter
        //so that new data dont get appended
        mMessageAdapter.clear();
    }

    private void onSignedInInitialize(FirebaseUser user){

        mUsername=user.getDisplayName();
        //get the unique id of the user and add it to the users database if the user doesn't exist

        String uid=user.getUid();

        //start listening if any change occurs in image database
        attachDatabaseReadListener();
    }
    private void onSignedOutCleanup(){
        mUsername=ANONYMOUS;
        //clear the messages from the adapter as no one is signed in right now
        mMessageAdapter.clear();
        //and we need to detach the database read listener
        detachDatabaseReadListener();
    }

    /*
    * setting up the read listener from the database only when the user is authenticated,
    * because we put restriction to our database that only authenticated user can read from it
    * thus we shifted the listener from the onCreate Method to onResume
    * */
    private void attachDatabaseReadListener(){
        //setting up the listener to the message object of the database
        //so that we can get notified whenever any change occurs
        if(mChildEventListener==null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
        }
        mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
    }

    private void detachDatabaseReadListener() {
        //checks whether a database listener is been attached or not
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener=null;
        }
    }
}