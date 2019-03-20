package com.matcher.matcher.Utils;

/**
 * Created by Ramiro on 06/02/2018.
 */

public class Constants {

    //Shared preferences
    public static final String IS_NEVER_SHOW_AGAIN = "neverShowAgain";
    public static final String IS_USER_VISIBLE_TO_COMMUNITY = "isUserVisibleToCommunity";
    public static final String LAST_COUNTRY_CODE = "lastCountryCode";
    public static final String LAST_LATITUDE = "lastLatitude";
    public static final String LAST_LONGITUDE = "lastLongitude";
    public static final String NOTIFICATION_TOKEN = "notifToken";
    //Profile
    public static final String PROFILE_SELECT_PIC_EVENT = "SELECT_PIC_EVENT";
    public static final String PROFILE_SELECT_ALIAS_EVENT = "SELECT_ALIAS_EVENT";
    public static final String PROFILE_SELECT_SPORT_EVENT = "SELECT_SPORT_EVENT";
    public static final String PROFILE_SELECT_ABOUT_EVENT = "SELECT_ABOUT_EVENT";
    //SportsActivity
    public static final String SPORTS_ACTIVITY_TYPE = "sportActivityType";
    public static final String SPORTS_SELECTED = "SELECTED_SPORTS";
    public static final int SPORTS_ACTIVITY_TYPE_PROFILE = 1;
    public static final int SPORTS_ACTIVITY_TYPE_CHALLENGE = 2;
    //Sport entity
    public static final String SPORT_ID = "sportId";
    public static final String SPORT_NAME = "sportName";
    //CreateGroupActivity
    public static final String GROUP_ACTIVITY_TYPE = "groupActivityType";
    public static final String GROUP_CREATE_EVENT = "CREATE_GROUP_EVENT";
    public static final int GROUP_ACTIVITY_TYPE_CREATE = 1;
    public static final int GROUP_ACTIVITY_TYPE_VIEW = 2;
    public static final int GROUP_ACTIVITY_TYPE_EDIT = 3;
    //CreateChallengeActivity
    public static final String CHALLENGE_ACTIVITY_TYPE = "sportActivityType";
    public static final String CHALLENGE_CREATE_EVENT = "CREATE_CHALLENGE";
    public static final String CHALLENGE_ACCEPT_EVENT = "ACCEPT_CHALLENGE";
    public static final String CHALLENGE_DECLINE_EVENT = "DECLINE_CHALLENGE";
    public static final String CHALLENGE_ABANDON_EVENT = "ABANDON_CHALLENGE";
    public static final String CHALLENGE_FINISH_EVENT = "FINISH_CHALLENGE";
    public static final int CHALLENGE_ACTIVITY_TYPE_CREATE = 1;
    public static final int CHALLENGE_ACTIVITY_TYPE_ACCEPT = 2;
    public static final int CHALLENGE_ACTIVITY_TYPE_VIEW = 3;
    public static final int CHALLENGE_LATITUDE = 4;
    public static final int CHALLENGE_LONGITUDE = 5;
    public static final String CHALLENGE_SCORE_TYPE = "challengeScoreType";
    public static final int CHALLENGE_INCREASE_SCORE = 1;
    public static final int CHALLENGE_DECREASE_SCORE = 2;
    public static final int CHALLENGE_STANDARD_INCREASE_SCORE = 10;
    public static final int CHALLENGE_STANDARD_DECREASE_SCORE = 5;
    //ChatsFragment
    public static final String CHAT_CREATE_GROUP_EVENT = "CREATE_GROUP_CHAT_EVENT";
    public static final String CHAT_SELECT_CHAT_EVENT = "SELECT_CHAT_EVENT";
    //ChatActivity
    public static final int CHAT_VIEW_TYPE_USER_MESSAGE = 1;
    public static final int CHAT_VIEW_TYPE_FRIEND_MESSAGE = 2;
    public static final int CHAT_VIEW_TYPE_GROUP_MESSAGE = 3;
    //EventsFragment
    public static final String EVENTS_CLEAR_LOC_EVENT = "CLEAR_LOC_EVENT";
    //Community
    public static final String COMMUNITY_SHOW_ME_EVENT = "SHOW_ME_EVENT";
    public static final String COMMUNITY_HIDE_ME_EVENT = "HIDE_ME_EVENT";
    public static final String COMMUNITY_ADD_FRIEND_EVENT = "ADD_FRIEND_EVENT";
    //Fetch adress constants
    public static final int SUCCESS_RESULT = 0;
    public static final int FAILURE_RESULT = 1;
    public static final String PACKAGE_NAME = "com.matcher.matcher";
    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";


    public static class FirebaseAnalytics{
        public static final String FAVORITE_SPORT= "favorite_sport";
    }
}
