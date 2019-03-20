package com.matcher.matcher.entities;

public class SelectableFriend extends ScoredFriend {

    private boolean isSelected = false;

    public SelectableFriend(ScoredFriend item, boolean isSelected) {
        super(item.getUid(), item.getFullName(), item.getScore());
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}