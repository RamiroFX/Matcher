package com.matcher.matcher.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideContext;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.entities.FriendItemData;
import com.matcher.matcher.fragments.FriendsFragment.OnFriendListInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FriendItemData} and makes a call to the
 * specified {@link OnFriendListInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFriendsRecyclerViewAdapter extends RecyclerView.Adapter<MyFriendsRecyclerViewAdapter.ViewHolder> {

    private List<FriendItemData> mValues;
    private Context mContext;
    private StorageReference profileRef;
    private OnFriendListInteractionListener mListener;


    public MyFriendsRecyclerViewAdapter(List<FriendItemData> items, OnFriendListInteractionListener listener, Context context) {
        mValues = items;
        mContext = context;
        mListener = listener;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        profileRef = storageRef.child("profile");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friends, parent, false);
        return new ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        profileRef.child(mValues.get(position).getId()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadURI = task.getResult();
                    Glide.with(mContext)
                            .load(downloadURI)
                            .into(holder.ivUser);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.com_facebook_profile_picture_blank_square), 100, 100);
                holder.ivUser.setImageBitmap(thumbImage);
            }
        });

        holder.tvUserName.setText(mValues.get(position).getName());
    }


    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View mView;
        public ImageView ivUser;
        private TextView tvUserName;
        public FriendItemData mItem;
        private OnFriendListInteractionListener mListener;

        public ViewHolder(View view, OnFriendListInteractionListener listener) {
            super(view);
            mView = view;
            mListener = listener;
            ivUser = view.findViewById(R.id.iv_friend);
            ivUser.setOnClickListener(this);
            tvUserName = view.findViewById(R.id.tv_name);
            tvUserName.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + tvUserName.getText() + "'";
        }

        @Override
        public void onClick(View view) {
            mListener.onFriendListInteraction(mItem, view, getAdapterPosition());
        }
    }


}
