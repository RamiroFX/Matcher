package com.matcher.matcher.Utils;

import android.content.Context;
import android.widget.Toast;

public class NotificationsUtils {
    public static void showMessage(String message, Context context) {
        int lengthShort = Toast.LENGTH_SHORT;
        int lengthLong = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, message, lengthLong);

        //toast.setGravity(Gravity.TOP|Gravity.LEFT, 0, 0);
        toast.show();
    }
}
