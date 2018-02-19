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
    public interface confirmLogoutDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }


    public ConfirmLogoutDialog() {
    }

    public static ConfirmLogoutDialog newInstance(confirmLogoutDialogListener confirmLogoutDialogListener) {
        ConfirmLogoutDialog frag = new ConfirmLogoutDialog();
        frag.mListener = confirmLogoutDialogListener;
        return frag;
    }
    confirmLogoutDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.confirm_logout)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(ConfirmLogoutDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
