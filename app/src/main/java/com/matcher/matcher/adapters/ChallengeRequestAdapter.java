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
import com.matcher.matcher.entities.Challenge;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChallengeRequestAdapter extends RecyclerView.Adapter<ChallengeRequestAdapter.ChallengeRequestHolder> {

    private static final String TAG = "ChallengeRequestAdapter";
    private List<Challenge> mValues;
    private ChallengeRequestAdapter.OnChallengeRequestAdapterListener mListener;
    private StorageReference storageReference;


    public interface OnChallengeRequestAdapterListener {
        void onImageViewInteraction(Challenge friendRequest);

        void onTextViewInteraction(Challenge friendRequest);
    }

    public ChallengeRequestAdapter(ChallengeRequestAdapter.OnChallengeRequestAdapterListener mListener) {
        this.mListener = mListener;
        this.mValues = new ArrayList<>();
        this.storageReference = FirebaseStorage.getInstance().getReference().child(DBContract.ProfileStorage.TABLE_NAME);
    }

    @NonNull
    @Override
    public ChallengeRequestAdapter.ChallengeRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_friend_request, parent, false);
        return new ChallengeRequestAdapter.ChallengeRequestHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChallengeRequestAdapter.ChallengeRequestHolder holder, int position) {
        storageReference.child(mValues.get(position).getChallenger().getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
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
        holder.ivUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onImageViewInteraction(mValues.get(holder.getAdapterPosition()));
            }
        });

        holder.tvFriendName.setText(mValues.get(position).getChallenger().getFullName());
        holder.tvFriendName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onTextViewInteraction(mValues.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public List<Challenge> getmValues() {
        return mValues;
    }

    public void setmValues(List<Challenge> mValues) {
        this.mValues = mValues;
        notifyDataSetChanged();
    }

    public void onRequestAdded(Challenge friendRequest) {
        mValues.add(friendRequest);
        notifyItemChanged(mValues.size() - 1);
    }

    public void onRequestChanged(Challenge friendRequest) {
        int index = mValues.indexOf(friendRequest);
        if (index > -1) {
            mValues.set(index, friendRequest);
            notifyItemChanged(index);
        }
    }

    public void onRequestRemoved(Challenge friendRequest) {
        int index = mValues.indexOf(friendRequest);
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

    public class ChallengeRequestHolder extends RecyclerView.ViewHolder {
        private ImageView ivUserProfile;
        private TextView tvFriendName;
        private View itemView;

        public ChallengeRequestHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.ivUserProfile = itemView.findViewById(R.id.iv_friend_profile);
            this.tvFriendName = itemView.findViewById(R.id.tv_friend_name);
        }
    }
}
