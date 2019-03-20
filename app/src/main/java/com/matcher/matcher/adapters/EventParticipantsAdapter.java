package com.matcher.matcher.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.entities.EventParticipant;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class EventParticipantsAdapter extends RecyclerView.Adapter<EventParticipantsAdapter.EventParticipantsHolder> {

    private List<EventParticipant> mValues;
    private StorageReference storageRef;

    public EventParticipantsAdapter() {
        this.mValues = new ArrayList<>();
        this.storageRef = FirebaseStorage.getInstance().getReference().child(DBContract.ProfileStorage.TABLE_NAME);
    }

    @NonNull
    @Override
    public EventParticipantsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_event_participant, parent, false);
        return new EventParticipantsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final EventParticipantsHolder holder, int position) {
        this.storageRef.child(mValues.get(position).getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).rotate(90).resize(100, 100).into(holder.ivUserProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get().load(R.drawable.com_facebook_profile_picture_blank_square).resize(100, 100).into(holder.ivUserProfile);
            }
        });
        holder.tvUserName.setText(mValues.get(position).getFullName());
        holder.tvUserStatus.setText(mValues.get(position).getStatus());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void setmValues(List<EventParticipant> mValues) {
        this.mValues = mValues;
        notifyDataSetChanged();
    }

    public List<EventParticipant> getmValues() {
        return mValues;
    }

    public void setEventParticipantStatus(String uid, String status) {
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setUid(uid);
        int index = mValues.indexOf(eventParticipant);
        if (index > -1) {
            mValues.get(index).setStatus(status);
            notifyItemChanged(index);
        }
    }

    public void onEventParticipantAdded(EventParticipant eventParticipant) {
        mValues.add(eventParticipant);
        notifyItemChanged(mValues.size() - 1);
    }

    public void onEventParticipantChanged(EventParticipant eventParticipant) {
        int index = mValues.indexOf(eventParticipant);
        if (index > -1) {
            mValues.set(index, eventParticipant);
            notifyItemChanged(index);
        }
    }

    public void clearList() {
        mValues.clear();
        notifyDataSetChanged();
    }

    public class EventParticipantsHolder extends RecyclerView.ViewHolder {

        private ImageView ivUserProfile;
        private TextView tvUserName, tvUserStatus;

        public EventParticipantsHolder(View itemView) {
            super(itemView);
            this.ivUserProfile = itemView.findViewById(R.id.iv_event_participant_icon);
            this.tvUserName = itemView.findViewById(R.id.tv_event_participant_name);
            this.tvUserStatus = itemView.findViewById(R.id.tv_event_participant_status);
        }
    }
}
