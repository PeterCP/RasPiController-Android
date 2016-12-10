package dev.petercp.raspicontroller.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.interfaces.OnSingleRoomResponseListener;
import dev.petercp.raspicontroller.utils.NetworkHelper;

public class AddRoomDialogFragment extends DialogFragment {

    public interface OnRoomCreatedListener {
        void onRoomCreated(Room room);
    }

    private OnRoomCreatedListener listener;
    private View view;

    public AddRoomDialogFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_widget, null);
        builder.setTitle(R.string.label_add_room);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onSaveButtonPressed();
            }
        });
        builder.setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getDialog().cancel();
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRoomCreatedListener) {
            listener = (OnRoomCreatedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRoomCreatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void onSaveButtonPressed() {
        String name = ((EditText) view.findViewById(R.id.input_name)).getText().toString();
        final OnRoomCreatedListener localListener = listener;
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name should not be empty.", Toast.LENGTH_SHORT).show();
        } else {
            new NetworkHelper(getContext()).createRoom(name, new OnSingleRoomResponseListener() {
                @Override
                public void onSingleRoomResponse(Room room) {
                    dismiss();
                    localListener.onRoomCreated(room);
                }
            });
        }
    }
}
