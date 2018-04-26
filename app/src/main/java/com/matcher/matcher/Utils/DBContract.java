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
        public static final String COL_NAME_FRIENDS = "friends";
        public static final String COL_NAME_FACEBOOK_ID = "facebookId";
        public static final String COL_NAME_DOB = "birthDate";
        public static final String COL_NAME_GENDER = "gender";
        public static final String COL_NAME_EVENTS = "events";
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

    public static class ChatsTable{
        public static final String TABLE_NAME = "chats";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_LAST_MESSAGE = "lastMessage";
        public static final String COL_NAME_TIMESTAMP = "timeStamp";
    }

    public static class MessageTable{
        public static final String TABLE_NAME = "messages";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_MESSAGE = "message";
        public static final String COL_NAME_TIMESTAMP = "timeStamp";
    }

    public static class EventsTable{
        public static final String TABLE_NAME = "events";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_DESCRIPTION = "description";
        public static final String COL_NAME_OWNER_UID = "ownerUid";
        public static final String COL_NAME_OWNER_NAME = "ownerName";
        public static final String COL_NAME_SCHEDULED_TIME = "scheduledTime";
        public static final String COL_NAME_PLACE_NAME = "placeName";
        public static final String COL_NAME_LATITUDE = "latitude";
        public static final String COL_NAME_LONGITUDE = "longitude";
    }

    public static class EventsParticipantsTable{
        public static final String TABLE_NAME = "eventsParticipants";
        public static final String COL_NAME_PARTICIPANT_UID = "uid";
        public static final String COL_NAME_PARTICIPANT_NAME = "name";
    }

    public static class FriendshipTable{
        public static final String TABLE_NAME = "friendship";
    }
}
