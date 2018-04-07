package com.matcher.matcher.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.adapters.MyFriendsRecyclerViewAdapter;
import com.matcher.matcher.entities.FriendItemData;
import com.matcher.matcher.entities.Users;
import com.matcher.matcher.fragments.ChatsFragment;
import com.matcher.matcher.fragments.FriendsFragment;
import com.matcher.matcher.fragments.PerfilFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FriendsFragment.OnFriendListInteractionListener, ChatsFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    /*
    Firebase references
     */

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getFriendList() {
        AccessToken token = AccessToken.getCurrentAccessToken();
        if (token.isExpired()) {
            Toast.makeText(getApplicationContext(), "El token expiro", Toast.LENGTH_LONG);
            return;
        }
        GraphRequest graphRequest = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                try {
                    Log.i(TAG, graphResponse.toString());
                    JSONArray jsonArrayFriends = jsonObject.getJSONObject("friendlists").getJSONArray("data");
                    JSONObject friendlistObject = jsonArrayFriends.getJSONObject(0);
                    String friendListID = friendlistObject.getString("id");
                    myNewGraphReq(friendListID);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "friendlists,member");
        graphRequest.setParameters(param);
        graphRequest.executeAsync();
    }

    private void myNewGraphReq(String friendlistId) {
        final String graphPath = "/" + friendlistId + "/members/";
        AccessToken token = AccessToken.getCurrentAccessToken();
        GraphRequest request = new GraphRequest(token, graphPath, null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse graphResponse) {
                JSONObject object = graphResponse.getJSONObject();
                try {
                    JSONArray arrayOfUsersInFriendList = object.getJSONArray("data");
                    /* Do something with the user list */
                    /* ex: get first user in list, "name" */
                    JSONObject user = arrayOfUsersInFriendList.getJSONObject(0);
                    String usersName = user.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        Bundle param = new Bundle();
        param.putString("fields", "name");
        request.setParameters(param);
        request.executeAsync();
    }



    @Override
    public void onFriendListInteraction(FriendItemData mItem, final View view, int position) {
        Log.d(TAG,"onFriendListInteraction: "+mItem);
        Query query = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).
                orderByChild(DBContract.UserTable.COL_NAME_FACEBOOK_ID).
                equalTo(mItem.getId()).
                limitToFirst(1);
        Log.d(TAG,"onDataChange: "+mItem.getId());
        Log.d(TAG,"onDataChange: "+query);
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG,"onDataChange: "+dataSnapshot);
                if (dataSnapshot.exists()) {
                    Log.d(TAG,"dataSnapshot.exists()");
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        //ViewFriendProfile(snapshot);
                        if(view instanceof ImageView){
                            ViewFriendProfile(snapshot);
                        }else if(view instanceof TextView){
                            chatActivity(snapshot);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getFriendProfileDataFacebook(String facebookId) {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + facebookId + "?fields=birthday,education,email,gender",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject object = response.getJSONObject();
                        Log.i(TAG, object.toString());
                        try {
                            String name;
                            if (object.has("name")) {
                                name = object.getString("name");
                            }
                            if (object.has("gender")) {

                            }
                            if (object.has("birthday")) {

                            }
                            if (object.has("education")) {
                                JSONArray education = object.getJSONArray("education");
                                if (education != null && education.length() > 0) {
                                    for (int i = 0; i < education.length(); i++) {
                                        if (education.getJSONObject(i).getString("type").equals("College")) {
                                            JSONObject school = education.getJSONObject(i).getJSONObject("school");
                                            String school_name = school.getString("name");
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,gender,education");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void ViewFriendProfile(DataSnapshot dataSnapshot) {
        Log.i(TAG, "ViewFriendProfile");
        if (dataSnapshot.getValue() != null) {
            String uid = dataSnapshot.getKey();
            String fullName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
            String nickName = dataSnapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
            String aboutUser = dataSnapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
            String facebookId = dataSnapshot.child(DBContract.UserTable.COL_NAME_FACEBOOK_ID).getValue(String.class);
            if (dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                Log.i(TAG, dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue().toString());
                int[] favoriteSports;
                long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                long childrenCount = 1;
            }
            //FIN
            Intent i = new Intent(getApplicationContext(), ViewProfile.class);
            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
            i.putExtra(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
            i.putExtra(DBContract.UserTable.COL_NAME_ABOUT, aboutUser);
            i.putExtra(DBContract.UserTable.COL_NAME_FACEBOOK_ID, facebookId);
            i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
            startActivity(i);
        }
    }

    private void chatActivity(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
            String uid = dataSnapshot.getKey();
            String fullName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
            Intent i = new Intent(getApplicationContext(), ChatActivity.class);
            i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
            i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
            startActivity(i);
        }

    }
    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "ChatsFragment.onFragmentInteraction");
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new PerfilFragment();
                case 1:
                    return new FriendsFragment();
                case 2:
                    return new ChatsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    public void closeApp() {
        finish();
    }
}
