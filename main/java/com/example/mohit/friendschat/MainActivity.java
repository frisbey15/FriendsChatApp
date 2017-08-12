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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN=1;

    //referencing to all the views in activity_main.xml
    private ListView muserListView;
    private ProgressBar mProgressBar;
    //username of the signed in user
    //which will be set once the user signs in
    public static String mUsername;
    private boolean userexist=false;
    public static  FirebaseUser mUser=null;


    //firebase instance variables
    //this is the entry point for our app to access the database
    private FirebaseDatabase mFirebaseDatabase;
    //is a class that references to a specific part of the database
    private DatabaseReference mThreadsDatabaseReference;
    private DatabaseReference mUsersDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;

    private ValueEventListener mUserListListener;

    private UserlistAdapter userListAdapter;

    private String mFirebaseToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG,"--------------inside the oncreate activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initially we don't know the user name , and we will set this up once the user signs in
        mUsername = ANONYMOUS;

        //Initialize firebase components
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseStorage=FirebaseStorage.getInstance();
        mFirebaseToken= FirebaseInstanceId.getInstance().getToken();

        //getting reference to a particular node in database
        //mFirebaseDatabase.getReference refers to the root node of the database
        mThreadsDatabaseReference=mFirebaseDatabase.getReference().child("threads");
        mUsersDatabaseReference=mFirebaseDatabase.getReference().child("users");
        mChatPhotosStorageReference=mFirebaseStorage.getReference().child("chat_photos");

        //mUsersDatabaseReference.child("raju").setValue("hello there");

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        muserListView=(ListView)findViewById(R.id.userListView);

        // Initialize message ListView and its adapter
        //List<UserListItem> friendlyMessages = new ArrayList<UserListItem>();
        userListAdapter = new UserlistAdapter(this,new ArrayList<UserListItem>());
        muserListView.setAdapter(userListAdapter);

        //implement the function when any username is clicked  and start another activity and display
        //all the text messages between ths users
        muserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this,"user name clicked",Toast.LENGTH_SHORT).show();
                UserListItem userListItem=userListAdapter.getItem(position);
                final String receiverUid=userListItem.getUserUid();
                final String senderUid=mUser.getUid().toString();

                final String threadUid_type1=senderUid+"_"+receiverUid;
                final String threadUid_type2=receiverUid+"_"+senderUid;

                //set the listener for all the three cases in which
                //case 1:if the thread of conversation of type1 already exists between the users
                //case 2:if the thread of conversation of type2 already exists between the users
                //case 2:if the thread don't exists then create a new thread
                mUsersDatabaseReference.child(senderUid).child("threads")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //case when user 1 was the one who created the thread
                        if(dataSnapshot.hasChild(threadUid_type1)){
                            Intent i =new Intent(MainActivity.this,ChatActivity.class);
                            i.putExtra("thread",threadUid_type1);
                            startActivity(i);
                        }
                        //case when user 2 was the one that created the thread
                        else if(dataSnapshot.hasChild(threadUid_type2)){
                            Intent i =new Intent(MainActivity.this,ChatActivity.class);
                            i.putExtra("thread",threadUid_type2);
                            startActivity(i);
                        }
                        //no thread has been created for the users to send messages
                        //thus we need to create new thread
                        else{
                            //create a new thread of type1 and sets its value equal to 1 as an identifier
                            mUsersDatabaseReference.child(senderUid).child("threads").child(threadUid_type1).setValue(1);
                            mUsersDatabaseReference.child(receiverUid).child("threads").child(threadUid_type1).setValue(1);
                            mThreadsDatabaseReference.child(threadUid_type1).child("users").child("user1").setValue(receiverUid);
                            mThreadsDatabaseReference.child(threadUid_type1).child("users").child("user2").setValue(senderUid);
                            Intent i =new Intent(MainActivity.this,ChatActivity.class);
                            i.putExtra("thread",threadUid_type1);
                            startActivity(i);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        //created the authentication state listener
        //and we will attach it in onResume
        mAuthStateListener=new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 mUser=firebaseAuth.getCurrentUser();

                if(mUser!=null){
                    //user is signed in
                    Log.v(TAG,"user is signed in -------------------------");
                    onSignedInInitialize(mUser);
                }
                else{
                    Log.v(TAG,"user is not signed in----------------------");
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
    }



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
    }

    @Override
    protected void onResume() {
        Log.v(TAG,"----------inside the onResume acitivity");
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

        //and clear the message adapter
        //so that new data dont get appended
        userListAdapter.clear();
    }

    private void onSignedInInitialize(FirebaseUser user){
        mUsername=user.getDisplayName();
        final String userUid=user.getUid().toString();
        final String userEmail=user.getEmail();

        Log.v(TAG,"-------------the value of userexistence is :"+userexist);

        Log.v(TAG,"----------------userid is:"+user.getUid());
        Log.v(TAG,"-------------------username is :"+user.getDisplayName());
        mUsersDatabaseReference.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()==null){
                    Log.v(TAG,"inside the datasnapshot-------------------------");
                    //get the unique id of the user and add it to the users database if the user doesn't exist
                    mUsersDatabaseReference.child(userUid).child("authenticationId").setValue(userUid);
                    mUsersDatabaseReference.child(userUid).child("meta").child("email").setValue(userEmail);
                    mUsersDatabaseReference.child(userUid).child("meta").child("name").setValue(mUsername);

                    //mUsersDatabaseReference.child(user.getUid()).child("meta").child("pictureUrl").setValue(user.getPhotoUrl());
                    mUsersDatabaseReference.child(userUid).child("online").setValue(true);
                    mUsersDatabaseReference.child(userUid).child("threads").setValue(null);

                }
                //setting up the firebase token here so that whenever our app starts after uninstalling of the app,
                //if the firebase token changes then we have the updated token for us.
                mUsersDatabaseReference.child(userUid).child("meta").child("firebase_token").setValue(mFirebaseToken);

                //start listening if any change occurs in the userlist database
                attachDatabaseReadListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
       // Log.v(TAG,"the value of the userexistence is :----------------"+userexist);
        //the user will see the list of all the signed in users that are on bhundapa
        //thus if the user is signing in for the first time then , we will add the user details in
        // the users database otherwise it is already present

        //Log.v(TAG,"starting the attach dataabse readlistener----------------");

    }

    //when the user get signed out from the app then we need to clean the adapter ,
    //set the username to anonymous and userexist is now set to false
    //as when another user signs in , we want the initial value of {userexist} to be false
    private void onSignedOutCleanup(){
        mUsername=ANONYMOUS;
        //clear the messages from the adapter as no one is signed in right now
        userListAdapter.clear();
        userexist=false;
    }

    /*
    * setting up the read listener from the database only when the user is authenticated,
    * because we put restriction to our database that only authenticated user can read from it
    * thus we shifted the listener from the onCreate Method to onResume
    * */
    private void attachDatabaseReadListener(){
        //setting the value event listener to read the users list
        //and store all the elements of the user list into userlistadapter
        //to get displayed on the screen
       mUserListListener=new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               int count=0;
               for (DataSnapshot usersnapshot:dataSnapshot.getChildren()){
                   String listusername=usersnapshot.child("meta").child("name").getValue(String.class);
                   String listuserUid=usersnapshot.child("authenticationId").getValue(String.class);

                   Log.v(TAG,"the value of users is :------"+listusername);
                   //making the UserListItem object in order to add it to the adapter
                   UserListItem userlistitem=new UserListItem(listusername,listuserUid);
                   userListAdapter.add(userlistitem);
                   count++;
               }
               Log.v(TAG,"the value of the count is :----------------"+count);
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG,"--------------------------------------ERROR IN FETCHING THE USER LIST DATA");
           }
       };
       Log.v(TAG,"setting up the listener to get user list--------------------------");
        //setting the listener for single event to fetch the list of all the users
        mUsersDatabaseReference.addListenerForSingleValueEvent(mUserListListener);
    }
}