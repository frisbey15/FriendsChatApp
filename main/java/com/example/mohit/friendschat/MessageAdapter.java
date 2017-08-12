package com.example.mohit.friendschat;

/**
 * Created by mohit on 28/5/17.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {


    public static class MessageHolder{
        private TextView text;
        private ImageView photoImageView;
        private TextView authorTextView;

        private TextView textR;
        private ImageView photoImageViewR;
        private TextView authorTextViewR;
    }

    private static final String LOG_TAG=MessageAdapter.class.getName();

    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //using the Messageholder in order to save the findViewbyId calls that we had to do for
        //every view earlier
        MessageHolder holder;

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
            holder = new MessageHolder();
            holder.text = (TextView) convertView.findViewById(R.id.messageTextView);
            holder.photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
            holder.authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);

            holder.textR = (TextView) convertView.findViewById(R.id.messageTextViewR);
            holder.photoImageViewR = (ImageView) convertView.findViewById(R.id.photoImageViewR);
            holder.authorTextViewR = (TextView) convertView.findViewById(R.id.nameTextViewR);

            convertView.setTag(holder);
        }
        //it will check whether the sender of the last message is same as that of current message
        //and if that so then we will hide the name of sender from the above text message
        boolean last_message_sender=false;
        holder = (MessageHolder) convertView.getTag();
        FriendlyMessage message = getItem(position);

        boolean isPhoto = message.getPhotoUrl() != null;
        //checking the sender of the message in order to set the layout for the messages
        //if the sender of the message is user itself then set the right layout for the messages
        //else we will set it to left layout

       // Log.v(LOG_TAG,"-----------senders Uid is-------- :"+message.getSendersUid());

        if (message.getSendersUid().equals(MainActivity.mUser.getUid().toString())) {
           //making the left layout to be gone as we need only right layout
            holder.photoImageView.setVisibility(View.GONE);
            holder.authorTextView.setVisibility(View.GONE);
            holder.text.setVisibility(View.GONE);
            holder.authorTextViewR.setVisibility(View.VISIBLE);
            if (isPhoto) {
                holder.textR.setVisibility(View.GONE);
                holder.photoImageViewR.setVisibility(View.VISIBLE);
                Glide.with(holder.photoImageViewR.getContext())
                        .load(message.getPhotoUrl())
                        .into(holder.photoImageViewR);
            } else {

                holder.textR.setVisibility(View.VISIBLE);
                holder.textR.setBackgroundColor(Color.parseColor("#ffab91"));
                holder.photoImageViewR.setVisibility(View.GONE);
                holder.textR.setText(message.getText());
            }
            holder.authorTextViewR.setText(message.getName());

        }
        else{
            holder.photoImageViewR.setVisibility(View.GONE);
            holder.authorTextViewR.setVisibility(View.GONE);
            holder.textR.setVisibility(View.GONE);
            holder.authorTextView.setVisibility(View.VISIBLE);
            if (isPhoto) {
                holder.text.setVisibility(View.GONE);
                holder.photoImageView.setVisibility(View.VISIBLE);
                Glide.with(holder.photoImageView.getContext())
                        .load(message.getPhotoUrl())
                        .into(holder.photoImageView);
            } else {

                holder.text.setVisibility(View.VISIBLE);
                holder.text.setBackgroundColor(Color.parseColor("#80deea"));
                Log.v(LOG_TAG, "name received through get name is ---------" + message.getName());
                holder.photoImageView.setVisibility(View.GONE);
                holder.text.setText(message.getText());
            }
            holder.authorTextView.setText(message.getName());
        }

        return convertView;
    }
}