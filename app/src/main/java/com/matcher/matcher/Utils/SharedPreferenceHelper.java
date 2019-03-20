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

    public void setParameter(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringParameter(String parameter) {
        return preferences.getString(parameter, "");
    }

    public int getIntegerParamter(String parameter) {
        return preferences.getInt(parameter, -1);
    }

    public void setParameterInteger(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public void saveUser(Users user) {
        editor.putString(DBContract.UserTable.COL_NAME_UID, user.getUid());
        editor.putString(DBContract.UserTable.COL_NAME_FULLNAME, user.getFullName());
        editor.putString(DBContract.UserTable.COL_NAME_EMAIL, user.getEmail());
        editor.putString(DBContract.UserTable.COL_NAME_ABOUT, user.getAbout());
        editor.putString(DBContract.UserTable.COL_NAME_FACEBOOK_ID, user.getFacebookId());
        editor.putString(DBContract.UserTable.COL_NAME_NICKNAME, user.getNickName());
        editor.putString(DBContract.UserTable.COL_NAME_SPORTS, user.getFavSports());
        editor.putString(DBContract.UserTable.COL_NAME_DOB, user.getBirthDate());
        editor.putString(DBContract.UserTable.COL_NAME_GENDER, user.getGender());
        editor.putInt(DBContract.UserTable.COL_NAME_SCORE, user.getScore());
        editor.apply();
    }

    public Users getUser() {
        Users user = new Users();
        user.setUid(preferences.getString(DBContract.UserTable.COL_NAME_UID, ""));
        user.setFullName(preferences.getString(DBContract.UserTable.COL_NAME_FULLNAME, ""));
        user.setEmail(preferences.getString(DBContract.UserTable.COL_NAME_EMAIL, ""));
        user.setAbout(preferences.getString(DBContract.UserTable.COL_NAME_ABOUT, ""));
        user.setFacebookId(preferences.getString(DBContract.UserTable.COL_NAME_FACEBOOK_ID, ""));
        user.setNickName(preferences.getString(DBContract.UserTable.COL_NAME_NICKNAME, ""));
        user.setFavSports(preferences.getString(DBContract.UserTable.COL_NAME_SPORTS, ""));
        user.setBirthDate(preferences.getString(DBContract.UserTable.COL_NAME_DOB, ""));
        user.setGender(preferences.getString(DBContract.UserTable.COL_NAME_GENDER, ""));
        user.setScore(preferences.getInt(DBContract.UserTable.COL_NAME_SCORE, 0));
        return user;
    }

    public void setNeverShowDialogAgain(boolean isChecked) {
        editor.putBoolean(Constants.IS_NEVER_SHOW_AGAIN, isChecked);
        editor.apply();
    }

    public boolean getDialogStatus() {
        return preferences.getBoolean(Constants.IS_NEVER_SHOW_AGAIN, false);
    }

    public void setUserVisibilityToCommunity(boolean isVisible) {
        editor.putBoolean(Constants.IS_USER_VISIBLE_TO_COMMUNITY, isVisible);
        editor.apply();
    }

    public boolean isUserVisibleToCommunity() {
        return preferences.getBoolean(Constants.IS_USER_VISIBLE_TO_COMMUNITY, false);
    }

    public void setLastCountryCode(String lastCountryCode) {
        editor.putString(Constants.LAST_COUNTRY_CODE, lastCountryCode);
        editor.apply();
    }

    public String getLastCountryCode() {
        return preferences.getString(Constants.LAST_COUNTRY_CODE, "");
    }

    public void setLastLatitude(String lastLatitude) {
        editor.putString(Constants.LAST_LATITUDE, lastLatitude);
        editor.apply();
    }

    public String getLastLatitude() {
        return preferences.getString(Constants.LAST_LATITUDE, "0.0");
    }

    public void setLastLongitude(String longitude) {
        editor.putString(Constants.LAST_LONGITUDE, longitude);
        editor.apply();
    }

    public String getLastLongitude() {
        return preferences.getString(Constants.LAST_LONGITUDE, "0.0");
    }

    public void setNotificationToken(String notificationToken) {
        editor.putString(Constants.NOTIFICATION_TOKEN, notificationToken);
        editor.apply();
    }

    public String getNotificationToken() {
        return preferences.getString(Constants.NOTIFICATION_TOKEN, "");
    }
}