package com.matcher.matcher.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.matcher.matcher.R;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.entities.Event;

import java.util.ArrayList;
import java.util.List;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private MainActivity mainActivity;
    private List<Event> mValues;

    public EventsAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mValues = new ArrayList<>();
    }

    @NonNull
    @Override
    public EventsAdapter.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_event, parent, false);
        return new EventsAdapter.EventsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.EventsViewHolder holder, final int position) {
        Event item = mValues.get(position);
        holder.bindEvent(item);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEventItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void onEventItemClicked(int position) {
        mainActivity.onEventItemClicked(mValues.get(position));
    }

    public void onEventAdded(Event event) {
        mValues.add(0, event);
        notifyItemChanged(0);
    }

    public void onEventChanged(Event event) {
        mValues.set(0, event);
        notifyItemChanged(0);
    }

    public void clearList() {
        mValues.clear();
    }

    class EventsViewHolder extends RecyclerView.ViewHolder {


        private ImageView ivIcon;
        private TextView tvName, tvDate;
        private View itemView;

        EventsViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivIcon = itemView.findViewById(R.id.iv_event_icon);
            tvName = itemView.findViewById(R.id.tv_event_name);
            tvDate = itemView.findViewById(R.id.tv_event_date);
        }

        void bindEvent(Event anEvent) {
            //TODO retrieve profile avatar
            Bitmap thumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(itemView.getResources(), R.drawable.com_facebook_button_like_icon_selected), 100, 100);
            ivIcon.setImageBitmap(thumbImage);
            tvName.setText(anEvent.getEventName());
            tvDate.setText(anEvent.eventDate());
        }
    }
}