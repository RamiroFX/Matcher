package com.matcher.matcher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.matcher.matcher.R;

/**
 * Created by Ramiro on 16/02/2018.
 */

public class AddFriendDialog extends DialogFragment {

    private addFriendDialogListener mListener;

    public interface addFriendDialogListener {
        void onFriendDialogPositiveClick(String email);
    }

    public AddFriendDialog() {
    }

    public static AddFriendDialog newInstance(addFriendDialogListener confirmLogoutDialogListener) {
        AddFriendDialog frag = new AddFriendDialog();
        frag.mListener = confirmLogoutDialogListener;
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final EditText etEmail = new EditText(this.getContext());
        etEmail.setText("mr.ramiro@hotmail.com");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        etEmail.setHint(R.string.hint_add_friend_email);
        String title = getResources().getString(R.string.add_friend_title);
        String message = getResources().getString(R.string.add_friend_message);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(etEmail);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(R.string.text_add_friend_acept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                verifyEmail(etEmail.getText());
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

    public boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void verifyEmail(Editable text) {
        if (isValidEmail(text)) {
            mListener.onFriendDialogPositiveClick(text.toString());
        } else {
            Toast.makeText(getContext(), R.string.text_add_friend_invalid_email, Toast.LENGTH_SHORT).show();
        }
    }
}