package com.matcher.matcher.entities;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ramiro on 03/02/2018.
 */

public class Users {
    private String uid;
    private String facebookId;
    private String fullName;
    private String nickName;
    private String email;
    private String about;
    private String gender;
    private String favSports;
    private String birthDate;
    private int score;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private long creationDate;
    private Map<Users, Boolean> friends;
    private Map<Groups, Boolean> groups;
    private Map<Sports, Boolean> sports;

    public Users() {
    }

    public Users(String fullName, String email, int score, double latitude, double longitude) {
        this.fullName = fullName;
        this.email = email;
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickname) {
        this.nickName = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Map<Users, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<Users, Boolean> friends) {
        this.friends = friends;
    }

    public Map<Groups, Boolean> getGroups() {
        return groups;
    }

    public void setGroups(Map<Groups, Boolean> groups) {
        this.groups = groups;
    }

    public Map<Sports, Boolean> getSports() {
        return sports;
    }

    public void setSports(Map<Sports, Boolean> sports) {
        this.sports = sports;
    }

    public String getLargeImageUrl(String imageUrl) {
        String largeImageUrl = imageUrl.substring(0, imageUrl.length() - 6).concat("o.jpg");
        return largeImageUrl;
    }

    public static void writeNewUser(DatabaseReference databaseReference, String userUid, Users anUser) {
        databaseReference.child("users").child(userUid).setValue(anUser);
    }

    public void saveUser(String userId, String name, String email) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Users user = new Users();

        mDatabase.child("Users").child(userId).setValue(user);
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFavSports() {
        return favSports;
    }

    public void setFavSports(String favSports) {
        this.favSports = favSports;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return "Users{" +
                "uid='" + uid + '\'' +
                ", facebookId='" + facebookId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", nickName='" + nickName + '\'' +
                ", email='" + email + '\'' +
                ", about='" + about + '\'' +
                ", rating='" + score + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", friends=" + friends +
                ", Groups=" + groups +
                ", Sports=" + sports +
                '}';
    }

}
