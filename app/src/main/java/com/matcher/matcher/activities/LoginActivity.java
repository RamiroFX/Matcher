package com.matcher.matcher.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.entities.Users;
import com.matcher.matcher.services.FacebookFriendsAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    //Facebook callbackmanager
    private CallbackManager callbackManager;

    // UI references.
    private LoginButton loginButton;
    private Dialog errorDialog;
    private SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        //Firebase init
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        //Initialize callback manager
        callbackManager = CallbackManager.Factory.create();

        //SharedPreferences
        sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());

        //Assign the views to the corresponding variables
        loginButton = (LoginButton) findViewById(R.id.login_button);

        //Assign the button permissions
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile", "user_birthday", "user_gender", "user_friends"));

        //Create instance of database
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Assign the button a task
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                getUserInfo(loginResult);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            CharSequence text = currentUser.getDisplayName();
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    protected void getUserInfo(final LoginResult login_result) {
        loginButton.setEnabled(false);
        GraphRequest data_request = GraphRequest.newMeRequest(
                login_result.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        try {
                            String facebook_id = object.getString("id");
                            String facebook_name = object.getString("name");
                            AccessToken token = login_result.getAccessToken();
                            saveFacebookCredentialsInFirebase(token, facebook_name, facebook_id);
                        } catch (JSONException ignored) {
                        }
                    }
                });
        Bundle permission_param = new Bundle();
        permission_param.putString("fields", "id,name,email");
        data_request.setParameters(permission_param);
        data_request.executeAsync();
    }

    private void saveFacebookCredentialsInFirebase(AccessToken token, final String fName, final String fID) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    final FirebaseUser user = mAuth.getCurrentUser();
                    final String uid = user.getUid();
                    mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(uid).
                            addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean isNewUser = true;
                                    String fullName = dataSnapshot.child(DBContract.UserTable.COL_NAME_FULLNAME).getValue(String.class);
                                    if (fullName != null && !fullName.isEmpty()) isNewUser = false;
                                    String email = dataSnapshot.child(DBContract.UserTable.COL_NAME_EMAIL).getValue(String.class);
                                    if (email != null && !email.isEmpty()) isNewUser = false;
                                    if (isNewUser) {
                                        // Add the user to users table.
                                        fullName = fName;
                                        String nickName = fName;
                                        email = user.getEmail();
                                        Long creationDate = user.getMetadata().getCreationTimestamp();
                                        final Users aNewUser = new Users(fullName, email, 0, 0.0, 0.0);
                                        aNewUser.setNickName(nickName);
                                        aNewUser.setFacebookId(fID);
                                        aNewUser.setCreationDate(creationDate);
                                        mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(uid).setValue(aNewUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "New user SAVING sharedPreferenceHelper");
                                                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_UID, uid);
                                                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_FULLNAME, fName);
                                                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_NICKNAME, fName);
                                                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_EMAIL, user.getEmail());
                                                sharedPreferenceHelper.setParameterInteger(DBContract.UserTable.COL_NAME_SCORE, 0);
                                                callFacebookFriendTask();
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Old user SAVING sharedPreferenceHelper");
                                        String nickName = dataSnapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                                        Integer score = dataSnapshot.child(DBContract.UserTable.COL_NAME_SCORE).getValue(Integer.class);
                                        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_UID, uid);
                                        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_FULLNAME, fName);
                                        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
                                        sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_EMAIL, user.getEmail());
                                        sharedPreferenceHelper.setParameterInteger(DBContract.UserTable.COL_NAME_SCORE, score);
                                    }
                                    String notifToken = FirebaseInstanceId.getInstance().getToken();
                                    mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(uid).child(DBContract.UserTable.COL_NAME_NOTIFICATION_TOKEN).setValue(notifToken);
                                    sharedPreferenceHelper.setNotificationToken(notifToken);
                                    updateUI(user);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
                loginButton.setEnabled(true);
            }
        });
    }

    private void callFacebookFriendTask() {
        FacebookFriendsAsyncTask task = new FacebookFriendsAsyncTask(getApplicationContext());
        task.execute();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }
                if (!errorDialog.isShowing()) {
                    errorDialog.show();
                }
            }
        }
        return resultCode == ConnectionResult.SUCCESS;
    }
}

