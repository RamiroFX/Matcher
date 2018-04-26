package com.matcher.matcher.entities;

public class SelectableFriend extends Friend {

    private boolean isSelected = false;

    public SelectableFriend(Friend item, boolean isSelected) {
        super(item.getUid(), item.getUsername());
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}