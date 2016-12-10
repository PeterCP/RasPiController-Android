package dev.petercp.raspicontroller.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.interfaces.OnRoomListResponseListener;
import dev.petercp.raspicontroller.interfaces.OnSingleWidgetResponseListener;
import dev.petercp.raspicontroller.utils.ConfigManager;
import dev.petercp.raspicontroller.utils.NetworkHelper;
import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.utils.WidgetDispatcher;

public class AddWidgetActivity extends AppCompatActivity {

    public static final int CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager.updateContextLocale(this);
        setContentView(R.layout.activity_add_widget);

        final Room room;
        if (getIntent().getExtras() != null && getIntent().getExtras().getParcelable("room") != null) {
            room = getIntent().getExtras().getParcelable("room");
        } else {
            room = null;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_add_widget);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, WidgetDispatcher.getWidgetTypes());
        ((Spinner) findViewById(R.id.input_type)).setAdapter(typeAdapter);

        new NetworkHelper(this).getRoomIndex(new OnRoomListResponseListener() {
            @Override
            public void onRoomListResponse(List<Room> rooms) {
                rooms.add(0, null);
                ArrayAdapter<Room> roomAdapter = new ArrayAdapter<Room>(AddWidgetActivity.this,
                        android.R.layout.simple_spinner_dropdown_item,
                        rooms)
                {
                    @NonNull
                    @Override
                    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                        if (getItem(position) == null) {
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(
                                        android.R.layout.simple_spinner_dropdown_item, parent, false);
                            }
                            ((CheckedTextView) convertView).setText("");
                            return convertView;
                        } else {
                            return super.getDropDownView(position, convertView, parent);
                        }
                    }

                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        if (getItem(position) == null) {
                            if (convertView == null) {
                                convertView = getLayoutInflater().inflate(
                                        android.R.layout.simple_spinner_dropdown_item, parent, false);
                            }
                            ((CheckedTextView) convertView).setText("");
                            return convertView;
                        } else {
                            return super.getView(position, convertView, parent);
                        }
                    }
                };
                Spinner roomsSpinner = (Spinner) findViewById(R.id.input_room);
                roomsSpinner.setAdapter(roomAdapter);
                if (room != null)
                    roomsSpinner.setSelection(rooms.indexOf(room));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSubmitButtonClicked(View view) {
        EditText nameInput = (EditText) findViewById(R.id.input_name),
                outpinInput = (EditText) findViewById(R.id.input_outpin);
        Spinner typeInput = (Spinner) findViewById(R.id.input_type);

        String name = nameInput.getText().toString(),
                type = typeInput.getSelectedItem().toString(),
                outpinString = outpinInput.getText().toString();
        int outpin;

        if (name.isEmpty()) {
            Toast.makeText(this, "Name should not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (outpinString.isEmpty()) {
            Toast.makeText(this, "Output pin should not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            outpin = Integer.valueOf(outpinInput.getText().toString());
        } catch (NumberFormatException ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        Room room = (Room) ((Spinner) findViewById(R.id.input_room)).getSelectedItem();

        NetworkHelper manager = new NetworkHelper(this);
        manager.createWidget(name, type, outpin, room, new OnSingleWidgetResponseListener() {
            @Override
            public void onSingleWidgetResponse(BaseWidget widget) {
                setResult(RESULT_OK);
                finish();
            }
        });
    }
}
