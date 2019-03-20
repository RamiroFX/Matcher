package com.matcher.matcher.entities;

public class SelectableSport extends Sports{

    private boolean isSelected = false;

    public SelectableSport(Sports item, boolean isSelected) {
        super(item.getUid(), item.getName(),item.getDrawableId());
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

}
