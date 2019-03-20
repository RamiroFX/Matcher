package com.matcher.matcher.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.entities.EventParticipant;
import com.matcher.matcher.fragments.ViewEventDetailFragment;
import com.matcher.matcher.fragments.ViewEventMapFragment;
import com.matcher.matcher.interfaces.GroupEventDetailListener;
import com.matcher.matcher.interfaces.GroupEventMapListener;
import com.matcher.matcher.services.LocationService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewEventActivity extends AppCompatActivity implements
        ViewEventMapFragment.OnViewEventMapFragmentInteractionListener,
        ConfirmLogoutDialog.confirmLogoutDialogListener,
        LocationService.LocationServiceListener {

    private static final int LOCALIZATION_PERMISION_REQUEST = 101;
    private static final int LOCATION_CONFIG_REQUEST = 201;
    private static final int MIN_DISTANCE_TO_ARRIVE = 100;
    private static final String TAG = "ViewEventActivity";
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private DatabaseReference mDatabaseRef, mGroupEventRef, mEventParticipantsRef;
    private ChildEventListener mChildEventListener;
    private FloatingActionButton fab;
    private String eventUID, myUID, myName;
    private boolean isEventOwner;
    private EventGroup mEventGroup;
    private Location mLocation;
    private boolean deletingEvent;
    private List<EventParticipant> eventParticipantList;
    private GroupEventDetailListener groupEventDetailListener;
    private GroupEventMapListener groupEventMapListener;

    //Location vars
    private LocationService locationService;
    private boolean isEnabledLocationUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event3);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        deletingEvent = false;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        fab = findViewById(R.id.fab_view_event);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLocation();
            }
        });
        this.eventParticipantList = new ArrayList<>();
        this.mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        this.myName = sharedPreferenceHelper.getUser().getFullName();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            eventUID = bundle.getString(DBContract.EventsTable.COL_NAME_UID);
            this.mGroupEventRef = mDatabaseRef.child(DBContract.EventsTable.TABLE_NAME).child(eventUID);
            this.mEventParticipantsRef = mDatabaseRef.child(DBContract.EventsParticipantsTable.TABLE_NAME).child(eventUID);
            Log.d(TAG, "uidEvent: " + eventUID);
        } else {
            Log.d(TAG, "No uidEvent");
            return;
        }
        locationService = new LocationService(this, this);
        isEnabledLocationUpdate = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_event_activity, menu);
        //check if the user is the event owner to show only the Delete event item
        // if not show the Leave event item for invited user
        Log.d(TAG, "onCreateOptionsMenu: " + isEventOwner);
        MenuItem deleteEvent = menu.findItem(R.id.item_view_event_delete);
        MenuItem leaveEvent = menu.findItem(R.id.item_view_event_leave);
        if (isEventOwner) {
            leaveEvent.setVisible(false);
            deleteEvent.setVisible(true);
        } else {
            leaveEvent.setVisible(true);
            deleteEvent.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: " + id);
        if (id == R.id.item_view_event_delete) {
            confirmDeleteEvent();
            return true;
        }
        if (id == R.id.item_view_event_leave) {
            confirmLeaveEvent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewMapFragmentInteraction() {
        Log.d(TAG, "retrieving EventGroup Data");
        retrieveEventData();
        retrieveEventParticipants();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        handlerMenuAction();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume.isEnabledLocationUpdate: " + isEnabledLocationUpdate);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause.isEnabledLocationUpdate: " + isEnabledLocationUpdate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        stopTraveling();
        stopLocationUpdates();
        stopListeningEventParticipantsStatus();
        removeAllParticipantsMarkers();
    }

    private void retrieveEventData() {
        Log.d(TAG, "retrieveEventData");
        mGroupEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildAdded: " + dataSnapshot);
                EventGroup eventGroup = dataSnapshot.getValue(EventGroup.class);
                setEventFields(eventGroup);
                setEventMap(eventGroup);
                setMenuOptions(eventGroup);
                mEventGroup = eventGroup;
                mLocation = new Location(eventGroup.getEventName());
                mLocation.setLongitude(eventGroup.getLongitude());
                mLocation.setLatitude(eventGroup.getLatitude());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void retrieveEventParticipants() {
        Log.d(TAG, "retrieveEventParticipants");
        mEventParticipantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Log.d(TAG, "dataSnapshot: " + dataSnapshot);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    EventParticipant eventParticipant = new EventParticipant(snapshot);
                    addParticipant(eventParticipant);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void listenEventParticipantsStatus() {
        Log.d(TAG, "listenEventParticipantsStatus");
        mChildEventListener = mEventParticipantsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "listenEventParticipantsStatus.onChildAdded: " + dataSnapshot);
                EventParticipant eventParticipant = new EventParticipant(dataSnapshot);
                addParticipantMarker(eventParticipant);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "listenEventParticipantsStatus.onChildChanged: " + dataSnapshot);
                EventParticipant eventParticipant = new EventParticipant(dataSnapshot);
                if (!checkIfMarkerIsPresent(eventParticipant.getUid())) {
                    addParticipantMarker(eventParticipant);
                }
                updateParticipantMarker(eventParticipant);
                checkIfArrived(eventParticipant);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "listenEventParticipantsStatus.onChildRemoved: " + dataSnapshot);
                EventParticipant eventParticipant = new EventParticipant(dataSnapshot);
                removeParticipantMarker(eventParticipant);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMenuOptions(EventGroup eventGroup) {
        String ownerUID = eventGroup.getOwner().getUid();
        if (ownerUID.equals(myUID)) {
            isEventOwner = true;
        } else {
            isEventOwner = false;
        }
        invalidateOptionsMenu();
    }

    private void setEventFields(EventGroup eventGroup) {
        groupEventDetailListener.setEventFields(eventGroup);
    }

    private void addParticipant(EventParticipant eventParticipant) {
        eventParticipantList.add(eventParticipant);
        groupEventDetailListener.addParticipant(eventParticipant);
    }

    private void addParticipantMarker(EventParticipant eventParticipant) {
        //ADD MARKER ONLY IF STATUS IS "moving"
        if (eventParticipant.getStatus().equals(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_TRAVELING)) {
            LatLng latLng = new LatLng(eventParticipant.getLatitude(), eventParticipant.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions().title(eventParticipant.getFullName()).position(latLng);
            groupEventMapListener.addMarker(eventParticipant.getUid(), markerOptions);
        }
    }

    private void updateParticipantMarker(EventParticipant eventParticipant) {
        Log.d(TAG, "updateParticipantMarker: " + eventParticipant);
        LatLng latLng = new LatLng(eventParticipant.getLatitude(), eventParticipant.getLongitude());
        groupEventMapListener.updateMarker(eventParticipant.getUid(), latLng);
    }

    private void removeParticipantMarker(EventParticipant eventParticipant) {
        Log.d(TAG, "removeParticipantMarker: " + eventParticipant);
        groupEventMapListener.removeMarker(eventParticipant.getUid());
    }

    private void removeAllParticipantsMarkers() {
        groupEventMapListener.removeAllMarker();
    }

    private void addMyLocationToFirebase(Location location) {
        Log.d(TAG, "addMyLocationToFirebase");
        Map<String, Object> updates = new HashMap();
        EventParticipant eventParticipant = new EventParticipant();
        eventParticipant.setFullName(myName);
        eventParticipant.setStatus(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_TRAVELING);
        eventParticipant.setLatitude(location.getLatitude());
        eventParticipant.setLongitude(location.getLongitude());
        eventParticipant.setLastUpdated(Calendar.getInstance().getTimeInMillis());
        //UPDATE CURRENT USER POSITION IN EVENT PARTICIPANTS TABLE
        /*updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS, DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_TRAVELING);
        updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LATITUDE, location.getLatitude());
        updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LONGITUDE, location.getLongitude());*/
        updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID, eventParticipant);
        this.mDatabaseRef.updateChildren(updates);
        Log.d(TAG, "updating location in firebase");
    }

    private void changeParticipantStatusInList(String uid, String status) {
        Log.d(TAG, "changeParticipantStatusInList");
        groupEventDetailListener.setParticipantStatus(uid, status);
    }

    private void checkIfArrived(EventParticipant eventParticipant) {
        double currLat = eventParticipant.getLatitude();
        double currLng = eventParticipant.getLongitude();
        Location currLocation = new Location("currLocation");
        currLocation.setLatitude(currLat);
        currLocation.setLongitude(currLng);
        float distance = mLocation.distanceTo(currLocation);
        if (distance < MIN_DISTANCE_TO_ARRIVE) {
            changeParticipantStatusInList(eventParticipant.getUid(), DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_ARRIVED);
        } else {
            changeParticipantStatusInList(eventParticipant.getUid(), eventParticipant.getStatus());
        }
    }

    private void setEventMap(EventGroup eventGroup) {
        Log.d(TAG, "setEventMap");
        groupEventMapListener.updateMap(eventGroup);
    }

    private void confirmDeleteEvent() {
        String message = getString(R.string.confirm_delete_event);
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this, message);
        dialog.show(getSupportFragmentManager(), TAG);
    }

    private void deleteEvent() {
        Log.d(TAG, "deleteEvent: " + eventUID);
        /*
        MULTI PATH DELETE
         */
        Map<String, Object> updates = new HashMap();
        //DELETE CURRENT EVENT FROM ALL USERS
        for (EventParticipant participant : eventParticipantList) {
            Log.d(TAG, "participant: " + participant);
            updates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + participant.getUid() + "/" + eventUID, null);
        }
        //DELETE CURRENT EVENT FROM EVENT PARTICIPANTS TABLE
        updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID, null);
        //DELETE CURRENT EVENT FROM EVENT TABLE
        updates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + eventUID, null);
        deletingEvent = true;//para no guardar el estado del usuario cuando se llama a stopTraveling una vez cerrada la ventana
        this.mDatabaseRef.updateChildren(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                finish();
            }
        });
    }

    private void confirmLeaveEvent() {
        String message = getString(R.string.confirm_leave_event);
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this, message);
        dialog.show(getSupportFragmentManager(), TAG);
    }

    private void leaveEvent() {
        /*
        MULTI PATH DELETE
         */
        Map<String, Object> updates = new HashMap();
        //DELETE CURRENT EVENT FROM CURRENT USER
        updates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + myUID + "/" + eventUID, null);
        //DELETE CURRENT EVENT FROM EVENT PARTICIPANTS TABLE
        updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID, null);
        this.mDatabaseRef.updateChildren(updates);
        finish();
    }

    private void handlerMenuAction() {
        if (isEventOwner) {
            deleteEvent();
        } else {
            leaveEvent();
        }
    }

    private void sendLocation() {
        if (isEnabledLocationUpdate) {
            Log.d(TAG, "Disabling location updates");
            stopLocationUpdates();
            stopListeningEventParticipantsStatus();
            stopTraveling();
            isEnabledLocationUpdate = false;
            removeAllParticipantsMarkers();
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        } else {
            Log.d(TAG, "Enabling location updates");
            locationService.getCurrentLocation();
            listenEventParticipantsStatus();
            isEnabledLocationUpdate = true;
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        }
    }

    private void stopLocationUpdates() {
        locationService.stopLocationUpdates();
    }

    private void stopListeningEventParticipantsStatus() {
        if (mChildEventListener != null) {
            mEventParticipantsRef.removeEventListener(mChildEventListener);
        }
    }

    private void stopTraveling() {
        Log.d(TAG, "stopTraveling: " + deletingEvent);
        if (!deletingEvent) {
            Map<String, Object> updates = new HashMap();
            //UPDATE CURRENT USER POSITION IN EVENT PARTICIPANTS TABLE
            updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS, DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_PRESENT);
            updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LATITUDE, 0.0);
            updates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + eventUID + "/" + myUID + "/" + DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_LONGITUDE, 0.0);
            this.mDatabaseRef.updateChildren(updates);
            isEnabledLocationUpdate = false;
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            Log.d(TAG, "updating location in firebase");
        }
    }

    private boolean checkIfMarkerIsPresent(String uid) {
        return groupEventMapListener.isMakerPresent(uid);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == LOCALIZATION_PERMISION_REQUEST) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                /*//Permiso concedido

                @SuppressWarnings("MissingPermission")
                Location lastLocation =
                        LocationServices.FusedLocationApi.getLastLocation(apiClient);

                updateUI(lastLocation);*/

            } else {
                //Permiso denegado:
                //Deberíamos deshabilitar toda la funcionalidad relativa a la localización.

                Log.e(TAG, "Permiso denegado");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        switch (requestCode) {
            case LOCATION_CONFIG_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        locationService.getCurrentLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "El usuario no ha realizado los cambios de configuración necesarios");
                        //btnActualizar.setChecked(false);
                        break;
                }
                break;
        }
    }

    @Override
    public void onLocationUpdate(LocationResult locationResult) {
        Log.d(TAG, "onLocationUpdate: " + locationResult);
        for (Location location : locationResult.getLocations()) {
            Log.d(TAG, "onLocationResult: " + location);
            addMyLocationToFirebase(location);
        }
    }

    public void setGroupEventDetailListener(GroupEventDetailListener groupEventListener) {
        this.groupEventDetailListener = groupEventListener;
    }

    public void setGroupEventMapListener(GroupEventMapListener groupEventMapListener) {
        this.groupEventMapListener = groupEventMapListener;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    ViewEventDetailFragment viewEventMapFragment = new ViewEventDetailFragment();
                    setGroupEventDetailListener(viewEventMapFragment);
                    return viewEventMapFragment;
                }
                case 1: {
                    ViewEventMapFragment viewEventMapFragment = new ViewEventMapFragment();
                    setGroupEventMapListener(viewEventMapFragment);
                    return viewEventMapFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
