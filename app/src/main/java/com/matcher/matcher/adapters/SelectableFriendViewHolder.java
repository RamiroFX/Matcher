package com.matcher.matcher.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.matcher.matcher.R;
import com.matcher.matcher.entities.SelectableFriend;

public class SelectableFriendViewHolder extends RecyclerView.ViewHolder {

    public interface OnFriendSelectedListener {

        void onFriendSelected(SelectableFriend item, View view);
    }

    public static final int MULTI_SELECTION = 2;
    public static final int SINGLE_SELECTION = 1;
    CheckedTextView textView;
    ImageView ivUser;
    SelectableFriend mItem;
    OnFriendSelectedListener friendSelectedListener;


    public SelectableFriendViewHolder(View view, OnFriendSelectedListener listener) {
        super(view);
        friendSelectedListener = listener;
        ivUser = view.findViewById(R.id.iv_selectable_friend);
        textView = (CheckedTextView) view.findViewById(R.id.ctv_selectable_friend);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (mItem.isSelected() && getItemViewType() == MULTI_SELECTION) {
                    setChecked(false);
                } else {
                    setChecked(true);
                }
                friendSelectedListener.onFriendSelected(mItem, view);

            }
        });
    }

    public void setChecked(boolean value) {
        if (value) {
            textView.setBackgroundColor(Color.LTGRAY);
        } else {
            textView.setBackground(null);
        }
        mItem.setSelected(value);
        textView.setChecked(value);
    }
}