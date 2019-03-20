package com.matcher.matcher.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.entities.Challenge;
import com.matcher.matcher.entities.ChatHeader;
import com.matcher.matcher.entities.Event;
import com.matcher.matcher.fragments.ChatsFragment;
import com.matcher.matcher.fragments.CommunityFragment;
import com.matcher.matcher.fragments.EventsFragment;
import com.matcher.matcher.fragments.PerfilFragment;
import com.matcher.matcher.interfaces.LogAnalyticEventListener;
import com.matcher.matcher.services.LocationService;

public class MainActivity extends AppCompatActivity implements LocationService.LocationServiceListener, LogAnalyticEventListener {

    public interface OnLocationUpdateListener {
        void onLocationUpdated(LocationResult locationResult);
    }

    public interface OnRequestedPermissionListener {
        void myOnRequestPermissionsResult(int requestCode,
                                          String permissions[], int[] grantResults);
    }

    private static final String TAG = "MainActivity";
    private static final String FRAGMENTS[] = {"PerfilFragment", "EventsFragment", "ChatsFragment", "CommunityFragment"};

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private MenuItem itemFriendReq, itemChallengeReq;
    private OnLocationUpdateListener mOnLocationUpdateListener;
    private OnRequestedPermissionListener mOnRequestedPermissionListener;
    /*
    Firebase references
     */
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference mDatabaseReference, mFriendReqRef, mChallengeReqRef;
    private ChildEventListener mChildFriendListener, mChildChallengeListener;
    private boolean hasFriendRequest, hasChallengeRequest, areListenersActive;
    private int friendReqNum, challengesNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        hasFriendRequest = false;
        hasChallengeRequest = false;
        areListenersActive = false;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                logScreenView(tab.getPosition());
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        String UID = sharedPreferenceHelper.getUser().getUid();
        if (UID.isEmpty()) {
            try {
                UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_UID, UID);
            } catch (NullPointerException e) {
                String message = getString(R.string.error_ocurrred_message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        }
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFriendReqRef = mDatabaseReference.child(DBContract.FriendRequestTable.TABLE_NAME).child(UID);
        mChallengeReqRef = mDatabaseReference.child(DBContract.ChallengeRequestsTable.TABLE_NAME).child(UID);
        //TODO LLAMAR DESDE EL SERVICICIO
        /*String notifToken = FirebaseInstanceId.getInstance().getToken();
        mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(UID).child(DBContract.UserTable.COL_NAME_NOTIFICATION_TOKEN).setValue(notifToken);*/
        String notifToken = sharedPreferenceHelper.getNotificationToken();
        if (notifToken.isEmpty()) {
            notifToken = FirebaseInstanceId.getInstance().getToken();
            mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(UID).child(DBContract.UserTable.COL_NAME_NOTIFICATION_TOKEN).setValue(notifToken);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        friendReqNum = 0;
        challengesNum = 0;
        hasChallengeRequest = false;
        hasFriendRequest = false;
        addListeners();
        checkFriendRequest();
        checkChallengeRequest();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        removeListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: " + menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemFriendReq = menu.findItem(R.id.menu_item_friend_request);
        itemChallengeReq = menu.findItem(R.id.menu_item_challenge_request);
        addListeners();
        checkFriendRequest();
        checkChallengeRequest();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onCreateOptionsMenu: " + item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.menu_item_friend_request: {
                showFriendshipRequest();
                break;
            }
            case R.id.menu_item_challenge_request: {
                showChallengesRequest();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFriendshipRequest() {
        Intent i = new Intent(getApplicationContext(), FriendRequestActivity.class);
        i.putExtra(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), RequestCode.CHAT_FRIENDS_REQUEST.getCode());
        startActivity(i);
    }

    private void showChallengesRequest() {
        Intent i = new Intent(getApplicationContext(), ChallengeRequestActivity.class);
        i.putExtra(RequestCode.CHALLENGE_FRIENDS_REQUEST.getDescription(), RequestCode.CHALLENGE_FRIENDS_REQUEST.getCode());
        startActivity(i);
    }

    private void addListeners() {
        if (!areListenersActive && itemChallengeReq != null && itemFriendReq != null) {
            listenChallengeRequest();
            listenFriendRequest();
            areListenersActive = true;
        }
    }

    private void listenFriendRequest() {
        Log.d(TAG, "listenFriendRequest");
        final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_notification_100_plus, null);
        mChildFriendListener = mFriendReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                itemFriendReq.setIcon(iconPlus);
                friendReqNum++;
                hasFriendRequest = true;
                checkFriendRequest();
                Log.d(TAG, "listenFriendRequest.onChildAdded: " + friendReqNum);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                friendReqNum--;
                if (friendReqNum < 1) hasFriendRequest = false;
                checkFriendRequest();
                Log.d(TAG, "listenFriendRequest.onChildRemoved: " + friendReqNum);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void listenChallengeRequest() {
        Log.d(TAG, "listenChallengeRequest");
        final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_apreton_manos_100_plus, null);
        mChildChallengeListener = mChallengeReqRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                itemChallengeReq.setIcon(iconPlus);
                challengesNum++;
                hasChallengeRequest = true;
                checkChallengeRequest();
                Log.d(TAG, "listenChallengeRequest.onChildAdded: " + challengesNum);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                challengesNum--;
                if (challengesNum < 1) hasChallengeRequest = false;
                checkChallengeRequest();
                Log.d(TAG, "listenChallengeRequest.onChildRemoved: " + challengesNum);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkFriendRequest() {
        if (hasFriendRequest && itemFriendReq != null) {
            final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_notification_100_plus, null);
            itemFriendReq.setIcon(iconPlus);
        } else if (!hasFriendRequest && itemFriendReq != null) {
            final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_notification_100, null);
            itemFriendReq.setIcon(iconPlus);
        }
    }

    private void checkChallengeRequest() {
        if (hasChallengeRequest && itemChallengeReq != null) {
            final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_apreton_manos_100_plus, null);
            itemChallengeReq.setIcon(iconPlus);
        } else if (!hasChallengeRequest && itemChallengeReq != null) {
            final Drawable iconPlus = ResourcesCompat.getDrawable(getResources(), R.drawable.icons8_apreton_100, null);
            itemChallengeReq.setIcon(iconPlus);
        }
    }

    private void removeListeners() {
        Log.d(TAG, "removeListeners");
        removeChallengeRequestListener();
        removeFriendRequestListener();
        areListenersActive = false;
    }

    private void removeFriendRequestListener() {
        if (mChildFriendListener != null) {
            mFriendReqRef.removeEventListener(mChildFriendListener);
        }
    }

    private void removeChallengeRequestListener() {
        if (mChildChallengeListener != null) {
            mChallengeReqRef.removeEventListener(mChildChallengeListener);
        }
    }


    public void onChatItemClicked(ChatHeader chatHeader) {
        if (chatHeader != null) {
            String uid = chatHeader.getUid();
            String fullName = chatHeader.getFullName();
            Intent i = new Intent(getApplicationContext(), ChatActivity.class);
            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
            i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
            i.putExtra(DBContract.GroupTable.COL_NAME_NAME, false);
            startActivity(i);
        }
    }

    public void onGroupChatItemClicked(ChatHeader chatHeader) {
        if (chatHeader != null) {
            String uid = chatHeader.getUid();
            String fullName = chatHeader.getFullName();
            Intent i = new Intent(getApplicationContext(), ChatActivity.class);
            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
            i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
            i.putExtra(DBContract.GroupTable.COL_NAME_NAME, true);
            startActivity(i);
        }
    }

    public void viewEvent(Event eventGroup) {
        String uid = eventGroup.getUid();
        Intent i = new Intent(getApplicationContext(), ViewEventActivity.class);
        i.putExtra(DBContract.EventsTable.COL_NAME_UID, uid);
        startActivity(i);
    }

    public void onEventItemClicked(Event eventGroup) {
        viewEvent(eventGroup);
    }

    public void onChallengeItemClicked(Challenge challenge) {
        Log.d(TAG, "onChallengeItemClicked: " + challenge.getUid());
        String uid = challenge.getUid();
        Intent i = new Intent(getApplicationContext(), CreateDuelActivity.class);
        i.putExtra(DBContract.ChallengeTable.COL_NAME_CHALLENGE_UID, uid);
        i.putExtra(Constants.CHALLENGE_ACTIVITY_TYPE, Constants.CHALLENGE_ACTIVITY_TYPE_VIEW);
        startActivity(i);
    }


    private void myLogAnalyticEvent(String name, String contentType) {
        Log.d(TAG, "myLogAnalyticEvent.name: " + name + "contentType: " + contentType);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    public void closeApp() {
        finish();
    }

    @Override
    public void onLocationUpdate(LocationResult locationResult) {
        Log.d(TAG, "onLocationUpdate: " + locationResult);
        mOnLocationUpdateListener.onLocationUpdated(locationResult);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        mOnRequestedPermissionListener.myOnRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void logAnalyticEvent(String name, String contentType) {
        myLogAnalyticEvent(name, contentType);
    }

    @Override
    public void setUserProperty(String property, String uid) {
        mFirebaseAnalytics.setUserProperty(Constants.FirebaseAnalytics.FAVORITE_SPORT, uid);
    }

    public void setmOnLocationUpdateListener(OnLocationUpdateListener mOnLocationUpdateListener) {
        this.mOnLocationUpdateListener = mOnLocationUpdateListener;
    }

    public void setmOnRequestedPermissionListener(OnRequestedPermissionListener mOnRequestedPermissionListener) {
        this.mOnRequestedPermissionListener = mOnRequestedPermissionListener;
    }

    private void logScreenView(int pos) {
        //Save the current fragment to analytics
        // This string must be <= 36 characters long in order for setCurrentScreen to succeed.
        String screenName = FRAGMENTS[pos];

        // [START set_current_screen]
        mFirebaseAnalytics.setCurrentScreen(this, screenName, null /* class override */);
        // [END set_current_screen]
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        MainActivity mainActivity;

        public SectionsPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
            super(fm);
            this.mainActivity = mainActivity;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PerfilFragment();
                case 1:
                    return new EventsFragment();
                case 2:
                    return new ChatsFragment();
                case 3:
                    CommunityFragment communityFragment = CommunityFragment.newInstance();
                    setmOnLocationUpdateListener(communityFragment);
                    setmOnRequestedPermissionListener(communityFragment);
                    return communityFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

    }

}

