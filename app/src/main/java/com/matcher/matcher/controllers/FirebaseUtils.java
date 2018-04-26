package com.matcher.matcher.controllers;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;

import java.lang.ref.WeakReference;

public class FirebaseUtils extends AsyncTask<Void, Void, Void> {
    private String userUid;
    private WeakReference<ImageView> ivProfile;
    private StorageReference firebaseStorage;
    private WeakReference<Context> weakContext;

    public FirebaseUtils(String userUid, ImageView ivProfile, StorageReference firebaseStorage, Context context) {
        this.userUid = userUid;
        this.ivProfile = new WeakReference<> (ivProfile);
        this.firebaseStorage = firebaseStorage;
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (weakContext!=null){
                firebaseStorage.child(userUid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadURI = task.getResult();
                            Glide.with(weakContext.get())
                                    .load(downloadURI)
                                    .into(ivProfile.get());
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }
}
