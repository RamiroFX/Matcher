package com.matcher.matcher.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.NotificationsUtils;
import com.matcher.matcher.Utils.RequestCode;

public class EditAboutUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditAboutUserActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText etAboutUser;
    private Button btnSave;
    private String aboutUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_about_user);
        //Create instance of database
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        etAboutUser = findViewById(R.id.etAboutMe);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            aboutUser = extras.getString(DBContract.UserTable.COL_NAME_ABOUT,"");
            if (!aboutUser.isEmpty()) {
                etAboutUser.setText(aboutUser);
            }
        }
    }


    private void setAboutUser() {
        if (!validateForm()) {
            return;
        }
        String uid;
        try {
            uid = mAuth.getCurrentUser().getUid();
        } catch (NullPointerException e) {
            String message = getResources().getString(R.string.general_error);
            NotificationsUtils.showMessage(message, this.getBaseContext());
            return;
        }
        String aboutUser = etAboutUser.getText().toString();
        mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(uid).child(DBContract.UserTable.COL_NAME_ABOUT).setValue(aboutUser);
        Intent intent = new Intent();
        intent.putExtra(RequestCode.RESULT.getDescription(), aboutUser);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean validateForm() {
        boolean result = true;
        if (etAboutUser.getText().toString().length() > 150) {
            etAboutUser.setError("Too long");
            result = false;
        }
        return result;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSaveProfile: {
                setAboutUser();
                break;
            }
        }
    }
}
