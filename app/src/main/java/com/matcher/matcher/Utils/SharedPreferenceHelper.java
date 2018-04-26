package com.matcher.matcher.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.matcher.matcher.entities.Users;

public class SharedPreferenceHelper {
    private static final String TAG = "SharedPreferenceHelper";

    private static SharedPreferenceHelper instance = null;
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;


    private SharedPreferenceHelper() {
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceHelper();
            preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
            editor = preferences.edit();
        }
        return instance;
    }

    public void saveUser(Users user) {
        editor.putString(DBContract.UserTable.COL_NAME_UID, user.getUid());
        editor.putString(DBContract.UserTable.COL_NAME_FULLNAME, user.getFullName());
        editor.putString(DBContract.UserTable.COL_NAME__EMAIL, user.getEmail());
        editor.putString(DBContract.UserTable.COL_NAME_ABOUT, user.getAbout());
        editor.putString(DBContract.UserTable.COL_NAME_FACEBOOK_ID, user.getFacebookId());
        editor.putString(DBContract.UserTable.COL_NAME_NICKNAME, user.getNickName());
        editor.putString(DBContract.UserTable.COL_NAME_SPORTS, user.getFavSports());
        editor.putString(DBContract.UserTable.COL_NAME_DOB, user.getBirthDate());
        editor.putString(DBContract.UserTable.COL_NAME_GENDER, user.getGender());
        editor.apply();
    }

    public Users getUser() {
        Users user = new Users();
        user.setUid(preferences.getString(DBContract.UserTable.COL_NAME_UID, ""));
        user.setFullName(preferences.getString(DBContract.UserTable.COL_NAME_FULLNAME, ""));
        user.setEmail(preferences.getString(DBContract.UserTable.COL_NAME__EMAIL, ""));
        user.setAbout(preferences.getString(DBContract.UserTable.COL_NAME_ABOUT, ""));
        user.setFacebookId(preferences.getString(DBContract.UserTable.COL_NAME_FACEBOOK_ID, ""));
        user.setNickName(preferences.getString(DBContract.UserTable.COL_NAME_NICKNAME, ""));
        user.setFavSports(preferences.getString(DBContract.UserTable.COL_NAME_SPORTS, ""));
        user.setBirthDate(preferences.getString(DBContract.UserTable.COL_NAME_DOB, ""));
        user.setGender(preferences.getString(DBContract.UserTable.COL_NAME_GENDER, ""));
        return user;
    }
}