package com.matcher.matcher.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.FriendRequestAdapter;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.dialogs.DatePickerFragment;
import com.matcher.matcher.dialogs.TimePickerFragment;
import com.matcher.matcher.entities.Challenge;
import com.matcher.matcher.entities.Friend;
import com.matcher.matcher.entities.FriendRequest;
import com.matcher.matcher.entities.Sports;
import com.matcher.matcher.entities.Users;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreateDuelActivity extends AppCompatActivity implements View.OnClickListener, DatePickerFragment.OnDatePickerListener,
        TimePickerFragment.OnTimePickerListener, FriendRequestAdapter.OnFriendRequestAdapterListener, ConfirmLogoutDialog.confirmLogoutDialogListener {

    private static final int PLACE_PICKER_REQUEST = 123;
    private static final int INVITED_FRIENDS_REQUEST = 5;
    private static final String TAG = "CreateDuelActivity";
    private static final String SHOW_TO_ABANDON_FRAG = "abandonDialog";

    private Button btnChallenger, btnSport, btnCreate, btnAccept, btnCancel, btnFinish, btnAbandon;
    private EditText etChallenger, etSport, etPlace, etDescription;
    private TextView tvDate, tvTime;
    private ImageView ivDateIcon, ivTimeIcon, ivPlaceIcon;
    private Dialog myDialog;

    private DatabaseReference mDatabaseReference, mChallengeRef;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TaskCompletionSource<DataSnapshot> dbFriendSource, dbScoreSource;
    private Task dbFriendList, dbScore;
    private Task<Void> allTask;

    private int activityMode;
    private Friend friend;
    private Sports sports;
    private Date challengeDate, challengeTime;
    private Place challengePlace;
    private Challenge challenge;
    private String myUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_duel);
        Toolbar toolbar = findViewById(R.id.toolbar_challenge);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.mDatabaseReference = mDatabase.getReference();
        this.mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        this.btnCreate = findViewById(R.id.btn_create_challenge);
        this.btnCreate.setOnClickListener(this);
        this.etChallenger = findViewById(R.id.et_challenger);
        this.etSport = findViewById(R.id.et_challenge);
        this.etPlace = findViewById(R.id.et_challenge_place_content);
        this.etPlace.setOnClickListener(this);
        this.ivPlaceIcon = findViewById(R.id.iv_challenge_place);
        this.ivPlaceIcon.setOnClickListener(this);
        this.etDescription = findViewById(R.id.et_challenge_description_content);
        this.tvDate = findViewById(R.id.tv_challenge_date);
        this.tvTime = findViewById(R.id.tv_challenge_time);
        this.friend = new Friend();
        this.sports = new Sports();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.get(Constants.CHALLENGE_ACTIVITY_TYPE) != null) {
            this.challenge = new Challenge();
            activityMode = extras.getInt(Constants.CHALLENGE_ACTIVITY_TYPE, -1);
            Log.d(TAG, "type: " + activityMode);
            switch (activityMode) {
                case Constants.CHALLENGE_ACTIVITY_TYPE_CREATE: {
                    getSupportActionBar().setTitle(R.string.title_activity_view_duel);
                    this.btnChallenger = findViewById(R.id.btn_challenger);
                    this.btnChallenger.setOnClickListener(this);
                    this.btnSport = findViewById(R.id.btn_sport);
                    this.btnSport.setOnClickListener(this);
                    this.tvDate.setOnClickListener(this);
                    this.tvTime.setOnClickListener(this);
                    this.ivDateIcon = findViewById(R.id.iv_challenge_date);
                    this.ivDateIcon.setOnClickListener(this);
                    this.ivTimeIcon = findViewById(R.id.iv_challenge_time);
                    this.ivTimeIcon.setOnClickListener(this);

                    break;
                }
                case Constants.CHALLENGE_ACTIVITY_TYPE_ACCEPT: {
                    getSupportActionBar().setTitle(R.string.title_activity_view_duel);
                    String challengeUid = extras.getString(DBContract.ChallengeTable.COL_NAME_CHALLENGE_UID);
                    this.challenge.setUid(challengeUid);
                    this.mChallengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challengeUid);
                    this.challenge.getChallenger().setUid(extras.getString(DBContract.ChallengeTable.COL_NAME_CHALLENGER_UID));
                    this.challenge.getChallenger().setFullName(extras.getString(DBContract.ChallengeTable.COL_NAME_CHALLENGER));
                    this.challenge.getSport().setName(extras.getString(DBContract.ChallengeTable.COL_NAME_SPORT));
                    this.challenge.setScheduledTime(extras.getLong(DBContract.ChallengeTable.COL_NAME_SCHEDULED_TIME));
                    this.btnAccept = findViewById(R.id.btn_accept_challenge);
                    this.btnAccept.setVisibility(View.VISIBLE);
                    this.btnAccept.setOnClickListener(this);
                    this.btnCancel = findViewById(R.id.btn_cancel_challenge);
                    this.btnCancel.setVisibility(View.VISIBLE);
                    this.btnCancel.setOnClickListener(this);
                    this.btnCreate.setVisibility(View.GONE);
                    this.etDescription.setEnabled(false);
                    this.etPlace.setFocusable(false);
                    this.etPlace.setClickable(true);
                    retrieveChallengeData();
                    break;
                }
                case Constants.CHALLENGE_ACTIVITY_TYPE_VIEW: {
                    getSupportActionBar().setTitle(R.string.title_activity_view_duel);
                    this.myDialog = new Dialog(CreateDuelActivity.this);
                    String challengeUid = extras.getString(DBContract.ChallengeTable.COL_NAME_CHALLENGE_UID);
                    this.challenge.setUid(challengeUid);
                    this.mChallengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challengeUid);
                    this.btnFinish = findViewById(R.id.btn_finish_challenge);
                    this.btnFinish.setVisibility(View.VISIBLE);
                    this.btnFinish.setOnClickListener(this);
                    this.btnAbandon = findViewById(R.id.btn_abandon_challenge);
                    this.btnAbandon.setVisibility(View.VISIBLE);
                    this.btnAbandon.setOnClickListener(this);
                    this.btnCreate.setVisibility(View.GONE);
                    this.etDescription.setEnabled(false);
                    this.etPlace.setFocusable(false);
                    this.etPlace.setClickable(true);
                    retrieveChallengeData();
                    break;
                }
                default: {
                    finish();
                    break;
                }
            }
        }
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
        int id = view.getId();
        switch (id) {
            case R.id.btn_challenger: {
                showFriendList();
                break;
            }
            case R.id.btn_sport: {
                showSportsList();
                break;
            }
            case R.id.btn_create_challenge: {
                createChallenge();
                break;
            }
            case R.id.tv_challenge_date: {
                showDatePickerDialog();
                break;
            }
            case R.id.tv_challenge_time: {
                showTimePickerDialog();
                break;
            }
            case R.id.iv_challenge_date: {
                showDatePickerDialog();
                break;
            }
            case R.id.iv_challenge_time: {
                showTimePickerDialog();
                break;
            }
            case R.id.iv_challenge_place: {
                showPlacePicker();
                break;
            }
            case R.id.et_challenge_place_content: {
                showDuelPlace();
                break;
            }
            case R.id.btn_accept_challenge: {
                acceptChallenge();
                break;
            }
            case R.id.btn_cancel_challenge: {
                refuseChallenge();
                break;
            }
            case R.id.btn_finish_challenge: {
                showSelectWinnerDialog();
                break;
            }
            case R.id.btn_abandon_challenge: {
                showAbandonDialog();
                break;
            }
        }
    }

    @Override
    public void onDatePickerInteraction(Date date) {
        challengeDate = date;
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy ");
        String reportDate = df.format(date);
        tvDate.setText(reportDate);
    }

    @Override
    public void onTimePickerInteraction(Date date) {
        challengeTime = date;
        DateFormat df = new SimpleDateFormat("HH:mm");
        String reportDate = df.format(date);
        tvTime.setText(reportDate);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        abandonChallenge();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //ignore
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    if (place != null) {
                        challengePlace = place;
                        etPlace.setText(place.getName());
                    }
                }
                break;
            }
            case INVITED_FRIENDS_REQUEST: {
                if (resultCode == RESULT_OK) {
                    String uid = data.getStringExtra(DBContract.UserTable.COL_NAME_UID);
                    String name = data.getStringExtra(DBContract.UserTable.COL_NAME_FULLNAME);
                    this.friend.setUid(uid);
                    this.friend.setFullName(name);
                    etChallenger.setText(name);
                }
                break;
            }
            case Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE: {
                if (resultCode == RESULT_OK) {
                    int uid = data.getIntExtra(Constants.SPORT_ID, -1);
                    String name = data.getStringExtra(Constants.SPORT_NAME);
                    this.sports.setUid(uid);
                    this.sports.setName(name);
                    etSport.setText(name);
                }
                break;
            }
        }
    }

    private void retrieveChallengeData() {
        mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "retrieveChallengeData.onDataChange: " + dataSnapshot);
                Challenge value = dataSnapshot.getValue(Challenge.class);
                challenge = value;
                challenge.setUid(dataSnapshot.getKey());
                if (myUID.equals(challenge.getChallenger().getUid())) {
                    etChallenger.setText(value.getChallenged().getFullName());
                } else {
                    etChallenger.setText(value.getChallenger().getFullName());
                }
                etSport.setText(value.getSport().getName());
                etPlace.setText(value.getPlaceName());
                etDescription.setText(value.getDescription());
                tvDate.setText(value.challengeDate());
                tvTime.setText(value.challengeTime());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showFriendList() {
        Intent i = new Intent(this, FriendsActivity.class);
        i.putExtra(RequestCode.CHAT_FRIENDS_REQUEST.getDescription(), RequestCode.CHAT_FRIENDS_REQUEST.getCode());
        startActivityForResult(i, INVITED_FRIENDS_REQUEST);
    }

    private void showSportsList() {
        Intent i = new Intent(this, SportsActivity.class);
        i.putExtra(Constants.SPORTS_ACTIVITY_TYPE, Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE);
        startActivityForResult(i, Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE);
    }

    private void showPlacePicker() {
        if (activityMode == Constants.CHALLENGE_ACTIVITY_TYPE_ACCEPT || activityMode == Constants.CHALLENGE_ACTIVITY_TYPE_VIEW) {
            if (challenge != null) {
                Intent i = new Intent(getApplicationContext(), ViewChallengeMapActivity.class);
                i.putExtra(DBContract.EventsTable.COL_NAME_LATITUDE, challenge.getLatitude());
                i.putExtra(DBContract.EventsTable.COL_NAME_LONGITUDE, challenge.getLongitude());
                i.putExtra(DBContract.EventsTable.COL_NAME_PLACE_NAME, challenge.getPlaceName());
                startActivity(i);
            }
        } else {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                Toast.makeText(this, R.string.place_picker_ex, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDuelPlace() {
        if (activityMode == Constants.CHALLENGE_ACTIVITY_TYPE_ACCEPT || activityMode == Constants.CHALLENGE_ACTIVITY_TYPE_VIEW) {
            if (challenge != null) {
                Intent i = new Intent(getApplicationContext(), ViewChallengeMapActivity.class);
                i.putExtra(DBContract.EventsTable.COL_NAME_LATITUDE, challenge.getLatitude());
                i.putExtra(DBContract.EventsTable.COL_NAME_LONGITUDE, challenge.getLongitude());
                i.putExtra(DBContract.EventsTable.COL_NAME_PLACE_NAME, challenge.getPlaceName());
                startActivity(i);
            }
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

    private boolean isFormValid() {
        Log.d(TAG, "isFormValid");
        boolean result = true;
        //VALIDATE CHALLENGER NAME
        if (etChallenger.getText() == null || TextUtils.isEmpty(etChallenger.getText()) || friend.getUid().isEmpty()) {
            String challenger_req = getString(R.string.challenge_friend_empty_txt);
            etChallenger.setError(challenger_req);
            Log.d(TAG, "isFormValid: " + challenger_req);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etChallenger.setError(null);");
            etChallenger.setError(null);
        }
        //VALIDATE SPORT
        if (etSport.getText() == null || TextUtils.isEmpty(etSport.getText())) {
            String sport_req = getString(R.string.challenge_sport_empty_txt);
            etSport.setError(sport_req);
            Log.d(TAG, "isFormValid: " + sport_req);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etSport.setError(null): ");
            etSport.setError(null);
        }
        //VALIDATE TIME
        if (challengeTime == null) {
            String time_req = getString(R.string.challenge_time_empty_txt);
            tvTime.setError(time_req);
            Log.d(TAG, "isFormValid: " + time_req);
            result = false;
        } else {
            Log.d(TAG, "Valid time");
            tvTime.setError(null);
        }
        //VALIDATE DATE
        if (challengeDate == null) {
            String date_req = getString(R.string.challenge_date_empty_txt);
            tvDate.setError(date_req);
            Log.d(TAG, "isFormValid: " + date_req);
            result = false;
        } else {
            Log.d(TAG, "Valid day");
            tvDate.setError(null);
        }
        if (challengeDate != null && challengeTime != null) {
            Calendar calTime = Calendar.getInstance();
            calTime.setTime(challengeTime);
            Calendar challengeSchedule = Calendar.getInstance();
            challengeSchedule.setTime(challengeDate);
            challengeSchedule.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
            challengeSchedule.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
            challengeSchedule.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
            if (challengeSchedule.getTime().before(Calendar.getInstance().getTime())) {
                String date_req = getString(R.string.challenge_date_before_now_txt);
                tvDate.setError(date_req);
                result = false;
            } else {
                tvDate.setError(null);
            }
        }
        //VALIDATE PLACE
        if (TextUtils.isEmpty(etPlace.getText().toString()) && challengePlace == null) {
            String place_req = getString(R.string.challenge_place_empty_txt);
            etPlace.setError(place_req);
            Log.d(TAG, "isFormValid: " + place_req);
            result = false;
        } else {
            Log.d(TAG, "isFormValid: etEventPlace.setError(null):");
            etPlace.setError(null);
        }
        Log.d(TAG, "isFormValid: " + result);
        return result;
    }

    private void createChallenge() {
        if (isFormValid()) {
            SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
            Users anUser = sharedPreferenceHelper.getUser();
            Friend challenger = new Friend(myUID, anUser.getFullName());
            Friend challenged = new Friend(friend.getUid(), friend.getFullName());
            String challengeDescription = etDescription.getText().toString();
            String placeName = etPlace.getText().toString();
            //set challenge schedule
            Calendar calendarDate = Calendar.getInstance();
            calendarDate.setTime(challengeDate);
            Calendar calendarTime = Calendar.getInstance();
            calendarTime.setTime(challengeTime);
            calendarDate.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
            calendarDate.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
            calendarDate.set(Calendar.SECOND, calendarTime.get(Calendar.SECOND));
            Long challengeSchedule = calendarDate.getTime().getTime();
            //set the challenge
            Challenge challenge = new Challenge();
            challenge.setChallenged(challenged);
            challenge.setChallenger(challenger);
            challenge.setSport(sports);
            challenge.setScheduledTime(challengeSchedule);
            challenge.setDescription(challengeDescription);
            challenge.setPlaceName(placeName);
            challenge.setStatus(DBContract.ChallengeTable.COL_NAME_CHALLENGE_STATUS_PENDING);
            challenge.setLatitude(challengePlace.getLatLng().latitude);
            challenge.setLongitude(challengePlace.getLatLng().longitude);
            /*
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGER_UID + "\": \"" + getChallenger().getUid() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGER + "\": \"" + getChallenger().getFullName() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGED_UID + "\": \"" + getChallenged().getUid() + "\"" +
                ", \"" + DBContract.ChallengeTable.COL_NAME_CHALLENGED + "\": \"" + getChallenged().getFullName() + "\"" +
             */
            Challenge challengeValue = new Challenge();
            challengeValue.setEventType(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT);
            challengeValue.setScheduledTime(challengeSchedule);
            challengeValue.setSport(sports);
            challengeValue.setChallenged(challenged);
            challengeValue.setChallenger(challenger);

            String newChallengeKey = mDatabaseReference.child(DBContract.ChallengeTable.TABLE_NAME).push().getKey();
            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put("/" + DBContract.ChallengeRequestsTable.TABLE_NAME + "/" + challenged.getUid() + "/" + newChallengeKey, challengeValue);

            //ADD challenge to users
            childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenger.getUid() + "/" + newChallengeKey, challengeValue);
            Log.d(TAG, "Challenge: " + challenge);
            //Challenge table
            childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + newChallengeKey, challenge);

            Log.d(TAG, "Saving challenge");
            mDatabaseReference.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        String onError = getString(R.string.create_challenge_on_complete_error);
                        Toast.makeText(getApplicationContext(), onError, Toast.LENGTH_SHORT).show();
                    } else {
                        logAnalyticEvent(Constants.CHALLENGE_CREATE_EVENT);
                        String onSuccess = getString(R.string.create_challenge_on_complete_success);
                        Toast.makeText(getApplicationContext(), onSuccess, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        }
    }

    private void acceptChallenge() {
        Challenge challengeValue = new Challenge();
        challengeValue.setEventType(DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT);
        challengeValue.setScheduledTime(challenge.getScheduledTime());
        challengeValue.setSport(challenge.getSport());
        challengeValue.setChallenged(challenge.getChallenged());
        challengeValue.setChallenger(challenge.getChallenger());
        Map<String, Object> childUpdates = new HashMap<>();
        //ADD CHALLENGE TO USER
        childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + myUID + "/" + challenge.getUid(), challengeValue);
        //CHANGE CHALLENGE STATUS TO ACCEPTED
        childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + challenge.getUid() + "/" + DBContract.ChallengeTable.COL_NAME_CHALLENGE_STATUS, DBContract.ChallengeTable.COL_NAME_CHALLENGE_STATUS_ACEPTED);
        //REMOVE CHALLENGE REQUEST
        childUpdates.put("/" + DBContract.ChallengeRequestsTable.TABLE_NAME + "/" + myUID + "/" + challenge.getUid(), null);
        mDatabaseReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                logAnalyticEvent(Constants.CHALLENGE_ACCEPT_EVENT);
                String message = getString(R.string.create_challenge_on_accept_success);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void refuseChallenge() {
        Map<String, Object> childUpdates = new HashMap<>();
        //REMOVE CHALLENGE FROM CHALLENGER
        childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenge.getChallenger().getUid() + "/" + challenge.getUid(), null);
        //CHANGE CHALLENGE STATUS TO ACCEPTED
        childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + challenge.getUid(), null);
        //REMOVE CHALLENGE REQUEST
        childUpdates.put("/" + DBContract.ChallengeRequestsTable.TABLE_NAME + "/" + myUID + "/" + challenge.getUid(), null);
        mDatabaseReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                String message = getString(R.string.create_challenge_on_refuse_success);
                logAnalyticEvent(Constants.CHALLENGE_DECLINE_EVENT);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void SelectWinner(final Friend winnerFriend) {
        this.btnFinish.setEnabled(false);
        this.btnAbandon.setEnabled(false);
       /* DatabaseReference challengeRef;
        if (myUID.equals(challenge.getChallenger().getUid())) {
            challengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid()).child(DBContract.ChallengeTable.COL_NAME_CHALLENGER_WINNER);
        } else {
            challengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid()).child(DBContract.ChallengeTable.COL_NAME_CHALLENGED_WINNER);
        }
        challengeRef.setValue(winnerFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                checkWinner();
            }
        });*/
        final TaskCompletionSource dbChallengeSource = new TaskCompletionSource<>();
        final Task dbChallenge = dbChallengeSource.getTask();
        mChallengeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbChallengeSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbChallengeSource.setException(databaseError.toException());
            }
        });
        Task<Void> allTask = Tasks.whenAll(dbChallenge);
        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DataSnapshot dataScore = (DataSnapshot) dbChallenge.getResult();
                if (dataScore.exists()) {
                    DatabaseReference challengeRef;
                    if (myUID.equals(challenge.getChallenger().getUid())) {
                        challengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid()).child(DBContract.ChallengeTable.COL_NAME_CHALLENGER_WINNER);
                    } else {
                        challengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid()).child(DBContract.ChallengeTable.COL_NAME_CHALLENGED_WINNER);
                    }
                    challengeRef.setValue(winnerFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            checkWinner();
                        }
                    });
                } else {
                    String message = getString(R.string.challenge_finished_message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = getString(R.string.challenge_finished_error_message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkWinner() {
        DatabaseReference challengeRef = mDatabaseReference.child(DBContract.EventsTable.TABLE_NAME).child(challenge.getUid());
        challengeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "dataSnapshot: " + dataSnapshot);
                if (dataSnapshot.hasChild(DBContract.ChallengeTable.COL_NAME_CHALLENGED_WINNER) &&
                        dataSnapshot.hasChild(DBContract.ChallengeTable.COL_NAME_CHALLENGER_WINNER)) {
                    Friend challengedWinner = dataSnapshot.child(DBContract.ChallengeTable.COL_NAME_CHALLENGED_WINNER).getValue(Friend.class);
                    Friend challengerWinner = dataSnapshot.child(DBContract.ChallengeTable.COL_NAME_CHALLENGER_WINNER).getValue(Friend.class);
                    if (challengedWinner.getUid().equals(challengerWinner.getUid())) {
                        setScore(challengedWinner.getUid(), Constants.CHALLENGE_INCREASE_SCORE, Constants.CHALLENGE_STANDARD_INCREASE_SCORE);
                    } else {
                        String message = getString(R.string.select_winner_rule);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String message = getString(R.string.select_winner_incomplete_voting);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showSelectWinnerDialog() {
        Date challengeDate = new Date();
        challengeDate.setTime(challenge.getScheduledTime());
        Date today = Calendar.getInstance().getTime();
        if (today.before(challengeDate)) {
            String message = getString(R.string.challenge_finish_date_before_now_txt);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        } else {
            myDialog.setContentView(R.layout.fragment_dialog_select_winner);
            RecyclerView rvChallengeParticipants = myDialog.findViewById(R.id.rvChallengeParticipants);
            FriendRequestAdapter selectableFriendAdapter = new FriendRequestAdapter(this);
            rvChallengeParticipants.setAdapter(selectableFriendAdapter);
            rvChallengeParticipants.setLayoutManager(new LinearLayoutManager(this));
            selectableFriendAdapter.onRequestAdded(new FriendRequest(challenge.getChallenged().getUid(), challenge.getChallenged().getFullName()));
            selectableFriendAdapter.onRequestAdded(new FriendRequest(challenge.getChallenger().getUid(), challenge.getChallenger().getFullName()));
            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            myDialog.show();
        }
    }

    private void showAbandonDialog() {
        String message = "";
        if (challenge.getStatus().equals(DBContract.ChallengeTable.COL_NAME_CHALLENGE_STATUS_PENDING)) {
            message = getString(R.string.select_winner_abandon_message_pending);
        } else {
            message = getString(R.string.select_winner_abandon_message);
        }
        DialogFragment newFragment = ConfirmLogoutDialog.newInstance(this, message);
        newFragment.show(getSupportFragmentManager(), SHOW_TO_ABANDON_FRAG);
    }

    private void abandonChallenge() {
        Map<String, Object> childUpdates = new HashMap<>();
        //REMOVE CHALLENGE FROM PLAYERS
        childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenge.getChallenger().getUid() + "/" + challenge.getUid(), null);
        childUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenge.getChallenged().getUid() + "/" + challenge.getUid(), null);
        //REMOVE CHALLENGE
        childUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + challenge.getUid(), null);
        if (challenge.getStatus().equals(DBContract.ChallengeTable.COL_NAME_CHALLENGE_STATUS_PENDING)) {
            //REMOVE CHALLENGE REQUEST
            childUpdates.put("/" + DBContract.ChallengeRequestsTable.TABLE_NAME + "/" + challenge.getChallenged().getUid() + "/" + challenge.getUid(), null);
            mDatabaseReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    logAnalyticEvent(Constants.CHALLENGE_ABANDON_EVENT);
                    String message = getString(R.string.challenge_finished_message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            mDatabaseReference.updateChildren(childUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    setScore(myUID, Constants.CHALLENGE_DECREASE_SCORE, Constants.CHALLENGE_STANDARD_DECREASE_SCORE);
                }
            });
        }
    }

    private void setScore(final String winnerUID, final int operation, final int score) {
        final TaskCompletionSource dbChallengeSource = new TaskCompletionSource<>();
        this.dbFriendSource = new TaskCompletionSource<>();
        this.dbScoreSource = new TaskCompletionSource<>();
        this.dbScore = dbScoreSource.getTask();
        this.dbFriendList = dbFriendSource.getTask();
        final Task dbChallenge = dbChallengeSource.getTask();
        DatabaseReference mFriendshipRef = mDatabaseReference.child(DBContract.FriendshipTable.TABLE_NAME).child(winnerUID);
        mFriendshipRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbFriendSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbFriendSource.setException(databaseError.toException());
            }
        });

        ///
        DatabaseReference scoreRef = mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(winnerUID).child(DBContract.UserTable.COL_NAME_SCORE);
        scoreRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbScoreSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbScoreSource.setException(databaseError.toException());
            }
        });
        ///
        mChallengeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbChallengeSource.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dbChallengeSource.setException(databaseError.toException());
            }
        });
        ///
        allTask = Tasks.whenAll(dbChallenge, dbFriendList, dbScore);
        allTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //CHECK IF CHALLENGE IS STILL ACTIVE IN DB
                DataSnapshot dataChallenge = (DataSnapshot) dbChallenge.getResult();
                if (!dataChallenge.exists()) {
                    String message = getString(R.string.challenge_finished_message);
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    finish();
                }
                //ACTUAL SCORE
                DataSnapshot dataScore = (DataSnapshot) dbScore.getResult();
                int actualScore = dataScore.getValue(Integer.class);
                //FRIEND LIST
                Map<String, Object> scoreUpdates = new HashMap<>();
                //REMOVE CHALLENGE FROM USERS
                scoreUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenge.getChallenger().getUid() + "/" + challenge.getUid(), null);
                scoreUpdates.put("/" + DBContract.UserEventsTable.TABLE_NAME + "/" + challenge.getChallenged().getUid() + "/" + challenge.getUid(), null);
                //REMOVE CHALLENGE
                scoreUpdates.put("/" + DBContract.EventsTable.TABLE_NAME + "/" + challenge.getUid(), null);
                switch (operation) {
                    case Constants.CHALLENGE_INCREASE_SCORE: {
                        scoreUpdates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + winnerUID + "/" + DBContract.UserTable.COL_NAME_SCORE, actualScore + score);
                        DataSnapshot data = (DataSnapshot) dbFriendList.getResult();
                        for (DataSnapshot snapshot : data.getChildren()) {
                            scoreUpdates.put("/" + DBContract.FriendshipTable.TABLE_NAME + "/" + snapshot.getKey() + "/" + winnerUID + "/" + DBContract.FriendshipTable.COL_NAME_SCORE, actualScore + score);
                        }
                        break;
                    }
                    case Constants.CHALLENGE_DECREASE_SCORE: {
                        scoreUpdates.put("/" + DBContract.UserTable.TABLE_NAME + "/" + winnerUID + "/" + DBContract.UserTable.COL_NAME_SCORE, actualScore - score);
                        DataSnapshot data = (DataSnapshot) dbFriendList.getResult();
                        for (DataSnapshot snapshot : data.getChildren()) {
                            scoreUpdates.put("/" + DBContract.FriendshipTable.TABLE_NAME + "/" + snapshot.getKey() + "/" + winnerUID + "/" + DBContract.FriendshipTable.COL_NAME_SCORE, actualScore - score);
                        }
                        break;
                    }
                }
                mDatabaseReference.updateChildren(scoreUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        btnFinish.setEnabled(true);
                        btnAbandon.setEnabled(true);
                        logAnalyticEvent(Constants.CHALLENGE_FINISH_EVENT);
                        String message = getString(R.string.challenge_finished_message);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
            }
        });
        allTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = getString(R.string.challenge_finished_error_message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void checkDuelExistence() {
        //Query q = mDatabaseReference.
    }

    private void logAnalyticEvent(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, DBContract.ChallengeTable.COL_NAME_CHALLENGE_EVENT);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onImageViewInteraction(FriendRequest item) {
        SelectWinner(new Friend(item.getUid(), item.getFullName()));
        myDialog.dismiss();
    }

    @Override
    public void onTextViewInteraction(FriendRequest item) {
        SelectWinner(new Friend(item.getUid(), item.getFullName()));
        myDialog.dismiss();
    }
}
