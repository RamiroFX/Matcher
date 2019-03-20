package com.matcher.matcher.entities;

import android.graphics.drawable.Drawable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;

/**
 * Created by Ramiro on 04/02/2018.
 */

public class Sports {
    private int uid;
    private String name;
    private String description;
    private SportsCategories category;
    private Drawable icon;
    @Exclude
    private int drawableId;

    public Sports() {
    }

    public Sports(int uid, String name, int drawableId) {
        this.uid = uid;
        this.name = name;
        this.drawableId = drawableId;
    }

    public Sports(int uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public Sports(String name, String description, SportsCategories category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static void saveSport(DatabaseReference databaseReference, String sportUID, Sports aSport) {
        databaseReference.child(DBContract.SportTable.TABLE_NAME).child(sportUID).setValue(aSport);
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Exclude
    public int getDrawableId() {
        return drawableId;
    }
    @Exclude
    public void setDrawableId(int drawableId) {
        this.drawableId = drawableId;
    }

    @Override
    public String toString() {
        return "Sports{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                '}';
    }

    public String toJsonString() {
        return "{" +
                "\"" + Constants.SPORT_ID + "\": \"" + getUid() + "\"" +
                ", \"" + Constants.SPORT_NAME + "\": \"" + getName() + "\"" +
                "}";
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        Sports itemCompare = (Sports) obj;
        return itemCompare.getUid() == (this.getUid());

    }
}
