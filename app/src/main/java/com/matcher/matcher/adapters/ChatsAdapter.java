package com.matcher.matcher.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.entities.ChatHeader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder> {

    private static final String TAG = "ChatsAdapter";
    private MainActivity mainActivity;
    private List<ChatHeader> mValues;
    private StorageReference storageRef;

    public ChatsAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mValues = new ArrayList<>();
        //Reference to CloudStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference().child(DBContract.ProfileStorage.TABLE_NAME);
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_chat_item, parent, false);
        return new ChatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder");
        if (mValues.get(position).getIsGroup()) {
            Log.d(TAG, "GroupChatHeader");
            ChatHeader item = mValues.get(position);
            holder.bindChat(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGroupChatItemClicked(position);
                }
            });
        } else {
            Log.d(TAG, "ChatHeader");
            ChatHeader item = mValues.get(position);
            holder.bindChat(item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChatItemClicked(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void onChatItemClicked(int position) {
        mainActivity.onChatItemClicked(mValues.get(position));
    }

    private void onGroupChatItemClicked(int position) {
        mainActivity.onGroupChatItemClicked(mValues.get(position));
    }

    public void onChatAdded(ChatHeader chatHeader) {
        mValues.add(0, chatHeader);
        notifyItemChanged(0);
    }

    public void onChatChanged(ChatHeader chatHeader) {
        int index = mValues.indexOf(chatHeader);
        if (index > -1) {
            mValues.set(index, chatHeader);
            notifyItemChanged(index);
        }
    }

    public void onChatRemoved(ChatHeader chatHeader) {
        int index = mValues.indexOf(chatHeader);
        if (index > -1) {
            mValues.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, mValues.size());
        }
    }

    public void clearList() {
        mValues.clear();
        notifyDataSetChanged();
    }

    class ChatsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProfile;
        private TextView tvName, tvLastChat, tvTime;
        private View itemView;

        ChatsViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.ivProfile = itemView.findViewById(R.id.iv_chat_profile);
            this.tvName = itemView.findViewById(R.id.tv_chat_name);
            this.tvLastChat = itemView.findViewById(R.id.tv_last_chat);
            this.tvTime = itemView.findViewById(R.id.tv_chat_timestamp);
        }

        void bindChat(ChatHeader ch) {
            Log.d(TAG, "bindChat: " + ch);
            if (ch.getIsGroup()) {
                tvLastChat.setText(ch.getLastMessage());
                tvName.setText(ch.getFullName());
                tvTime.setText(ch.getParseTimeStamp());
                Picasso.get()
                        .load(R.drawable.icons8_conferencia_100)
                        .resize(100, 100)
                        .error(R.drawable.com_facebook_profile_picture_blank_square)
                        .into(ivProfile);
            } else {
                String friendUid = ch.getUid();
                storageRef.child(friendUid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get()
                                .load(uri)
                                .resize(100, 100)
                                .rotate(90)
                                .error(R.drawable.com_facebook_profile_picture_blank_square)
                                .into(ivProfile);
                    }
                });
                tvName.setText(ch.getFullName());
                tvLastChat.setText(ch.getLastMessage());
                tvTime.setText(ch.getParseTimeStamp());
            }
        }

        /*void bindGroupChat(GroupChatHeader groupChatHeader) {
            Log.d(TAG, "bindGroupChat: " + groupChatHeader);
            tvName.setText(groupChatHeader.getFullName());
            tvTime.setText(groupChatHeader.getParseTimeStamp());
        }*/
    }
}
