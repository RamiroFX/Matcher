package com.matcher.matcher.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
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
    private static final int VIEW_TYPE_GROUP_MESSAGE = 3;
    private Users user;
    private int type;
    private String currentDate;
    private List<Chat> chats = new ArrayList<>();
    private Uri userAvatarUri, friendAvatarUri;
    private StorageReference myProfRef, friendProfRef;

    public MessageAdapter(Users user, Friend friend, int type) {
        this.user = user;
        this.type = type;
        myProfRef = FirebaseStorage.getInstance().getReference().child(DBContract.ProfileStorage.TABLE_NAME);
        myProfRef.child(user.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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
        friendProfRef = FirebaseStorage.getInstance().getReference().child(DBContract.ProfileStorage.TABLE_NAME);
        if (type != VIEW_TYPE_GROUP_MESSAGE) {
            friendProfRef.child(friend.getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
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
        if (chats.get(position).getFriend().getUid().equals(user.getUid())) {
            return VIEW_TYPE_USER_MESSAGE;
        } else {
            return VIEW_TYPE_FRIEND_MESSAGE;
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (position > 0 && chats.get(position - 1).getFormatedDate().equals(currentDate)) {
            ((ChatViewHolder) holder).tvDate.setVisibility(View.GONE);
        } else {
            ((ChatViewHolder) holder).tvDate.setVisibility(View.VISIBLE);
            currentDate = chats.get(position).getFormatedDate();
        }
        if (type == VIEW_TYPE_GROUP_MESSAGE) {
            String friendUid= chats.get(position).getFriend().getUid();
            friendProfRef.child(friendUid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .rotate(90)
                            .resize(100, 100)
                            .error(R.drawable.com_facebook_profile_picture_blank_square)
                            .into(((ChatViewHolder) holder).ivAvatar);
                }
            });
            ((ChatViewHolder) holder).bind(chats.get(position), userAvatarUri, friendAvatarUri);
        } else {
            ((ChatViewHolder) holder).bind(chats.get(position), userAvatarUri, friendAvatarUri);
        }
    }

    public void onChatAdded(Chat chat) {
        chats.add(chat);
        notifyItemChanged(chats.size() - 1);
    }

    public void clearList() {
        chats.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDate;
        private TextView tvMessage;
        private TextView tvMessageTime;
        private TextView tvUserName;
        private ImageView ivAvatar;
        private int viewHolderType;
        private Uri userAvatarUri, friendAvatarUri;

        ChatViewHolder(View itemView, int viewHolderType, Uri avatarUri) {
            super(itemView);
            this.viewHolderType = viewHolderType;
            switch (viewHolderType) {
                case VIEW_TYPE_USER_MESSAGE: {
                    this.userAvatarUri = avatarUri;
                    this.tvDate = itemView.findViewById(R.id.tv_user_date_message_content);
                    this.tvMessage = itemView.findViewById(R.id.tv_user_message_content);
                    this.ivAvatar = itemView.findViewById(R.id.civ_user_message_avatar);
                    this.tvMessageTime = itemView.findViewById(R.id.tv_user_message_time);
                    this.tvUserName = itemView.findViewById(R.id.tv_user_message_name);
                    break;
                }
                case VIEW_TYPE_FRIEND_MESSAGE: {
                    this.friendAvatarUri = avatarUri;
                    this.tvDate = itemView.findViewById(R.id.tv_friend_date_message_content);
                    this.tvMessage = itemView.findViewById(R.id.tv_friend_message_content);
                    this.ivAvatar = itemView.findViewById(R.id.civ_friend_message_avatar);
                    this.tvMessageTime = itemView.findViewById(R.id.tv_friend_message_time);
                    this.tvUserName = itemView.findViewById(R.id.tv_friend_message_name);
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
                                .rotate(90)
                                .into(ivAvatar);
                    }
                    this.tvDate.setText(chat.getFormatedDate());
                    this.tvMessage.setText(chat.getMessage());
                    this.tvUserName.setText(chat.getFriend().getFullName());
                    this.tvMessageTime.setText(chat.getFormatedTime());
                    break;
                }
                case VIEW_TYPE_FRIEND_MESSAGE: {
                    if (friendAvatarUri != null) {
                        Picasso.get()
                                .load(friendAvatarUri)
                                .rotate(90)
                                .into(ivAvatar);
                    }
                    this.tvDate.setText(chat.getFormatedDate());
                    this.tvMessage.setText(chat.getMessage());
                    this.tvUserName.setText(chat.getFriend().getFullName());
                    this.tvMessageTime.setText(chat.getFormatedTime());
                    break;
                }
            }
        }
    }
}
