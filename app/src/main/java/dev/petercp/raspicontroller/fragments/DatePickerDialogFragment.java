package dev.petercp.raspicontroller.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import org.joda.time.DateTime;

public class DatePickerDialogFragment extends DialogFragment implements
        DialogInterface.OnCancelListener,
        DatePickerDialog.OnDateSetListener {

    public interface OnDatePickedListener {
        void onDatePicked(String tag, @Nullable DateTime date);
    }

    public static DatePickerDialogFragment newInstance(String tag) {
        return newInstance(tag, null);
    }

    public static DatePickerDialogFragment newInstance(String tag,
                                                       @Nullable DateTime date) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        Bundle args = new Bundle();
        args.putString("tag", tag);
        args.putSerializable("date", date);
        fragment.setArguments(args);
        return fragment;
    }

    private DateTime date;
    private String tag;
    private OnDatePickedListener listener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDatePickedListener) {
            listener = (OnDatePickedListener) context;
        } else {
            throw new RuntimeException(context.toString() +
                    " must implement OnDatePickedListener");
        }

        if (getArguments() != null) {
            Bundle args = getArguments();
            tag = args.getString("tag");
            if (args.getSerializable("date") != null)
                setDate((DateTime) args.getSerializable("date"));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        DateTime localDate = date;
        if (localDate == null)
            localDate = DateTime.now();
        Log.i(this.toString(), localDate.toString());
        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(),
                this,
                localDate.year().get(),
                localDate.getMonthOfYear(),
                localDate.getDayOfMonth()
        );
        dialog.setOnCancelListener(this);
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        setDate(new DateTime(year, month + 1, day, 0, 0));
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        setDate(null);
    }

    public void setDate(@Nullable DateTime date) {
        this.date = date;
        if (listener != null)
            listener.onDatePicked(tag, date);
    }

    @Nullable
    public DateTime getDate() {
        return date;
    }
}
