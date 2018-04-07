package com.matcher.matcher.Utils;



public final class DBContract {

    private DBContract() { }

    public static class UserTable {
        public static final String TABLE_NAME = "users";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_FULLNAME = "fullName";
        public static final String COL_NAME_NICKNAME = "nickName";
        public static final String COL_NAME__EMAIL = "email";
        public static final String COL_NAME_ABOUT = "about";
        public static final String COL_NAME_SPORTS = "sports";
        public static final String COL_NAME_FACEBOOK_ID = "facebookId";
    }

    public static class SportCategoryTable {
        public static final String TABLE_NAME = "sportsCategories";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_DESC = "description";

    }

    public static class SportTable {
        public static final String TABLE_NAME = "sports";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_DESC = "description";

    }
}
