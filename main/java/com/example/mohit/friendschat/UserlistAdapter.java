package com.example.mohit.friendschat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.ui.auth.ui.User;

import java.util.ArrayList;

/**
 * Created by mohit on 7/6/17.
 */

public class UserlistAdapter extends ArrayAdapter<UserListItem> {

    public UserlistAdapter(Activity context, ArrayList<UserListItem> userListItems){
        super(context,0,userListItems);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //check if the existing view is being reused
        View listItemView=convertView;
        if(listItemView==null){
            listItemView= LayoutInflater.from(getContext()).inflate(
                    R.layout.user_list_view, parent, false);
        }
        //get the object located at this location from the list which need to be displayed in listItemView
        UserListItem currentUser=getItem(position);

        //find the textview in the user_list_item.xml file that need to be set with userDisplayName
        TextView userDisplayName=(TextView)listItemView.findViewById(R.id.userDisplayName);
        userDisplayName.setText(currentUser.getUserName());

        //return the listitemview in order to get displayed on the screen
        return listItemView;
    }
}
