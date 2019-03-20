package com.matcher.matcher.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        sharedPreferenceHelper.setNotificationToken(refreshedToken);
        String userUid = sharedPreferenceHelper.getUser().getUid();
        if (userUid.isEmpty()) {
            try {
                userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (NullPointerException e) {
                return;
            }
        }
        FirebaseDatabase.getInstance().getReference().child(DBContract.UserTable.TABLE_NAME).child(userUid).child(DBContract.UserTable.COL_NAME_NOTIFICATION_TOKEN).setValue(refreshedToken);
    }
}
