package com.matcher.matcher.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EditProfileActivity";

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText etNickName;
    private Button btnSave;
    private String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //Create instance of database
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        etNickName = findViewById(R.id.etAlias);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            nickName = extras.getString(DBContract.UserTable.COL_NAME_NICKNAME);
            assert nickName != null;
            if (!nickName.isEmpty()) {
                etNickName.setText(nickName);
            }
        }
    }


    private void setAlias() {
        // FirebaseUser.getToken() instead.
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
        String nickName = etNickName.getText().toString();
        mDatabaseReference.child(Constants.USER_TABLE_NAME).child(uid).child("nickName").setValue(nickName);
        Intent intent = new Intent();
        intent.putExtra(RequestCode.RESULT.getDescription(), nickName);
        setResult(RESULT_OK, intent);
        finish();
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(etNickName.getText().toString())) {
            etNickName.setError("Required");
            result = false;
        } else {
            etNickName.setError(null);
        }
        if (etNickName.getText().toString().length() > 25) {
            etNickName.setError("Too long");
            result = false;
        }
        return result;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSaveProfile: {
                setAlias();
                break;
            }
        }
    }
}
