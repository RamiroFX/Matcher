package com.matcher.matcher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.matcher.matcher.R;

/**
 * Created by Ramiro on 15/02/2018.
 */

public class ConfirmLogoutDialog extends DialogFragment {

    private String message;
    public interface confirmLogoutDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }


    public ConfirmLogoutDialog() {
    }

    public static ConfirmLogoutDialog newInstance(confirmLogoutDialogListener confirmLogoutDialogListener, String message) {
        ConfirmLogoutDialog frag = new ConfirmLogoutDialog();
        frag.mListener = confirmLogoutDialogListener;
        frag.message = message;
        return frag;
    }
    confirmLogoutDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(ConfirmLogoutDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(ConfirmLogoutDialog.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
