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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.entities.ChatHeader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>{

    private static final String TAG = "ChatsAdapter";
    private MainActivity mainActivity;
    private List<ChatHeader> mValues;
    private StorageReference storageRef;
    public ChatsAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mValues= new ArrayList<>();
        //Reference to CloudStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference().child("profile");
    }

    @NonNull
    @Override
    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_chat_item, parent, false);
        return new ChatsViewHolder(view, storageRef);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatsViewHolder holder, final int position) {
        ChatHeader item = mValues.get(position);
        holder.bindChat(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChatItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void onChatItemClicked(int position) {
        mainActivity.onChatItemClicked(mValues.get(position));
    }

    public void onChatAdded(ChatHeader chatHeader) {
        /*mValues.add(chatHeader);
        notifyItemChanged(mValues.size()-1);*/
        mValues.add(0,chatHeader);
        notifyItemChanged(0);
    }

    public void onChatChanged(ChatHeader chatHeader) {
        mValues.set(0, chatHeader);
        notifyItemChanged(0);
    }

    public void clearList() {
        mValues.clear();
    }

    class ChatsViewHolder extends RecyclerView.ViewHolder{

        private StorageReference pathReference;
        private ImageView ivProfile;
        private TextView tvName, tvLastChat, tvTime;
        private View itemView;

        ChatsViewHolder(View itemView, StorageReference pathReference) {
            super(itemView);
            this.itemView=itemView;
            this.pathReference= pathReference;
            this.ivProfile = itemView.findViewById(R.id.iv_chat_profile);
            this.tvName = itemView.findViewById(R.id.tv_chat_name);
            this.tvLastChat = itemView.findViewById(R.id.tv_last_chat);
            this.tvTime = itemView.findViewById(R.id.tv_chat_timestamp);
        }

        void bindChat(ChatHeader ch) {
            Log.d(TAG,"bindChat: "+ch);
            String friendUid = ch.getUid();
            storageRef.child(friendUid).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get()
                            .load(uri)
                            .resize(100, 100)
                            .error(R.drawable.com_facebook_profile_picture_blank_square)
                            .into(ivProfile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d(TAG,"onFailure: "+exception);
                }
            });
            tvName.setText(ch.getName());
            tvLastChat.setText(ch.getLastMessage());
            tvTime.setText(ch.getParseTimeStamp());
        }
    }
}