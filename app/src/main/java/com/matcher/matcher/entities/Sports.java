package com.matcher.matcher.entities;

import com.google.firebase.database.DatabaseReference;
import com.matcher.matcher.Utils.DBContract;

/**
 * Created by Ramiro on 04/02/2018.
 */

public class Sports {
    private String uid;
    private String name;
    private String description;
    private SportsCategories category;

    public Sports() {
    }

    public Sports(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Sports(String name, String description, SportsCategories category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
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
    @Override
    public String toString() {
        return "Sports{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category=" + category +
                '}';
    }
}
