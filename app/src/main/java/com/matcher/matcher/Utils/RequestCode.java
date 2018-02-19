package com.matcher.matcher.Utils;

public enum RequestCode {
    RESULT(1, "RESULT"),
    BTN_EDIT_PROFILE(2, "Edit profile btn from PerfilFragment"),
    BTN_EDIT_ABOUT_USER(3, "Edit about user profile btn from PerfilFragment");

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
