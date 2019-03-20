package com.matcher.matcher.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.matcher.matcher.R;
import com.matcher.matcher.entities.SelectableSport;

public class SelectableSportViewHolder extends RecyclerView.ViewHolder {

    public interface OnSportSelectedListener {

        void onSportSelected(SelectableSport item, View view);
    }

    public static final int MULTI_SELECTION = 2;
    public static final int SINGLE_SELECTION = 1;
    CheckedTextView textView;
    ImageView ivUser;
    SelectableSport mItem;
    SelectableSportViewHolder.OnSportSelectedListener sportSelectedListener;


    public SelectableSportViewHolder(View view, SelectableSportViewHolder.OnSportSelectedListener listener) {
        super(view);
        sportSelectedListener = listener;
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
                sportSelectedListener.onSportSelected(mItem, view);

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
