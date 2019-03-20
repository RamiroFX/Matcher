package com.matcher.matcher.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matcher.matcher.BuildConfig;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.activities.ChatActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.adapters.CommunityAdapter;
import com.matcher.matcher.dialogs.AddFriendDialog;
import com.matcher.matcher.dialogs.CustomDialogCheckBox;
import com.matcher.matcher.entities.CommunityFriend;
import com.matcher.matcher.interfaces.LogAnalyticEventListener;
import com.matcher.matcher.services.FetchAddressIntentService;
import com.matcher.matcher.services.LocationService;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class CommunityFragment extends Fragment implements CustomDialogCheckBox.customDialogCheckBoxListener,
        CommunityAdapter.OnCommunityAdapterListener, AddFriendDialog.addFriendDialogListener,
        MainActivity.OnLocationUpdateListener, MainActivity.OnRequestedPermissionListener {

    private static final String TAG = "CommunityFragment";
    private static final String SHOW_TO_COMMUNITY_FRAG = "showToComunityDialog";
    private static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    private static final String LOCATION_ADDRESS_KEY = "location-address";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final int ACTION_TYPE_ADD_FROM_EMAIL = 11;
    private static final int ACTION_TYPE_VIEW_PROFILE = 12;
    private static final int USER_TYPE_FRIEND = 21;
    private static final int USER_TYPE_NO_FRIEND = 22;
    private static final int USER_TYPE_YOURSELF = 23;
    private LocationService locationService;
    /**
     * Represents a geographical location.
     */
    private Location mLastLocation;
    private boolean mAddressRequested;
    private String mAddressOutput;
    private AddressResultReceiver mResultReceiver;
    private Dialog myDialog;
    private LogAnalyticEventListener mListener;


    private RecyclerView communityList;
    private CommunityAdapter communityAdapter;
    private DatabaseReference mDatabaseRef, communityRef;
    private SharedPreferenceHelper sharedPreferenceHelper;
    private ChildEventListener communityListener;
    private String myUID;

    //ImageView ivLoading;
    AnimationDrawable loadingAnim;

    public static CommunityFragment newInstance() {
        return new CommunityFragment();
    }

    public CommunityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        this.locationService = new LocationService(getActivity(), (LocationService.LocationServiceListener) getActivity());
        this.mResultReceiver = new AddressResultReceiver(new Handler());
        this.sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        this.myDialog = new Dialog(getContext());
        FloatingActionButton fab = view.findViewById(R.id.fab_community);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showToCommunityDialog();
            }
        });
        this.communityList = view.findViewById(R.id.rvCommunityList);
        /*ivLoading = view.findViewById(R.id.ivLoading);
        ivLoading.setVisibility(View.INVISIBLE);
        loadingAnim = (AnimationDrawable) ivLoading.getDrawable();*/
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.communityAdapter = new CommunityAdapter(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_community_tab, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_track:
                hideMeToTheCommunity();
                return true;
            case R.id.menu_item_add_friend:
                showAddFriendDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        dismissCommunity();
        retrieveCommunity();
        if (sharedPreferenceHelper.isUserVisibleToCommunity()
                && !sharedPreferenceHelper.getLastCountryCode().isEmpty()
                && !sharedPreferenceHelper.getLastLatitude().equals("0.0")
                && !sharedPreferenceHelper.getLastLongitude().equals("0.0")) {
            String countryCode = sharedPreferenceHelper.getLastCountryCode();
            double lastLat = Double.valueOf(sharedPreferenceHelper.getLastLatitude());
            double lastLong = Double.valueOf(sharedPreferenceHelper.getLastLongitude());
            String message = getString(R.string.getting_last_location_message);
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            showLoading();
            showMeToTheCommunity(countryCode, lastLat, lastLong);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissCommunity();
        silenceCommunity();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof LogAnalyticEventListener) {
            mListener = (LogAnalyticEventListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement LogAnalyticEventListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onLocationUpdated(LocationResult locationResult) {
        locationService.stopLocationUpdates();
        String message_success = getString(R.string.got_location_message);
        Toast.makeText(getContext(), message_success, Toast.LENGTH_SHORT).show();
        if (!locationResult.getLocations().isEmpty()) {
            for (Location location : locationResult.getLocations()) {
                mLastLocation = location;
            }
        } else {
            if (locationResult.getLastLocation() != null) {
                mLastLocation = locationResult.getLastLocation();
            }
        }
        if (mLastLocation != null) {
            sharedPreferenceHelper.setLastLongitude(mLastLocation.getLongitude() + "");
            sharedPreferenceHelper.setLastLatitude(mLastLocation.getLatitude() + "");

            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                showSnackbar(getString(R.string.no_geocoder_available));
                return;
            }

            // If the user pressed the fetch address button before we had the location,
            // this  will be set to true indicating that we should kick off the intent
            // service after fetching the location.
            if (mAddressRequested) {
                startIntentService();
            }
        } else {
            String message_failed = getString(R.string.could_not_get_location_message);
            Toast.makeText(getContext(), message_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void getUserLocation() {
        locationService.getCurrentLocation();
    }

    private void retrieveCommunity() {
        communityList.setAdapter(communityAdapter);
        communityList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void dismissCommunity() {
        communityAdapter.clearList();
    }

    public void listenCommunity() {
        communityListener = communityRef.addChildEventListener(
                new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        CommunityFriend communityFriend = new CommunityFriend(dataSnapshot);
                        communityAdapter.onChildAdded(communityFriend);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        CommunityFriend communityFriend = new CommunityFriend(dataSnapshot);
                        communityAdapter.onChildChanged(communityFriend);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        CommunityFriend communityFriend = new CommunityFriend(dataSnapshot);
                        communityAdapter.onChildRemoved(communityFriend);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    public void silenceCommunity() {
        if (communityListener != null) {
            communityRef.removeEventListener(communityListener);
        }
    }

    private void showToCommunityDialog() {
        DialogFragment newFragment = CustomDialogCheckBox.newInstance(this, getString(R.string.SHOW_TO_COMMUNITY_MESSAGE));
        newFragment.show(getChildFragmentManager(), SHOW_TO_COMMUNITY_FRAG);
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.COMMUNITY_SHOW_ME_EVENT, DBContract.CommunityTable.TABLE_NAME);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        String message = getString(R.string.looking_for_location_message);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        showLoading();
        getMyLocation();
        // If we have not yet retrieved the user location, we process the user's request by setting
        // mAddressRequested to true. As far as the user is concerned, pressing the Fetch Address button
        // immediately kicks off the process of getting the address.
        mAddressRequested = true;
    }

    private void getMyLocation() {
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getUserLocation();
        }
    }

    private void showMeToTheCommunity(String countryCode, double latitude, double longitude) {
        String alias = sharedPreferenceHelper.getUser().getNickName();
        communityAdapter.setUserLatitude(latitude);
        communityAdapter.setUserLongitude(longitude);
        CommunityFriend communityFriend = new CommunityFriend(myUID, alias, latitude, longitude);
        communityFriend.setUid(null);
        communityFriend.setDistanceFromUser(null);
        if (this.communityRef != null) {
            this.communityRef.child(myUID).setValue(communityFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPreferenceHelper.setUserVisibilityToCommunity(true);
                    retrieveCommunity();
                    listenCommunity();
                    hideLoading();
                }
            });
        } else {
            this.communityRef = mDatabaseRef.child(DBContract.CommunityTable.TABLE_NAME).child(countryCode);
            this.communityRef.child(myUID).setValue(communityFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    sharedPreferenceHelper.setUserVisibilityToCommunity(true);
                    retrieveCommunity();
                    listenCommunity();
                    hideLoading();
                }
            });
        }
    }

    private void hideMeToTheCommunity() {
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.COMMUNITY_HIDE_ME_EVENT, DBContract.CommunityTable.TABLE_NAME);
        }
        dismissCommunity();
        silenceCommunity();
        if (mAddressOutput != null && !mAddressOutput.isEmpty()) {
            sharedPreferenceHelper.setUserVisibilityToCommunity(false);
            if (this.communityRef != null) {
                this.communityRef.child(myUID).setValue(null);
            } else {
                this.communityRef = mDatabaseRef.child(DBContract.CommunityTable.TABLE_NAME).child(mAddressOutput);
                this.communityRef.child(myUID).setValue(null);
            }
        } else if (!sharedPreferenceHelper.getLastCountryCode().isEmpty()) {
            sharedPreferenceHelper.setUserVisibilityToCommunity(false);
            if (this.communityRef != null) {
                this.communityRef.child(myUID).setValue(null);
            } else {
                this.communityRef = mDatabaseRef.child(DBContract.CommunityTable.TABLE_NAME).child(sharedPreferenceHelper.getLastCountryCode());
                this.communityRef.child(myUID).setValue(null);
            }
        }
        hideLoading();
    }

    private void showAddFriendDialog() {
        if (mListener != null) {
            mListener.logAnalyticEvent(Constants.COMMUNITY_ADD_FRIEND_EVENT, DBContract.CommunityTable.TABLE_NAME);
        }
        AddFriendDialog addFriendDialog = AddFriendDialog.newInstance(this);
        addFriendDialog.show(getChildFragmentManager(), "showAddFriendDialog");
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(getContext(), FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        getActivity().startService(intent);
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     * <p>
     * private void updateUIWidgets() {
     * if (mAddressRequested) {
     * //mProgressBar.setVisibility(ProgressBar.VISIBLE);
     * //mFetchAddressButton.setEnabled(false);
     * } else {
     * //mProgressBar.setVisibility(ProgressBar.GONE);
     * //mFetchAddressButton.setEnabled(true);
     * }
     * }
     */

    @Override
    public void onCommunityAdapterInteraction(View view, CommunityFriend communityFriend) {
        checkFriendship(communityFriend, ACTION_TYPE_VIEW_PROFILE);
    }

    private void showCommunityDialogProfile(final CommunityFriend communityFriend, final int friendType) {
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(DBContract.ProfileStorage.TABLE_NAME);
        DatabaseReference userRef = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).child(communityFriend.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                TextView txtclose, tvName, tvDistance, tvSportContent, tvAboutUserContent;
                final ImageView ivProfile;
                Button btnAddFriend;
                String favoriteSports = "", userAbout = "";
                userAbout = dataSnapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                if (dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                    long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                    long childrenCount = 1;
                    for (DataSnapshot childSnapshot : dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildren()) {
                        Long sportIndex = (Long) childSnapshot.getValue();
                        List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                        for (int i = 0; i <= myArrayList.size(); i++) {
                            if (sportIndex == i) {
                                if (childrenCount < childrenSport) {
                                    favoriteSports = favoriteSports + myArrayList.get(i) + " - ";
                                    childrenCount++;
                                } else {
                                    favoriteSports = favoriteSports + myArrayList.get(i);
                                    childrenCount++;
                                }
                                break;
                            }
                        }
                    }
                }
                myDialog.setContentView(R.layout.fragment_dialog_social_profile);
                ivProfile = myDialog.findViewById(R.id.iv_social_prof_image);
                storageRef.child(communityFriend.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).rotate(90).resize(500, 500).into(ivProfile);
                    }
                });
                txtclose = myDialog.findViewById(R.id.txtclose);
                txtclose.setText("M");
                tvName = myDialog.findViewById(R.id.tv_social_prof_name);
                tvName.setText(communityFriend.getFullName());
                tvDistance = myDialog.findViewById(R.id.tv_social_prof_distance);
                String to = getString(R.string.text_to);
                String km = getString(R.string.text_KM);
                tvDistance.setText(String.format("%s %s %s", to, communityFriend.getDistanceFromUser(), km));
                tvSportContent = myDialog.findViewById(R.id.tvSportsContent);
                tvSportContent.setText(favoriteSports);
                tvAboutUserContent = myDialog.findViewById(R.id.tvAboutContent);
                tvAboutUserContent.setText(userAbout);
                btnAddFriend = myDialog.findViewById(R.id.btnfollow);
                switch (friendType) {
                    case USER_TYPE_FRIEND: {
                        btnAddFriend.setText(R.string.send_message);
                        break;
                    }
                    case USER_TYPE_YOURSELF: {
                        btnAddFriend.setText(R.string.accept_button);
                        break;
                    }
                }
                btnAddFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (friendType) {
                            case USER_TYPE_FRIEND: {
                                startChat(communityFriend);
                                break;
                            }
                            case USER_TYPE_NO_FRIEND: {
                                sendFriendRequest(dataSnapshot);
                                break;
                            }
                            case USER_TYPE_YOURSELF: {
                                myDialog.dismiss();
                                break;
                            }
                        }
                    }
                });
                txtclose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void startChat(CommunityFriend communityFriend) {
        String uid = communityFriend.getUid();
        String fullName = communityFriend.getFullName();
        Intent i = new Intent(getContext(), ChatActivity.class);
        i.putExtra(DBContract.UserTable.COL_NAME_FULLNAME, fullName);
        i.putExtra(DBContract.UserTable.COL_NAME_UID, uid);
        i.putExtra(DBContract.GroupTable.COL_NAME_NAME, false);
        startActivity(i);
    }

    private void sendFriendRequest(DataSnapshot communityUser) {
        String userName = sharedPreferenceHelper.getUser().getFullName();
        DatabaseReference friendReqRef = mDatabaseRef.child(DBContract.FriendRequestTable.TABLE_NAME);
        friendReqRef.child(communityUser.getKey()).child(myUID).setValue(userName).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String message = getString(R.string.friend_request_message_sended);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onFriendDialogPositiveClick(String email) {
        //checkEmailAdress(email);
        addNewFriend(email);
    }

    private void addNewFriend(final String email) {
        Log.e(TAG, "addNewFriend: " + email);
        showLoading();
        final TaskCompletionSource dbUserSource = new TaskCompletionSource<>();
        final TaskCompletionSource dbFriendshipSource = new TaskCompletionSource<>();
        final TaskCompletionSource dbFriendSource = new TaskCompletionSource<>();
        final Task dbUser = dbUserSource.getTask();
        final Task dbFriendship = dbFriendshipSource.getTask();
        final Task dbFriend = dbFriendSource.getTask();
        Task<Void> allTask;
        //GET USER DATA
        DatabaseReference mUserRef = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).child(myUID);
        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbUserSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbUserSource.setException(databaseError.toException());
            }
        });
        allTask = Tasks.whenAll(dbUser, dbFriendship, dbFriend);
        //GET FRIEND DATA
        Query mFriendRef = mDatabaseRef.child(DBContract.UserTable.TABLE_NAME).orderByChild(DBContract.UserTable.COL_NAME_EMAIL).equalTo(email).limitToFirst(1);
        mFriendRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbFriendSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbFriendSource.setException(databaseError.toException());
            }
        });
        //GET USER'S FRIENDSHIP DATA
        DatabaseReference mFriendshipRef = mDatabaseRef.child(DBContract.FriendshipTable.TABLE_NAME).child(myUID);
        mFriendshipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbFriendshipSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbFriendshipSource.setException(databaseError.toException());
            }
        });
        //PROCESS ALL TE DATA
        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String friendUID = "";
                String myEmail;
                String myName;
                //CHECK USER
                //CHECK THE ENTERED EMAIL IS NOT FROM THE SAME USER
                DataSnapshot dataUser = (DataSnapshot) dbUser.getResult();
                if (!dataUser.exists()) {
                    String message = getString(R.string.challenge_data_user_not_found_message);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    hideLoading();
                    return;
                } else {
                    myEmail = dataUser.child(DBContract.UserTable.COL_NAME_EMAIL).getValue() + "";
                    if (myEmail.equals(email)) {
                        String message = getString(R.string.user_same_email_message);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        hideLoading();
                        return;
                    }
                    myName = dataUser.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue() + "";
                }
                //CHECK FRIEND
                //THE RESULT IS LIMITED TO 1 SNAPSHOT
                DataSnapshot dataFriend = (DataSnapshot) dbFriend.getResult();
                if (dataFriend.exists()) {
                    for (DataSnapshot snapshot : dataFriend.getChildren()) {
                        friendUID = snapshot.getKey();
                    }
                } else {
                    String message = getString(R.string.text_add_friend_email_not_found);
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    hideLoading();
                    return;
                }

                //CHECK FRIENDSHIP
                //CHECK THAT THE REQUESTED FRIEND ISNT A FRIEND YET
                DataSnapshot dataFriendship = (DataSnapshot) dbFriendship.getResult();
                if (dataFriendship.exists()) {
                    for (DataSnapshot snapshot : dataFriendship.getChildren()) {
                        if (snapshot.getKey().equals(friendUID)) {
                            String message = getString(R.string.text_add_friend_already_friends);
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            hideLoading();
                            return;
                        }
                    }
                }
                mDatabaseRef.child(DBContract.FriendRequestTable.TABLE_NAME).child(friendUID).child(myUID).setValue(myName).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideLoading();
                        String message = getString(R.string.friend_request_message_sended);
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        allTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                hideLoading();
                String message = getString(R.string.challenge_finished_error_message);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkFriendship(final CommunityFriend communityFriend, final int actionType) {
        final String friendUID = communityFriend.getUid();
        DatabaseReference friendshipRef = mDatabaseRef.child(DBContract.FriendshipTable.TABLE_NAME).child(myUID);
        Query q = friendshipRef.orderByKey().equalTo(friendUID);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                switch (actionType) {
                    case ACTION_TYPE_ADD_FROM_EMAIL: {
                        if (dataSnapshot.getValue() != null) {
                            Toast.makeText(getContext(), getResources().getText(R.string.text_add_friend_already_friends), Toast.LENGTH_SHORT).show();
                        } else {
                            addFriend(friendUID);
                        }
                        break;
                    }
                    case ACTION_TYPE_VIEW_PROFILE: {
                        if (dataSnapshot.getValue() != null) {
                            showCommunityDialogProfile(communityFriend, USER_TYPE_FRIEND);
                        } else {
                            if (communityFriend.getUid().equals(myUID)) {
                                showCommunityDialogProfile(communityFriend, USER_TYPE_YOURSELF);
                            } else {
                                showCommunityDialogProfile(communityFriend, USER_TYPE_NO_FRIEND);
                            }
                        }
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void addFriend(String friendUID) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference friendShipRef = databaseReference.child(DBContract.FriendshipTable.TABLE_NAME).child(myUID);
        DatabaseReference userRef = databaseReference.child(DBContract.UserTable.TABLE_NAME).child(friendUID);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String friendUID = dataSnapshot.getKey();
                    String fullName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                    friendShipRef.child(friendUID).setValue(fullName, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                String friendNotAdded = getString(R.string.text_add_friend_not_added_friend);
                                Toast.makeText(getContext(), friendNotAdded, Toast.LENGTH_LONG).show();
                            } else {
                                String friendAdded = getString(R.string.text_add_friend_added_friend);
                                Toast.makeText(getContext(), friendAdded, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = getView().findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(getView().findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private void showLoading() {
        /*ivLoading.setVisibility(View.VISIBLE);
        loadingAnim.start();*/
    }

    private void hideLoading() {
        /*ivLoading.setVisibility(View.INVISIBLE);
        loadingAnim.start();*/
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            //Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });

        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void myOnRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getUserLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private class AddressResultReceiver extends ResultReceiver {
        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == Constants.FAILURE_RESULT) {
                String message = getString(R.string.location_service_not_available_message);
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                hideLoading();
            } else if (resultCode == Constants.SUCCESS_RESULT) {
                mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
                // Display the address string or an error message sent from the intent service.
                if (mAddressOutput != null && !mAddressOutput.isEmpty()) {
                    sharedPreferenceHelper.setLastCountryCode(mAddressOutput);
                    double lastLat = Double.valueOf(sharedPreferenceHelper.getLastLatitude());
                    double lastLong = Double.valueOf(sharedPreferenceHelper.getLastLongitude());
                    silenceCommunity();
                    dismissCommunity();
                    showMeToTheCommunity(mAddressOutput, lastLat, lastLong);
                    // Reset. Enable the Fetch Address button and stop showing the progress bar.
                    mAddressRequested = false;
                }
            }
        }
    }
}
