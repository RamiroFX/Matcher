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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.dialogs.DatePickerFragment;
import com.matcher.matcher.dialogs.TimePickerFragment;
import com.matcher.matcher.entities.Event;
import com.matcher.matcher.entities.Friend;

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
    private List<Friend> friendList;

    private String eventName, eventDescription;
    private Date eventDate, eventTime;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
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
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
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
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String eventName = etEventName.getText().toString();
            String eventDescription = etEventDescription.getText().toString();
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(eventDate);
            Calendar calendarTime = Calendar.getInstance();
            calendarTime.setTime(eventTime);
            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
            Long eventschedule = calendarDate.getTime().getTime();
            Event event = new Event();
            event.setEventName(eventName);
            event.setDescription(eventDescription);
            event.setScheduledTime(eventschedule);
            event.setPlaceName(eventPlace.getName().toString());
            event.setLatitude(eventPlace.getLatLng().latitude);
            event.setLongitude(eventPlace.getLatLng().longitude);
            event.setOwnerUid(uid);


            String newEventKey = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).push().getKey();
            Map<String, Object> childUpdates = new HashMap<>();
            for (Friend aFriend : friendList) {
                Log.d(TAG, "Adding friend to event : " + aFriend);
                event.addFriends(aFriend);
                childUpdates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + aFriend.getUid()+"/"+DBContract.UserTable.COL_NAME_EVENTS+"/"+newEventKey, event.toJsonString());
            }
            childUpdates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + event.getOwnerUid()+"/"+DBContract.UserTable.COL_NAME_EVENTS+"/"+newEventKey, event.toJsonString());
            Log.d(TAG, "Event: " + event);
            Log.d(TAG, "Participants: " + event.getInvitedFriends());
            //Event
            childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + newEventKey, event);
            //Participants
            childUpdates.put("/" + DBContract.EventsParticipantsTable.TABLE_NAME + "/" + newEventKey, event.getInvitedFriends());

            Log.d(TAG, "Saving event");
            mDatabaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(getApplicationContext(), CREATE_EVENT_ON_COMPLETE_ERROR, Toast.LENGTH_SHORT).show();
                    } else {
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

    private boolean isFormValid() {
        Log.d(TAG, "isFormValid");
        boolean result = true;
        //VALIDATE EVENT NAME
        if (TextUtils.isEmpty(etEventName.getText().toString())) {
            etEventName.setError(CREATE_EVENT_NAME_REQUIRED);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_NAME_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etEventName.setError(null);");
            etEventName.setError(null);
        }
        if (etEventName.getText().toString().length() > 25) {
            etEventName.setError(CREATE_EVENT_NAME_TOO_LONG);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_NAME_TOO_LONG);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etEventName.setError(null)");
            etEventName.setError(null);
        }
        //VALIDATE EVENT DESCRIPTION
        if (etEventDescription.getText().toString().length() > 50) {
            etEventDescription.setError(CREATE_EVENT_DESCRIPTION_TOO_LONG);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_DESCRIPTION_TOO_LONG);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etEventDescription.setError(null): " + CREATE_EVENT_DESCRIPTION_TOO_LONG);
            etEventDescription.setError(null);
        }
        //VALIDATE EVENT TIME
        if (TextUtils.isEmpty(tvEventScheduledTime.getText().toString())) {
            tvEventScheduledTime.setError(CREATE_EVENT_TIME_REQUIRED);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_TIME_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: tvEventScheduledTime.setError(null)" + CREATE_EVENT_TIME_REQUIRED);
            tvEventScheduledTime.setError(null);
        }
        //VALIDATE EVENT DATE
        if (TextUtils.isEmpty(tvEventScheduledDate.getText().toString())) {
            tvEventScheduledDate.setError(CREATE_EVENT_DATE_REQUIRED);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_DATE_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: tvEventScheduledDate.setError(null):" + CREATE_EVENT_DATE_REQUIRED);
            tvEventScheduledDate.setError(null);
        }
        //VALIDATE EVENT PLACE
        if (TextUtils.isEmpty(etEventPlace.getText().toString())) {
            etEventPlace.setError(CREATE_EVENT_PLACE_REQUIRED);
            Log.d(TAG, "isFormValid: " + CREATE_EVENT_PLACE_REQUIRED);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etEventPlace.setError(null):" + CREATE_EVENT_PLACE_REQUIRED);
            etEventPlace.setError(null);
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
                        Log.d(TAG, "INVITED_FRIENDS_REQUEST: "+jsonFriendArray);
                        friendList.clear();
                        for (int i = 0; i < jsonFriendArray.length(); i++) {
                            JSONObject jsonFriend = jsonFriendArray.getJSONObject(i);
                            Log.d(TAG, "INVITED_FRIENDS_REQUEST: "+jsonFriend);
                            String uid = jsonFriend.getString(jsonFriend.keys().next());
                            String name = jsonFriend.getString(DBContract.UserTable.COL_NAME_FULLNAME);
                            Log.d(TAG, "uid: "+uid);
                            Log.d(TAG, "name: "+name);
                            friendList.add(new Friend(uid, name));
                            Log.d(TAG, "Added friend");
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
