package com.matcher.matcher.entities;

import com.google.firebase.database.DatabaseReference;
import com.matcher.matcher.Utils.DBContract;

import java.util.Map;

/**
 * Created by Ramiro on 08/02/2018.
 */

public class SportsCategories {
    private String uid;
    private String name;
    private String description;
    private Map<String, Boolean> sports;

    public SportsCategories(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public SportsCategories(String name, String description, Map<String, Boolean> sports) {
        this.name = name;
        this.description = description;
        this.sports = sports;
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

    public Map<String, Boolean> getSports() {
        return sports;
    }

    public void setSports(Map<String, Boolean> sports) {
        this.sports = sports;
    }

    public static void saveSportCategory(DatabaseReference databaseReference, SportsCategories aCategory) {
        databaseReference.child(DBContract.SportCategoryTable.TABLE_NAME).push();
        databaseReference.setValue(aCategory);
    }

    @Override
    public String toString() {
        return "SportsCategories{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", sports=" + sports +
                '}';
    }
}
