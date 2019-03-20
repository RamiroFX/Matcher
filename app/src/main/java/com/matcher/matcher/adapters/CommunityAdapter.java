package com.matcher.matcher.adapters;

import android.location.Location;
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
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.entities.CommunityFriend;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.CommunityHolder> {

    private static final int KILOMETER_METER_VALUE = 1000;
    private static final String TAG = "CommunityAdapter";
    private List<CommunityFriend> mValues;
    private StorageReference storageRef;
    private Location mLocation, comUserLocation;
    private OnCommunityAdapterListener mListener;

    public interface OnCommunityAdapterListener {
        void onCommunityAdapterInteraction(View view, CommunityFriend communityFriend);
    }

    public CommunityAdapter(OnCommunityAdapterListener mListener) {
        //this.mainActivity = mainActivity;
        this.mListener = mListener;
        this.mValues = new ArrayList<>();
        this.mLocation = new Location("User location");
        this.comUserLocation = new Location("Community location");
        //Reference to CloudStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        storageRef = storage.getReference().child(DBContract.ProfileStorage.TABLE_NAME);
    }

    @NonNull
    @Override
    public CommunityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_event_participant, parent, false);
        return new CommunityHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommunityHolder holder, int position) {
        storageRef.child(mValues.get(position).getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).rotate(90).into(holder.ivUserProfile);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Picasso.get().load(R.drawable.com_facebook_profile_picture_blank_square).into(holder.ivUserProfile);
            }
        });
        holder.tvUserName.setText(mValues.get(position).getFullName());
        comUserLocation.setLatitude(mValues.get(position).getLatitude());
        comUserLocation.setLongitude(mValues.get(position).getLongitude());
        float distance = mLocation.distanceTo(comUserLocation);
        float displayDistance = distance / KILOMETER_METER_VALUE;
        if (displayDistance < 1) {
            displayDistance = 1;//MIN DISTANCE
        }
        double distFromUser = Math.ceil(displayDistance);
        mValues.get(position).setDistanceFromUser(distFromUser);
        int mDistance = ((int) distFromUser);
        Log.d(TAG, "mLocation: " + mLocation);
        Log.d(TAG, "comUserLocation: " + comUserLocation);
        Log.d(TAG, "mDistance: " + mDistance);
        holder.tvUserDistance.setText(String.format("%s Km.", String.valueOf(mDistance)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onCommunityAdapterInteraction(holder.itemView, mValues.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public List<CommunityFriend> getmValues() {
        return mValues;
    }

    public void setmValues(List<CommunityFriend> mValues) {
        this.mValues = mValues;
        notifyDataSetChanged();
    }

    public void setUserLatitude(Double userLatitude) {
        this.mLocation.setLatitude(userLatitude);
    }

    public void setUserLongitude(Double userLongitude) {
        this.mLocation.setLongitude(userLongitude);
    }

    public void onChildAdded(CommunityFriend communityFriend) {
        mValues.add(communityFriend);
        notifyItemChanged(mValues.size() - 1);
    }

    public void onChildChanged(CommunityFriend communityFriend) {
        int index = mValues.indexOf(communityFriend);
        if (index > -1) {
            mValues.set(index, communityFriend);
            notifyItemChanged(index);
        }
    }

    public void onChildRemoved(CommunityFriend communityFriend) {
        int index = mValues.indexOf(communityFriend);
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

    public class CommunityHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserProfile;
        private TextView tvUserName, tvUserDistance;
        private View itemView;

        public CommunityHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.ivUserProfile = itemView.findViewById(R.id.iv_event_participant_icon);
            this.tvUserName = itemView.findViewById(R.id.tv_event_participant_name);
            this.tvUserDistance = itemView.findViewById(R.id.tv_event_participant_status);
        }
    }
}
