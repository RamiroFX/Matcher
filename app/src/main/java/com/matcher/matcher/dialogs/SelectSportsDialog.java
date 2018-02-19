package com.matcher.matcher.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.matcher.matcher.R;

import java.util.ArrayList;

/**
 * Created by Ramiro on 16/02/2018.
 */

public class SelectSportsDialog extends DialogFragment {

    public interface SelectSportsDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, ArrayList sports);
    }

    public SelectSportsDialog() {
    }

    public static SelectSportsDialog newInstance(SelectSportsDialogListener selectSportsDialogListener) {
        SelectSportsDialog frag = new SelectSportsDialog();
        frag.mListener = selectSportsDialogListener;
        return frag;
    }
    SelectSportsDialogListener mListener;
    private ArrayList mSelectedItems;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.sports)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(R.array.sports_array, null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedItems.add(which);
                                } else if (mSelectedItems.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedItems.remove(Integer.valueOf(which));
                                }
                            }
                        })
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(SelectSportsDialog.this, mSelectedItems);
                    }
                })
                .setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //...
                    }
                });

        return builder.create();
    }

}
