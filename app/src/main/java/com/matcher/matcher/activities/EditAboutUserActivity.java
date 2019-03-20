package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;

public class EditAboutUserActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditAboutUserActivity";

    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText etAboutUser;
    private Button btnSave;
    private String aboutUser, myUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_about_user);
        //Create instance of database
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        etAboutUser = findViewById(R.id.etAboutMe);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            aboutUser = extras.getString(DBContract.UserTable.COL_NAME_ABOUT, "");
            if (!aboutUser.isEmpty()) {
                etAboutUser.setText(aboutUser);
            }
        }
    }


    private void setAboutUser() {
        if (!validateForm()) {
            return;
        }
        String aboutUser = etAboutUser.getText().toString();
        mDatabaseReference.child(DBContract.UserTable.TABLE_NAME).child(myUID).child(DBContract.UserTable.COL_NAME_ABOUT).setValue(aboutUser);
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
