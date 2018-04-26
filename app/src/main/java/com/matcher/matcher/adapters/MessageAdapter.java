package com.matcher.matcher.adapters;

import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.entities.Chat;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MessageAdapter";
    private static final int VIEW_TYPE_USER_MESSAGE = 1;
    private static final int VIEW_TYPE_FRIEND_MESSAGE = 2;
    private Users user;
    private String currentDate;
    private List<Chat> chats = new ArrayList<>();
    private Uri userAvatarUri, friendAvatarUri;

    public MessageAdapter(Users user, Friend friend) {
        this.user = user;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileRef = storageRef.child("profile");
        profileRef.child(user.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    userAvatarUri = task.getResult();
                    notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                userAvatarUri = null;
            }
        });
        profileRef.child(friend.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    friendAvatarUri = task.getResult();
                    notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                friendAvatarUri = null;
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_message_user, parent, false);
                return new ChatViewHolder(itemView, VIEW_TYPE_USER_MESSAGE, userAvatarUri);
            }
            case VIEW_TYPE_FRIEND_MESSAGE: {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_message_friend, parent, false);
                return new ChatViewHolder(itemView, VIEW_TYPE_FRIEND_MESSAGE, friendAvatarUri);
            }
            default: {
                return null;
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (chats.get(position).getName().equals(user.getUid())) {
            return VIEW_TYPE_USER_MESSAGE;
        } else {
            return VIEW_TYPE_FRIEND_MESSAGE;
        }
    }



    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position > 0 && chats.get(position - 1).getFormatedDate().equals(currentDate)) {
            ((ChatViewHolder) holder).tvDate.setVisibility(View.GONE);
        } else {
            ((ChatViewHolder) holder).tvDate.setVisibility(View.VISIBLE);
            currentDate = chats.get(position).getFormatedDate();
        }
        ((ChatViewHolder) holder).bind(chats.get(position), userAvatarUri, friendAvatarUri);
    }

    public void onChatAdded(Chat chat) {
        chats.add(chat);
        notifyItemChanged(chats.size() - 1);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDate;
        private TextView tvMessage;
        private ImageView ivAvatar;
        private int viewHolderType;
        private BackgroundColorSpan timeBackground;
        private Uri userAvatarUri, friendAvatarUri;

        ChatViewHolder(View itemView, int viewHolderType, Uri avatarUri) {
            super(itemView);
            this.viewHolderType = viewHolderType;
            this.timeBackground = new BackgroundColorSpan(Color.rgb(224, 224, 224));
            switch (viewHolderType) {
                case VIEW_TYPE_USER_MESSAGE: {
                    this.userAvatarUri = avatarUri;
                    this.tvDate = itemView.findViewById(R.id.tv_user_date_message_content);
                    this.tvMessage = itemView.findViewById(R.id.tv_user_message_content);
                    this.ivAvatar = itemView.findViewById(R.id.civ_user_message_avatar);
                    break;
                }
                case VIEW_TYPE_FRIEND_MESSAGE: {
                    this.friendAvatarUri = avatarUri;
                    this.tvDate = itemView.findViewById(R.id.tv_friend_date_message_content);
                    this.tvMessage = itemView.findViewById(R.id.tv_friend_message_content);
                    this.ivAvatar = itemView.findViewById(R.id.civ_friend_message_avatar);
                    break;
                }
            }
        }

        void bind(Chat chat, Uri userAvatarUri, Uri friendAvatarUri) {
            switch (viewHolderType) {
                case VIEW_TYPE_USER_MESSAGE: {
                    if (userAvatarUri != null) {
                        Picasso.get()
                                .load(userAvatarUri)
                                .into(ivAvatar);
                    }
                    this.tvDate.setText(chat.getFormatedDate());
                    SpannableString messageContent = new SpannableString(chat.getMessage() + " " + chat.getFormatedTime());
                    messageContent.setSpan(timeBackground, chat.getMessage().length() + 1, messageContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    this.tvMessage.setText(messageContent);
                    break;
                }
                case VIEW_TYPE_FRIEND_MESSAGE: {
                    if (friendAvatarUri != null) {
                        Picasso.get()
                                .load(friendAvatarUri)
                                .into(ivAvatar);
                    }
                    this.tvDate.setText(chat.getFormatedDate());
                    SpannableString messageContent = new SpannableString(chat.getMessage() + " " + chat.getFormatedTime());
                    messageContent.setSpan(timeBackground, chat.getMessage().length() + 1, messageContent.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    this.tvMessage.setText(messageContent);
                    break;
                }
            }
        }
    }
}
