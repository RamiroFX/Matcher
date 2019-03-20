package com.matcher.matcher.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.matcher.matcher.R;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.entities.Challenge;
import com.matcher.matcher.entities.Event;
import com.matcher.matcher.entities.EventGroup;

import java.util.ArrayList;
import java.util.List;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventsViewHolder> {

    private static final String TAG = "EventsAdapter";
    private static final int VIEW_TYPE_CHALLENGE = 1;
    private static final int VIEW_TYPE_GROUP_EVENT = 2;

    private MainActivity mainActivity;
    private List<Event> mValues;
    private String userUid;

    public EventsAdapter(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.mValues = new ArrayList<>();
        this.userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public EventsAdapter.EventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CHALLENGE: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_challenge, parent, false);
                return new EventsAdapter.EventsViewHolder(view, VIEW_TYPE_CHALLENGE);
            }
            case VIEW_TYPE_GROUP_EVENT: {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.element_event, parent, false);
                return new EventsAdapter.EventsViewHolder(view, VIEW_TYPE_GROUP_EVENT);
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull EventsAdapter.EventsViewHolder holder, final int position) {
        Event item;
        if (mValues.get(position) instanceof EventGroup) {
            item = mValues.get(position);
            holder.bindEvent((EventGroup) item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onEventItemClicked(position);
                }
            });
        } else if (mValues.get(position) instanceof Challenge) {
            item = mValues.get(position);
            holder.bindChallenge((Challenge) item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChallengeItemClicked(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    private void onEventItemClicked(int position) {
        mainActivity.onEventItemClicked((EventGroup) mValues.get(position));
    }

    private void onChallengeItemClicked(int position) {
        mainActivity.onChallengeItemClicked((Challenge) mValues.get(position));
    }

    public void onEventAdded(EventGroup eventGroup) {
        mValues.add(eventGroup);
        notifyItemChanged(mValues.size() - 1);
        //notifyItemInserted(mValues.size() - 1);
    }

    public void onChallengeAdded(Challenge challenge) {
        mValues.add(challenge);
        notifyItemChanged(mValues.size() - 1);
    }

    public void onChallengeChanged(Challenge challenge) {
        int index = mValues.indexOf(challenge);
        if (index > -1) {
            mValues.set(index, challenge);
            notifyItemChanged(index);
        }
    }

    public void onEventChanged(EventGroup eventGroup) {
        int index = mValues.indexOf(eventGroup);
        if (index > -1) {
            mValues.set(index, eventGroup);
            notifyItemChanged(index);
        }
    }

    public void onEventRemoved(EventGroup eventGroup) {
        int index = mValues.indexOf(eventGroup);
        if (index > -1) {
            mValues.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, mValues.size());
        }
    }

    public void onChallengeRemoved(Challenge challenge) {
        int index = mValues.indexOf(challenge);
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

    public List<Event> getmValues() {
        return mValues;
    }

    public void setmValues(List<Event> mValues) {
        this.mValues = mValues;
    }


    @Override
    public int getItemViewType(int position) {
        if (mValues.get(position) instanceof Challenge) {
            return VIEW_TYPE_CHALLENGE;
        } else {
            return VIEW_TYPE_GROUP_EVENT;
        }
    }

    class EventsViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivIcon;
        private TextView tvName, tvSport, tvDate;
        private View itemView;

        EventsViewHolder(View itemView, int viewType) {
            super(itemView);
            this.itemView = itemView;
            switch (viewType) {
                case VIEW_TYPE_GROUP_EVENT: {
                    ivIcon = itemView.findViewById(R.id.iv_event_icon);
                    tvName = itemView.findViewById(R.id.tv_event_name);
                    tvDate = itemView.findViewById(R.id.tv_event_date);
                    break;
                }
                case VIEW_TYPE_CHALLENGE: {
                    ivIcon = itemView.findViewById(R.id.iv_challenge_icon);
                    tvName = itemView.findViewById(R.id.tv_challenge_name);
                    tvDate = itemView.findViewById(R.id.tv_challenge_schedule);
                    tvSport = itemView.findViewById(R.id.tv_challenge_sport);
                    break;
                }
            }
        }

        void bindEvent(EventGroup anEventGroup) {
            tvName.setText(anEventGroup.getEventName());
            tvDate.setText(anEventGroup.eventDate());
        }

        void bindChallenge(Challenge challenge) {
            if (userUid.equals(challenge.getChallenged().getUid())) {
                tvName.setText(challenge.getChallenger().getFullName());
            } else {
                tvName.setText(challenge.getChallenged().getFullName());
            }
            tvDate.setText(challenge.challengeDate());
            tvSport.setText(challenge.getSport().getName());
        }
    }
}