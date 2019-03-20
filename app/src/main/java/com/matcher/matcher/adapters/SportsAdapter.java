package com.matcher.matcher.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.matcher.matcher.R;
import com.matcher.matcher.entities.SelectableSport;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SportsAdapter extends RecyclerView.Adapter implements SelectableSportViewHolder.OnSportSelectedListener {

    private static final String TAG = "SportsAdapter";
    private final List<SelectableSport> mValues;
    private boolean isMultiSelectionEnabled;
    SelectableSportViewHolder.OnSportSelectedListener listener;

    public SportsAdapter(SelectableSportViewHolder.OnSportSelectedListener listener,
                         List<SelectableSport> items, boolean isMultiSelectionEnabled) {
        this.listener = listener;
        this.isMultiSelectionEnabled = isMultiSelectionEnabled;
        this.mValues = items;
    }

    @Override
    public SelectableSportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_selectable_friend, parent, false);
        return new SelectableSportViewHolder(itemView, this);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        final SelectableSportViewHolder holder = (SelectableSportViewHolder) viewHolder;
        Log.d(TAG, "mValues[" + position + "]" + mValues.get(position).getDrawableId());
        Picasso.get().load(mValues.get(position).getDrawableId()).into(holder.ivUser);
        SelectableSport selectableItem = mValues.get(position);
        String name = selectableItem.getName();
        holder.textView.setText(name);
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

    public void onSportAdded(SelectableSport aSport) {
        mValues.add(aSport);
        notifyItemChanged(mValues.size() - 1);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public List<SelectableSport> getSelectedItems() {

        List<SelectableSport> selectedItems = new ArrayList<>();
        for (SelectableSport item : mValues) {
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
    public void onSportSelected(SelectableSport item, View view) {
        if (!isMultiSelectionEnabled) {

            for (SelectableSport selectableItem : mValues) {
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
        listener.onSportSelected(item, view);
    }
}
