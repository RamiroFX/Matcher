package com.matcher.matcher.Utils;


public final class DBContract {

    private DBContract() {
    }

    public static class UserTable {
        public static final String TABLE_NAME = "users";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_FULLNAME = "fullName";
        public static final String COL_NAME_NICKNAME = "nickName";
        public static final String COL_NAME_LATITUDE = "latitude";
        public static final String COL_NAME_LONGITUDE = "longitude";
        public static final String COL_NAME_EMAIL = "email";
        public static final String COL_NAME_ABOUT = "about";
        public static final String COL_NAME_TIMESTAMP = "ts";
        public static final String COL_NAME_FACEBOOK_ID = "facebookId";
        public static final String COL_NAME_DOB = "birthDate";
        public static final String COL_NAME_GENDER = "gender";
        public static final String COL_NAME_SCORE = "score";
        public static final String COL_NAME_NOTIFICATION_TOKEN = "notifToken";
        public static final String COL_NAME_SPORTS = "sports";
        public static final String COL_NAME_GROUPS = "groups";
    }

    public static class UserEventsTable {
        public static final String TABLE_NAME = "userEvents";
        public static final String COL_NAME_UID = "uid";
    }

    public static class UserGroupsTable {
        public static final String TABLE_NAME = "userGroups";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_NAME = "groupName";

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

    public static class ChatsTable {
        public static final String TABLE_NAME = "chats";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_FULLNAME = "fullName";
        public static final String COL_NAME_LAST_MESSAGE = "lastMessage";
        public static final String COL_NAME_TIMESTAMP = "timeStamp";
        public static final String COL_NAME_IS_GROUP = "isGroup";
    }

    public static class MessageTable {
        public static final String TABLE_NAME = "messages";
        public static final String COL_NAME_USER = "user";
        public static final String COL_NAME_MESSAGE = "message";
        public static final String COL_NAME_TIMESTAMP = "timeStamp";
    }

    public static class EventsTable {
        public static final String TABLE_NAME = "events";
        public static final String COL_NAME_NAME = "name";
        public static final String COL_NAME_EVENT_TYPE = "eventType";
        public static final String COL_NAME_GROUP_EVENT = "groupEvent";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_DESCRIPTION = "description";
        public static final String COL_NAME_OWNER_UID = "ownerUid";
        public static final String COL_NAME_OWNER_NAME = "ownerName";
        public static final String COL_NAME_SCHEDULED_TIME = "scheduledTime";
        public static final String COL_NAME_PLACE_NAME = "placeName";
        public static final String COL_NAME_LATITUDE = "latitude";
        public static final String COL_NAME_LONGITUDE = "longitude";
    }

    public static class EventsParticipantsTable {
        public static final String TABLE_NAME = "eventsParticipants";
        public static final String COL_NAME_PARTICIPANT_UID = "uid";
        public static final String COL_NAME_PARTICIPANT_NAME = "fullName";
        public static final String COL_NAME_PARTICIPANT_STATUS = "status";
        public static final String COL_NAME_PARTICIPANT_LATITUDE = "latitude";
        public static final String COL_NAME_PARTICIPANT_LONGITUDE = "longitude";
        public static final String COL_NAME_PARTICIPANT_STATUS_PRESENT = "ok";
        public static final String COL_NAME_PARTICIPANT_STATUS_TRAVELING = "moving";
        public static final String COL_NAME_PARTICIPANT_STATUS_ARRIVED = "arrived";
    }

    public static class FriendshipTable {
        public static final String TABLE_NAME = "friendship";
        public static final String COL_NAME_SCORE = "score";
        public static final String COL_NAME_FULLNAME = "fullName";
    }

    public static class CommunityTable {
        public static final String TABLE_NAME = "community";
    }

    public static class NotificationTable {
        public static final String TABLE_NAME = "notifications";
    }

    public static class FriendRequestTable {
        public static final String TABLE_NAME = "friendRequest";
    }

    public static class ProfileStorage {
        public static final String TABLE_NAME = "profile";
    }

    public static class GroupTable {
        public static final String TABLE_NAME = "groups";
        public static final String COL_NAME_NAME = "groupName";
        public static final String COL_NAME_UID = "uid";
        public static final String COL_NAME_DESCRIPTION = "description";
        public static final String COL_NAME_OWNER_UID = "ownerUid";
        public static final String COL_NAME_OWNER_NAME = "ownerName";
        public static final String COL_NAME_LATITUDE = "latitude";
        public static final String COL_NAME_LONGITUDE = "longitude";
    }

    public static class GroupMemberTable {
        public static final String TABLE_NAME = "groupMembers";
    }

    public static class GroupChat {
        public static final String TABLE_NAME = "groupChats";
    }

    public static class ChallengeTable {
        public static final String TABLE_NAME = "challenges";
        public static final String COL_NAME_CHALLENGE_UID = "uid";
        public static final String COL_NAME_SPORT = "sport";
        public static final String COL_NAME_CHALLENGER_UID = "challengerUid";
        public static final String COL_NAME_EVENT_TYPE = "eventType";
        public static final String COL_NAME_CHALLENGE_EVENT = "challenge";
        public static final String COL_NAME_CHALLENGER = "challenger";
        public static final String COL_NAME_CHALLENGED = "challenged";
        public static final String COL_NAME_CHALLENGED_UID = "challengedUid";
        public static final String COL_NAME_SCHEDULED_TIME = "schedule";
        public static final String COL_NAME_CHALLENGE_STATUS = "status";
        public static final String COL_NAME_CHALLENGE_STATUS_PENDING = "pending";
        public static final String COL_NAME_CHALLENGE_STATUS_CANCELED = "canceled";
        public static final String COL_NAME_CHALLENGE_STATUS_ACEPTED = "acepted";
        public static final String COL_NAME_CHALLENGED_WINNER = "challengedWinner";
        public static final String COL_NAME_CHALLENGER_WINNER = "challengerWinner";
    }

    public static class ChallengeRequestsTable {
        public static final String TABLE_NAME = "challengeRequest";
        public static final String COL_NAME_CHALLENGER = "challenger";
        public static final String COL_NAME_CHALLENGER_UID = "challengerUid";
        public static final String COL_NAME_CHALLENGED = "challenged";
        public static final String COL_NAME_CHALLENGED_UID = "challengedUid";
        public static final String COL_NAME_SPORT = "sport";
        public static final String COL_NAME_SCHEDULED_TIME = "schedule";
    }

}
