package com.matcher.matcher.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class ViewProfile extends AppCompatActivity {
    private static final String TAG = "ViewProfile";

    private TextView tvSportContent, tvAboutContent;
    private TextView tvName, tvNickName, tvGender, tvDOB, tvEmail, tvEducation;
    private ImageView ivEditProfile, ivProfilePicture;
    private Button btnSendMessage;
    private StorageReference profileRef;

    private String userName, userNick, userAbout, userFacebookId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        profileRef = storageRef.child("profile");

        tvSportContent = findViewById(R.id.tvSportsContent);
        tvAboutContent = findViewById(R.id.tvAboutContent);
        tvName = findViewById(R.id.tvName);
        tvGender = findViewById(R.id.tvGenderContent);
        tvNickName = findViewById(R.id.tvAlias);
        tvDOB = findViewById(R.id.tvDOBContent);
        tvEmail = findViewById(R.id.tvEmailContent);
        tvEducation = findViewById(R.id.tvEducationContent);
        btnSendMessage= findViewById(R.id.btnSignOut);
        String sendMessage = getString(R.string.send_message);
        btnSendMessage.setText(sendMessage);
        //btnSendMessage.setOnClickListener(this);
        ivEditProfile = findViewById(R.id.edit);
        ivEditProfile.setVisibility(View.INVISIBLE);
        ivProfilePicture = findViewById(R.id.profile);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userName = extras.getString(DBContract.UserTable.COL_NAME_FULLNAME, "");
            userNick = extras.getString(DBContract.UserTable.COL_NAME_NICKNAME, "");
            userAbout = extras.getString(DBContract.UserTable.COL_NAME_ABOUT, "");
            userFacebookId = extras.getString(DBContract.UserTable.COL_NAME_ABOUT, "");
            String uid = extras.getString(DBContract.UserTable.COL_NAME_UID, "");
            tvName.setText(userName);
            tvNickName.setText(userNick);
            tvAboutContent.setText(userAbout);
            if (!userFacebookId.isEmpty()) {
                getFriendProfileDataFacebook(userFacebookId);
            }
            getUserPictureProfile(uid);
        }
    }

    private void getFriendProfileDataFacebook(String facebookId) {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + facebookId,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject object = response.getJSONObject();
                        Log.i(TAG, response.toString());
                        try {
                            if (object == null) {
                                return;
                            }
                            if (object.getString("name") != null) {
                                String name = object.getString("name");
                                tvName.setText(name);
                            }
                            if (object.getString("email") != null) {
                                String email = object.getString("email");
                                tvEmail.setText(email);
                            }
                            if (object.getString("gender") != null) {
                                String gender = object.getString("gender");
                                tvGender.setText(gender);
                            }
                            if (object.getString("birthday") != null) {
                                String birthday = object.getString("birthday");
                                tvDOB.setText(birthday);
                            }
                            /*if (object.getJSONArray("education") != null) {
                                JSONArray education = object.getJSONArray("education");
                                if (education.length() > 0) {
                                    for (int i = 0; i < education.length(); i++) {
                                        if (education.getJSONObject(i).getString("type").equals("College")) {
                                            JSONObject school = education.getJSONObject(i).getJSONObject("school");
                                            String school_name = school.getString("name");
                                            tvEducation.setText(school_name);
                                        }
                                    }
                                }
                            }*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,education");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getUserPictureProfile(String uid){
        final Context context = getBaseContext();
        profileRef.child(uid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadURI = task.getResult();
                    Glide.with(context)
                            .load(downloadURI)
                            .into(ivProfilePicture);
                }
            }
        });
    }
}