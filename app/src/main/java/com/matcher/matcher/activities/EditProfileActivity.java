package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.Utils.SharedPreferenceHelper;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EditProfileActivity";

    private String myUID;
    private DatabaseReference mDatabaseReference;
    private EditText etNickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //Create instance of database
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        etNickName = findViewById(R.id.etAlias);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        btnSave.setOnClickListener(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String nickName = extras.getString(DBContract.UserTable.COL_NAME_NICKNAME, "");
            if (nickName != null && !nickName.isEmpty()) {
                etNickName.setText(nickName);
            }
        }
    }


    private void setAlias() {
        if (!validateForm()) {
            return;
        }
        String nickName = etNickName.getText().toString();
        //ruta a la propiedad Alias del usuario: users/uid/nickName
        mDatabaseReference.child(DBContract.UserTable.TABLE_NAME)
                .child(myUID)
                .child(DBContract.UserTable.COL_NAME_NICKNAME)
                .setValue(nickName);
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
