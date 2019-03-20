package com.matcher.matcher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.matcher.matcher.R;
import com.matcher.matcher.Utils.SharedPreferenceHelper;

public class CustomDialogCheckBox extends DialogFragment {

    private String message;
    private CustomDialogCheckBox.customDialogCheckBoxListener mListener;

    public interface customDialogCheckBoxListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }

    public CustomDialogCheckBox() {
    }

    public static CustomDialogCheckBox newInstance(CustomDialogCheckBox.customDialogCheckBoxListener customDialogCheckBoxListener, String message) {
        CustomDialogCheckBox frag = new CustomDialogCheckBox();
        frag.mListener = customDialogCheckBoxListener;
        frag.message = message;
        return frag;
    }

    public void onCreateViewas(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        /*View mView = getLayoutInflater().inflate(R.layout.fragment_dialog_checkbox, container);
        CheckBox mCheckBox = mView.findViewById(R.id.checkBox);
        mCheckBox.setText(R.string.never_show_again_message);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    storeDialogStatus(true);
                } else {
                    storeDialogStatus(false);
                }
            }
        });
        return mView;*/
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(CustomDialogCheckBox.this);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        /*View mView = getLayoutInflater().inflate(R.layout.fragment_dialog_checkbox, null);
        CheckBox mCheckBox = mView.findViewById(R.id.checkBox);
        mCheckBox.setText(R.string.never_show_again_message);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (compoundButton.isChecked()) {
                    storeDialogStatus(true);
                } else {
                    storeDialogStatus(false);
                }
            }
        });

        builder.setView(mView);*/
        return builder.create();
    }


    private void storeDialogStatus(boolean isChecked) {
        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getContext());
        sharedPreferenceHelper.setNeverShowDialogAgain(isChecked);
    }
}