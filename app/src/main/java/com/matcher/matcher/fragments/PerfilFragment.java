package com.matcher.matcher.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.activities.EditAboutUserActivity;
import com.matcher.matcher.activities.EditProfileActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.activities.SportsActivity;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.dialogs.SelectSportsDialog;
import com.matcher.matcher.entities.Users;
import com.matcher.matcher.interfaces.LogAnalyticEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PerfilFragment extends Fragment implements View.OnClickListener, ConfirmLogoutDialog.confirmLogoutDialogListener, SelectSportsDialog.SelectSportsDialogListener {

    private static final String TAG = "PerfilFragment";

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private static final String USER_GENDER = "user_gender";
    private static final String USER_BIRTHDAY = "user_birthday";
    private TextView tvSportContent, tvAboutContent, tvScoreContent;
    private TextView tvNanme, tvNickName, tvGender, tvDOB, tvEmail, tvEducation;
    private ImageView ivEditProfile, ivProfilePicture;
    private Button btnSignOut;
    private ProgressBar progressBar;

    //Firebase vars
    private DatabaseReference mDatabaseRef, mUserDatabaseRef, mScoreRef;
    private StorageReference profileRef;
    private ValueEventListener mScoreListener;
    private LogAnalyticEventListener mListener;
    //User vars
    private Uri downloadURI;
    private String userUID;
    private String userNick;
    private String userAbout;
    private String favoriteSports;
    private int score;
    private String userName;
    private String userEmail;
    private String userGender;
    private String userBirthday;
    private SharedPreferenceHelper sharedPreferenceHelper;

    public PerfilFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        profileRef = storageRef.child(DBContract.ProfileStorage.TABLE_NAME);

        userUID = sharedPreferenceHelper.getStringParameter(DBContract.UserTable.COL_NAME_UID);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabaseRef = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).child(userUID);
        mScoreRef = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).child(userUID).child(DBContract.UserTable.COL_NAME_SCORE);
        progressBar = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleLarge);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.BLACK,
                Color.RED,
                Color.GREEN,
                Color.BLUE
        };

        ColorStateList myList = new ColorStateList(states, colors);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressBackgroundTintList(myList);
        }

        //progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
        progressBar.setVisibility(View.GONE);     // To Hide ProgressBar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_perfil, container, false);
        tvSportContent = RootView.findViewById(R.id.tvSportsContent);
        tvSportContent.setOnClickListener(this);
        tvAboutContent = RootView.findViewById(R.id.tvAboutContent);
        tvAboutContent.setOnClickListener(this);
        tvScoreContent = RootView.findViewById(R.id.tvRankContent);
        tvNanme = RootView.findViewById(R.id.tvName);
        tvGender = RootView.findViewById(R.id.tvGenderContent);
        tvNickName = RootView.findViewById(R.id.tvAlias);
        tvDOB = RootView.findViewById(R.id.tvDOBContent);
        tvEmail = RootView.findViewById(R.id.tvEmailContent);
        tvEducation = RootView.findViewById(R.id.tvEducationContent);
        btnSignOut = RootView.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(this);
        ivEditProfile = RootView.findViewById(R.id.edit);
        ivEditProfile.setOnClickListener(this);
        ivProfilePicture = RootView.findViewById(R.id.profile);
        ivProfilePicture.setOnClickListener(this);
        return RootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getUserProfileDataFacebook();
        getUserProfileDataFirebase();
    }

    @Override
    public void onResume() {
        super.onResume();
        Users anUser = sharedPreferenceHelper.getUser();
        userName = anUser.getFullName();
        userNick = anUser.getNickName();
        userEmail = anUser.getEmail();
        userGender = anUser.getGender();
        userBirthday = anUser.getBirthDate();
        favoriteSports = anUser.getFavSports();
        userAbout = anUser.getAbout();
        score = anUser.getScore();

        tvNickName.setText(String.format("(%s)", userNick));
        tvAboutContent.setText(userAbout);
        tvSportContent.setText(favoriteSports);
        tvScoreContent.setText(String.valueOf(score));
        tvNanme.setText(userName);
        tvEmail.setText(userEmail);
        tvGender.setText(userGender);
        tvDOB.setText(userBirthday);
        getUserPictureProfile();
        listenUserScore();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_FULLNAME, userName);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_NICKNAME, userNick);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_EMAIL, userEmail);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_GENDER, userGender);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_DOB, userBirthday);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_SPORTS, favoriteSports);
        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_ABOUT, userAbout);
        sharedPreferenceHelper.setParameterInteger(DBContract.UserTable.COL_NAME_SCORE, score);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: " + outState);
        outState.putString(DBContract.UserTable.COL_NAME_FULLNAME, userName);
        outState.putString(DBContract.UserTable.COL_NAME_NICKNAME, userNick);
        outState.putString(DBContract.UserTable.COL_NAME_EMAIL, userEmail);
        outState.putString(USER_GENDER, userGender);
        outState.putString(USER_BIRTHDAY, userBirthday);
        outState.putString(DBContract.UserTable.COL_NAME_SPORTS, favoriteSports);
        outState.putString(DBContract.UserTable.COL_NAME_ABOUT, userAbout);
        outState.putInt(DBContract.UserTable.COL_NAME_SCORE, score);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        stopListeningScore();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored: " + savedInstanceState);
        if (savedInstanceState != null) {
            userName = savedInstanceState.getString(DBContract.UserTable.COL_NAME_FULLNAME);
            userNick = savedInstanceState.getString(DBContract.UserTable.COL_NAME_NICKNAME);
            userEmail = savedInstanceState.getString(DBContract.UserTable.COL_NAME_EMAIL);
            userGender = savedInstanceState.getString(USER_GENDER);
            userBirthday = savedInstanceState.getString(USER_BIRTHDAY);
            favoriteSports = savedInstanceState.getString(DBContract.UserTable.COL_NAME_SPORTS);
            userAbout = savedInstanceState.getString(DBContract.UserTable.COL_NAME_ABOUT);
            score = savedInstanceState.getInt(DBContract.UserTable.COL_NAME_SCORE);

            tvNickName.setText(String.format("(%s)", userNick));
            tvAboutContent.setText(userAbout);
            tvSportContent.setText(favoriteSports);
            tvScoreContent.setText(String.valueOf(score));
            tvNanme.setText(userName);
            tvEmail.setText(userEmail);
            tvGender.setText(userGender);
            tvDOB.setText(userBirthday);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignOut: {
                signOut();
                break;
            }
            case R.id.edit: {
                editProfile();
                break;
            }
            case R.id.tvSportsContent: {
                editFavoriteSportsProfile();
                break;
            }
            case R.id.tvAboutContent: {
                editAboutUserProfile();
                break;
            }
            case R.id.profile: {
                setProfilePicture();
                break;
            }
        }
    }

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {
        disconnectFromFacebookAndLogout();
    }

    public void disconnectFromFacebookAndLogout() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return; // already logged out
        }

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                .Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                ((MainActivity) getActivity()).closeApp();

            }
        }).executeAsync();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, final ArrayList sports) {
        final Context context = this.getContext();
        mUserDatabaseRef
                .child(DBContract.UserTable.COL_NAME_SPORTS)
                .setValue(sports, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(context, "Some error occured, try again", Toast.LENGTH_SHORT).show();
                        } else {
                            int sportCount = 1;
                            favoriteSports = "";
                            tvSportContent.setText(favoriteSports);
                            for (int i = 0; i < sports.size(); i++) {
                                int sportIndex = (int) sports.get(i);
                                List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                                for (int x = 0; x < myArrayList.size(); x++) {
                                    if (sportIndex == x) {
                                        if (sportCount < sports.size()) {
                                            favoriteSports = favoriteSports + myArrayList.get(x) + " - ";
                                            sportCount++;
                                        } else {
                                            favoriteSports = favoriteSports + myArrayList.get(x);
                                            sportCount++;
                                        }
                                        break;
                                    }
                                }

                            }
                            tvSportContent.setText(favoriteSports);
                        }
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LogAnalyticEventListener) {
            mListener = (LogAnalyticEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LogAnalyticEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void listenUserScore() {
        mScoreListener = mScoreRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                score = dataSnapshot.getValue(Integer.class);
                sharedPreferenceHelper.setParameterInteger(DBContract.UserTable.COL_NAME_SCORE, score);
                tvScoreContent.setText(String.valueOf(score));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void stopListeningScore() {
        if (mScoreListener != null) {
            mScoreRef.removeEventListener(mScoreListener);
            Log.d(TAG, "removeEventListener");
        }
    }

    private void getUserProfileDataFacebook() {
        Log.v(TAG, "getUserProfileDataFacebook");
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "onCompleted.response: " + response);
                        Log.d(TAG, "onCompleted.object: " + object);
                        if (object != null) {
                            // Application code
                            try {
                                userName = object.getString("name");
                                tvNanme.setText(userName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                userEmail = object.getString("email");
                                tvEmail.setText(userEmail);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                userGender = object.getString("gender");
                                tvGender.setText(userGender);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                userBirthday = object.getString("birthday");
                                tvDOB.setText(userBirthday);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "getUserProfileDataFacebook == null");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,education");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void getUserProfileDataFirebase() {
        mUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNick = dataSnapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                tvNickName.setText(String.format("(%s)", userNick));
                userAbout = dataSnapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                score = dataSnapshot.child(DBContract.UserTable.COL_NAME_SCORE).getValue(Integer.class);
                tvScoreContent.setText(String.valueOf(score));
                tvAboutContent.setText(userAbout);
                if (dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                    favoriteSports = "";
                    long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                    long childrenCount = 1;
                    for (DataSnapshot childSnapshot : dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildren()) {
                        Long sportIndex = (Long) childSnapshot.getValue();
                        List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                        for (int i = 0; i <= myArrayList.size(); i++) {
                            if (sportIndex == i) {
                                if (childrenCount < childrenSport) {
                                    favoriteSports = favoriteSports + myArrayList.get(i) + " - ";
                                    childrenCount++;
                                } else {
                                    favoriteSports = favoriteSports + myArrayList.get(i);
                                    childrenCount++;
                                }
                                break;
                            }
                        }
                    }
                    tvSportContent.setText(favoriteSports);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private void getUserPictureProfile() {
        profileRef.child(userUID).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadURI = task.getResult();
                    Picasso.get()
                            .load(downloadURI)
                            .into(ivProfilePicture);
                }
            }
        });
    }

    private void signOut() {
        String message = getString(R.string.confirm_logout);
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this, message);
        dialog.show(getChildFragmentManager(), TAG);
    }

    private void editProfile() {
        Log.i(TAG, "editProfile");
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.PROFILE_SELECT_ALIAS_EVENT, DBContract.ProfileStorage.TABLE_NAME);
        }
        Intent i = new Intent(getActivity(), EditProfileActivity.class);
        if (!TextUtils.isEmpty(userNick)) {
            i.putExtra(DBContract.UserTable.COL_NAME_NICKNAME, userNick);
        }
        startActivityForResult(i, RequestCode.BTN_EDIT_PROFILE.getCode());
    }

    private void editFavoriteSportsProfile() {
        Log.i(TAG, "editFavoriteSportsProfile");
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.PROFILE_SELECT_SPORT_EVENT, DBContract.ProfileStorage.TABLE_NAME);
        }
        Intent i = new Intent(getContext(), SportsActivity.class);
        i.putExtra(Constants.SPORTS_ACTIVITY_TYPE, Constants.SPORTS_ACTIVITY_TYPE_PROFILE);
        startActivityForResult(i, Constants.SPORTS_ACTIVITY_TYPE_PROFILE);
    }

    private void editAboutUserProfile() {
        Log.i(TAG, "editAboutUserProfile");
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.PROFILE_SELECT_ABOUT_EVENT, DBContract.ProfileStorage.TABLE_NAME);
        }
        Intent i = new Intent(getActivity(), EditAboutUserActivity.class);
        if (!TextUtils.isEmpty(userAbout)) {
            i.putExtra(DBContract.UserTable.COL_NAME_ABOUT, userAbout);
        }
        startActivityForResult(i, RequestCode.BTN_EDIT_ABOUT_USER.getCode());
    }

    private void setProfilePicture() {
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.PROFILE_SELECT_PIC_EVENT, DBContract.ProfileStorage.TABLE_NAME);
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RequestCode.IV_PROFILE_PICTURE.getCode());
    }

    private void profilePictureHandler(Intent data) {
        if (data != null) {
            Uri filePath = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), filePath);
                ivProfilePicture.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            final Context context = this.getContext();
            progressBar.setVisibility(View.VISIBLE);
            profileRef.child(userUID).putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.INVISIBLE);
                            getUserPictureProfile();
                            Toast.makeText(context, R.string.profile_picture_uploaded_message, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(context, R.string.general_error, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressBar.setProgress((int) progress);
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult requestCode:" + requestCode + "resultCode: " + resultCode);
        if (requestCode == RequestCode.BTN_EDIT_PROFILE.getCode() && resultCode == Activity.RESULT_OK) {
            String nickName = data.getExtras().getString(RequestCode.RESULT.getDescription());
            sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
            this.userNick = nickName;
        } else if (requestCode == RequestCode.BTN_EDIT_ABOUT_USER.getCode() && resultCode == Activity.RESULT_OK) {
            String aboutUser = data.getExtras().getString(RequestCode.RESULT.getDescription());
            sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_ABOUT, aboutUser);
            this.userAbout = aboutUser;
        } else if (requestCode == RequestCode.IV_PROFILE_PICTURE.getCode() && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            profilePictureHandler(data);
        } else if (requestCode == Constants.SPORTS_ACTIVITY_TYPE_PROFILE && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null) {
            favoriteSportsHandler(data);
        }
    }

    private void favoriteSportsHandler(Intent data) {
        Log.d(TAG, "favoriteSportsHandler: " + data);
        String json = data.getStringExtra(Constants.SPORTS_SELECTED);
        try {
            JSONArray jsonSportsArray = new JSONArray(json);
            Log.d(TAG, "FAVORITE_SPORTS_REQUEST: " + jsonSportsArray);
            ArrayList sportList = new ArrayList();
            for (int i = 0; i < jsonSportsArray.length(); i++) {
                JSONObject jsonFriend = jsonSportsArray.getJSONObject(i);
                String uid = jsonFriend.getString(jsonFriend.keys().next());
                if (mListener != null) {
                    mListener.setUserProperty(Constants.FirebaseAnalytics.FAVORITE_SPORT, uid);
                }
                Log.d(TAG, "uid: " + uid);
                sportList.add(uid);
            }
        } catch (Throwable t) {
            Log.e(TAG, "Could not parse malformed JSON: \"" + json + "\"");
        }
    }
}
