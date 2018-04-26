package com.matcher.matcher.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    public interface OnTimePickerListener {
        void onTimePickerInteraction(Date time);
    }

    private OnTimePickerListener mListener;

    public static TimePickerFragment newInstance(OnTimePickerListener mListener) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setListener(mListener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        Date aDate = calendar.getTime();
        mListener.onTimePickerInteraction(aDate);
    }

    public void setListener(OnTimePickerListener listener) {
        this.mListener = listener;
    }
}