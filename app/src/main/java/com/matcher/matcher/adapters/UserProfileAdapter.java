package com.matcher.matcher.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matcher.matcher.R;
import com.matcher.matcher.entities.FriendItemData;
import com.matcher.matcher.fragments.FriendsFragment;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Ramiro on 11/03/2018.
 */

public class UserProfileAdapter extends RecyclerView.Adapter<UserProfileAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(HashMap item);
    }
    private List<HashMap> mValues;
    private final OnItemClickListener mListener;

    public UserProfileAdapter(List<HashMap> mValues, OnItemClickListener listener) {
        this.mValues = mValues;
        this.mListener = listener;
    }


    @Override
    public UserProfileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_friends, parent, false);
        return new UserProfileAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final UserProfileAdapter.ViewHolder holder, final int position) {
        //holder.ivUser.setText(mValues.get(position).getId());
        holder.tvLabel.setText(mValues.get(position).get("label").toString());
        holder.tvValue.setText(mValues.get(position).get("value").toString());
        holder.ivIcon.setImageResource((Integer) mValues.get(position).get("icon"));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onItemClick(mValues.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        TextView tvLabel, tvValue;
        ImageView ivIcon;

        ViewHolder(View view) {
            super(view);
            mView = view;
            ivIcon = view.findViewById(R.id.iv_icon);
            tvLabel = view.findViewById(R.id.tv_label);
            tvValue = view.findViewById(R.id.tv_value);
        }
    }
}
