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
    private String fullName;
    private String nickName;
    private String email;
    private String about;
    private double rating;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private long creationDate;
    private Map<Users, Boolean> friends;
    private Map<Groups, Boolean> groups;
    private Map<Sports, Boolean> sports;

    public Users() {
    }

    public Users(String fullName, String email, double rating, double latitude, double longitude) {
        this.fullName = fullName;
        this.email = email;
        this.rating = rating;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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

    @Override
    public String toString() {
        return "Users{" +
                "fullName='" + fullName + '\'' +
                "nickName='" + nickName + '\'' +
                "email='" + email + '\'' +
                "about='" + about + '\'' +
                "rating='" + rating + '\'' +
                "imageUrl='" + imageUrl + '\'' +
                "latitude='" + latitude + '\'' +
                "longitude='" + longitude + '\'' +
                "creationDate='" + creationDate + '\'' +
                ", friends=" + friends +
                ", Groups=" + groups +
                ", Sports=" + sports +
                '}';
    }

}
