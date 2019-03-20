package com.matcher.matcher.Utils;

public enum RequestCode {
    RESULT(1, "RESULT"),
    BTN_EDIT_PROFILE(2, "Edit user nick btn from PerfilFragment"),
    BTN_EDIT_ABOUT_USER(3, "Edit user about btn from PerfilFragment"),
    IV_PROFILE_PICTURE(4, "SET profile picture from FriendFragment"),
    INVITED_FRIENDS_REQUEST(5, "Request friends from CreateEventActivity"),
    CHAT_FRIENDS_REQUEST(6, "Request friends from ChatActivity"),
    CHALLENGE_FRIENDS_REQUEST(7, "Request friend from CreateDuelActivity"),;

    private int code;
    private String description;

    RequestCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int codigo) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}
