package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.dialogs.DatePickerFragment;
import com.matcher.matcher.dialogs.TimePickerFragment;
import com.matcher.matcher.entities.EventGroup;
import com.matcher.matcher.entities.EventParticipant;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.Users;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity implements View.OnClickListener,
        DatePickerFragment.OnDatePickerListener,
        TimePickerFragment.OnTimePickerListener {

    private static final int PLACE_PICKER_REQUEST = 123;
    private static final int INVITED_FRIENDS_REQUEST = 5;
    private static final String TAG = "CreateEventActivity";
    private static final String CREATE_EVENT_NAME_REQUIRED = "Oye, amigo. El nombre de evento es importante...";
    private static final String CREATE_EVENT_NAME_TOO_LONG = "Máximo 25 caracteres";
    private static final String CREATE_EVENT_DESCRIPTION_TOO_LONG = "Máximo 50 caracteres";
    private static final String CREATE_EVENT_TIME_REQUIRED = "No te olivdes de fijar una hora!";
    private static final String CREATE_EVENT_DATE_REQUIRED = "No te olivdes de fijar una fecha!";
    private static final String CREATE_EVENT_PLACE_REQUIRED = "No te olivdes de fijar un Lugar!";
    private static final String CREATE_EVENT_ON_COMPLETE_ERROR = "Ha ocurrido un error inesperado al crear el evento. Intenta nuevamente :S";
    private static final String CREATE_EVENT_ON_COMPLETE_SUCCESS = "Enhorabuena! Evento creado :)";
    private TextView tvEventScheduledTime, tvEventScheduledDate;
    private EditText etEventName, etEventDescription, etEventPlace;
    private Button btnPlaceLocation, btnInviteFriends, btnCreateEvent;

    private Place eventPlace;
    private List<EventParticipant> friendList;
    private Date eventDate, eventTime;
    private String myUID;

    private DatabaseReference mDatabaseReference;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        this.friendList = new ArrayList<>();
        this.etEventName = findViewById(R.id.et_create_event_name_content);
        this.etEventDescription = findViewById(R.id.et_create_event_description_content);
        this.tvEventScheduledTime = findViewById(R.id.tv_create_event_time_content);
        this.tvEventScheduledTime.setOnClickListener(this);
        this.tvEventScheduledDate = findViewById(R.id.tv_create_event_date_content);
        this.tvEventScheduledDate.setOnClickListener(this);
        this.btnPlaceLocation = findViewById(R.id.location_button);
        this.btnPlaceLocation.setOnClickListener(this);
        this.etEventPlace = findViewById(R.id.location_field);
        this.btnInviteFriends = findViewById(R.id.create_event_invite_friends_button);
        this.btnInviteFriends.setOnClickListener(this);
        this.btnCreateEvent = findViewById(R.id.create_event_ok_button);
        this.btnCreateEvent.setOnClickListener(this);
        this.mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public boolean onNavigateUp() {
        this.finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_create_event_date_content: {
                showDatePickerDialog();
                break;
            }
            case R.id.tv_create_event_time_content: {
                showTimePickerDialog();
                break;
            }
            case R.id.location_button: {
                showPlacePicker();
                break;
            }
            case R.id.create_event_invite_friends_button: {
                showInviteFriendsDialog();
                break;
            }
            case R.id.create_event_ok_button: {
                createEvent();
                break;
            }
        }
    }

    private void createEvent() {
        Log.d(TAG, "createEvent");
        if (isFormValid()) {
            Log.d(TAG, "Form is valid");
            String eventName = etEventName.getText().toString();
            String eventDescription = etEventDescription.getText().toString();
            String eventPlaceName = etEventPlace.getText().toString();
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(eventDate);
            Calendar calendarTime = Calendar.getInstance();
            calendarTime.setTime(eventTime);
            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
            Long eventschedule = calendarDate.getTime().getTime();
            SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
            Users anUser = sharedPreferenceHelper.getUser();
            EventGroup eventGroup = new EventGroup();
            eventGroup.setEventName(eventName);
            eventGroup.setDescription(eventDescription);
            eventGroup.setScheduledTime(eventschedule);
            eventGroup.setPlaceName(eventPlaceName);
            eventGroup.setLatitude(eventPlace.getLatLng().latitude);
            eventGroup.setLongitude(eventPlace.getLatLng().longitude);
            eventGroup.setOwner(new Friend(myUID, anUser.getFullName()));

            String newEventKey = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).push().getKey();
            Map<String, Object> childUpdates = new HashMap<>();
            //ADD current user to the eventGroup
            EventParticipant currentUser = new EventParticipant();
            currentUser.setUid(myUID);
            currentUser.setFullName(anUser.getFullName());
            currentUser.setStatus(DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_PRESENT);
            eventGroup.addParticipant(currentUser);
            //ADD eventGroup to the users
            EventGroup eventValue = new EventGroup();
            eventValue.setEventName(eventName);
            eventValue.setOwner(new Friend(myUID, anUser.getFullName()));
            eventValue.setEventType(DBContract.EventsTable.COL_NAME_GROUP_EVENT);
            eventValue.setScheduledTime(eventschedule);
            for (EventParticipant eventParticipant : friendList) {
                Log.d(TAG, "Adding friend to eventGroup : " + eventParticipant);
                childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + eventParticipant.getUid() + "/" + newEventKey, eventValue);
                eventGroup.addParticipant(eventParticipant);
            }
            childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + eventGroup.getOwner().getUid() + "/" + newEventKey, eventValue);
            Log.d(TAG, "EventGroup: " + eventGroup);
            Log.d(TAG, "Participants: " + eventGroup.getInvitedFriends());
            //EventGroup
            childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + newEventKey, eventGroup);
            //Participants
            childUpdates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + newEventKey, eventGroup.getInvitedFriends());

            Log.d(TAG, "Saving eventGroup");
            mDatabaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(getApplicationContext(), CREATE_EVENT_ON_COMPLETE_ERROR, Toast.LENGTH_SHORT).show();
                    } else {
                        logAnalyticEvent(Constants.GROUP_CREATE_EVENT);
                        Toast.makeText(getApplicationContext(), CREATE_EVENT_ON_COMPLETE_SUCCESS, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }

    private void showInviteFriendsDialog() {
        Intent i = new Intent(this, FriendsActivity.class);
        i.putExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription(), RequestCode.INVITED_FRIENDS_REQUEST.getCode());
        startActivityForResult(i, INVITED_FRIENDS_REQUEST);
    }

    private void showPlacePicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, R.string.place_picker_ex, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        DialogFragment newFragment = DatePickerFragment.newInstance(this);
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void showTimePickerDialog() {
        DialogFragment newFragment = TimePickerFragment.newInstance(this);
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onDatePickerInteraction(Date date) {
        eventDate = date;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy ");
        String reportDate = df.format(date);
        tvEventScheduledDate.setText(reportDate);
    }

    @Override
    public void onTimePickerInteraction(Date date) {
        eventTime = date;
        DateFormat df = new SimpleDateFormat("HH:mm");
        String reportDate = df.format(date);
        tvEventScheduledTime.setText(reportDate);
    }

    private void logAnalyticEvent(String name){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DBContract.EventsTable.COL_NAME_GROUP_EVENT);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private boolean isFormValid() {
        Log.d(TAG, "isFormValid");
        boolean result = true;
        //VALIDATE EVENT NAME
        if (TextUtils.isEmpty(etEventName.getText().toString())) {
            etEventName.setError(CREATE_EVENT_NAME_REQUIRED);
            Log.d(TAG, "Invalid name: " + CREATE_EVENT_NAME_REQUIRED);
            result = false;
        } else {
            if (etEventName.getText().toString().length() > 25) {
                etEventName.setError(CREATE_EVENT_NAME_TOO_LONG);
                Log.d(TAG, "Invalid name: " + CREATE_EVENT_NAME_TOO_LONG);
                result = false;
            } else {
                Log.d(TAG, "VALID name");
                etEventName.setError(null);
            }
            etEventName.setError(null);
        }
        //VALIDATE EVENT DESCRIPTION
        if (etEventDescription.getText().toString().length() > 50) {
            etEventDescription.setError(CREATE_EVENT_DESCRIPTION_TOO_LONG);
            Log.d(TAG, "Invalid description: " + CREATE_EVENT_DESCRIPTION_TOO_LONG);
            result = false;
        } else {
            Log.d(TAG, "VALID description");
            etEventDescription.setError(null);
        }
        //VALIDATE EVENT TIME
        if (eventTime == null) {
            tvEventScheduledTime.setError(CREATE_EVENT_TIME_REQUIRED);
            Log.d(TAG, "Invalid time: " + CREATE_EVENT_TIME_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "VALID time");
            tvEventScheduledTime.setError(null);
        }
        //VALIDATE EVENT DATE
        if (eventDate == null) {
            tvEventScheduledDate.setError(CREATE_EVENT_DATE_REQUIRED);
            Log.d(TAG, "Invalid date: " + CREATE_EVENT_DATE_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "VALID date");
            tvEventScheduledDate.setError(null);
        }
        if (eventDate != null && eventTime != null) {
            Calendar calTime = Calendar.getInstance();
            calTime.setTime(eventTime);
            Calendar challengeSchedule = Calendar.getInstance();
            challengeSchedule.setTime(eventDate);
            challengeSchedule.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            challengeSchedule.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
            challengeSchedule.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
            if (challengeSchedule.getTime().before(Calendar.getInstance().getTime())) {
                String date_req = getString(R.string.challenge_date_before_now_txt);
                tvEventScheduledTime.setError(date_req);
                result = false;
            } else {
                tvEventScheduledTime.setError(null);
            }
        }
        //VALIDATE EVENT PLACE
        if(eventPlace!=null){
            if (TextUtils.isEmpty(etEventPlace.getText().toString())) {
                etEventPlace.setError(CREATE_EVENT_PLACE_REQUIRED);
                Log.d(TAG, "Invalid place: " + CREATE_EVENT_PLACE_REQUIRED);
                result = false;
            } else {
                Log.d(TAG, "VALID place");
                etEventPlace.setError(null);
            }
        }else {
            Log.d(TAG, "Invalid place: null");
            etEventPlace.setError(CREATE_EVENT_PLACE_REQUIRED);
            result = false;
        }
        Log.d(TAG, "isFormValid: " + result);
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    if (place != null) {
                        Log.d(TAG, "onActivityResult: " + place);
                        eventPlace = place;
                        etEventPlace.setText(place.getName());
                    }
                }
                break;
            }
            case INVITED_FRIENDS_REQUEST: {
                Log.d(TAG, "INVITED_FRIENDS_REQUEST");
                if (resultCode == RESULT_OK) {
                    String json = data.getStringExtra(RequestCode.INVITED_FRIENDS_REQUEST.getDescription());
                    try {
                        JSONArray jsonFriendArray = new JSONArray(json);
                        Log.d(TAG, "INVITED_FRIENDS_REQUEST: " + jsonFriendArray);
                        friendList.clear();
                        for (int i = 0; i < jsonFriendArray.length(); i++) {
                            JSONObject jsonFriend = jsonFriendArray.getJSONObject(i);
                            Log.d(TAG, "INVITED_FRIENDS_REQUEST: " + jsonFriend);
                            String uid = jsonFriend.getString(jsonFriend.keys().next());
                            String name = jsonFriend.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                            Log.d(TAG, "uid: " + uid);
                            Log.d(TAG, "name: " + name);
                            friendList.add(new EventParticipant(uid, name, DBContract.EventsParticipantsTable.COL_NAME_PARTICIPANT_STATUS_PRESENT, 0.0, 0.0, 0));
                            Log.d(TAG, "Participant added");
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Could not parse malformed JSON: \"" + json + "\"");
                    }
                }
                break;
            }
        }
    }
}
