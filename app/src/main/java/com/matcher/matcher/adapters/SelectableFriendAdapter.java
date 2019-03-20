package com.matcher.matcher.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.SelectableFriend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SelectableFriendAdapter extends RecyclerView.Adapter implements SelectableFriendViewHolder.OnFriendSelectedListener {

    private static final String TAG = "SelectableFriendAdapter";
    private StorageReference profileRef;
    private final List<SelectableFriend> mValues;
    private boolean isMultiSelectionEnabled = false;
    SelectableFriendViewHolder.OnFriendSelectedListener listener;

    public SelectableFriendAdapter(SelectableFriendViewHolder.OnFriendSelectedListener listener,
                                   List<SelectableFriend> items, boolean isMultiSelectionEnabled) {
        this.listener = listener;
        this.isMultiSelectionEnabled = isMultiSelectionEnabled;
        this.mValues = items;
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        profileRef = storageRef.child(DBContract.ProfileStorage.TABLE_NAME);
    }

    @Override
    public SelectableFriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_selectable_friend, parent, false);
        return new SelectableFriendViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final SelectableFriendViewHolder holder = (SelectableFriendViewHolder) viewHolder;
        //set profile picture
        profileRef.child(mValues.get(position).getUid()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadURI = task.getResult();
                    Picasso.get()
                            .load(downloadURI)
                            .rotate(90)
                            .into(holder.ivUser);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
        SelectableFriend selectableItem = mValues.get(position);
        holder.textView.setText(selectableItem.getFullName());
        holder.tvScore.setText(String.format("%d", selectableItem.getScore()));
        if (isMultiSelectionEnabled) {
            TypedValue value = new TypedValue();
            holder.textView.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorMultiple, value, true);
            int checkMarkDrawableResId = value.resourceId;
            holder.textView.setCheckMarkDrawable(checkMarkDrawableResId);
        } else {
            TypedValue value = new TypedValue();
            holder.textView.getContext().getTheme().resolveAttribute(android.R.attr.listChoiceIndicatorSingle, value, true);
            int checkMarkDrawableResId = value.resourceId;
            holder.textView.setCheckMarkDrawable(checkMarkDrawableResId);
        }

        holder.mItem = selectableItem;
        holder.setChecked(holder.mItem.isSelected());
    }

    public void onFriendAdded(SelectableFriend aFriend) {
        mValues.add(aFriend);
        notifyItemChanged(mValues.size() - 1);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public List<Friend> getSelectedItems() {

        List<Friend> selectedItems = new ArrayList<>();
        for (SelectableFriend item : mValues) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    @Override
    public int getItemViewType(int position) {
        if (isMultiSelectionEnabled) {
            return SelectableFriendViewHolder.MULTI_SELECTION;
        } else {
            return SelectableFriendViewHolder.SINGLE_SELECTION;
        }
    }

    @Override
    public void onFriendSelected(SelectableFriend item, View view) {
        if (!isMultiSelectionEnabled) {

            for (SelectableFriend selectableItem : mValues) {
                if (!selectableItem.equals(item)
                        && selectableItem.isSelected()) {
                    selectableItem.setSelected(false);
                } else if (selectableItem.equals(item)
                        && item.isSelected()) {
                    selectableItem.setSelected(true);
                }
            }
            notifyDataSetChanged();
        }
        listener.onFriendSelected(item, view);
    }
}