package com.matcher.matcher.dialogs;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    public interface OnDatePickerListener {
        void onDatePickerInteraction(Date date);
    }

    private OnDatePickerListener mListener;

    public static DatePickerFragment newInstance(OnDatePickerListener mListener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setListener(mListener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        Date aDate = calendar.getTime();
        mListener.onDatePickerInteraction(aDate);
    }

    public void setListener(OnDatePickerListener listener) {
        this.mListener = listener;
    }
}
