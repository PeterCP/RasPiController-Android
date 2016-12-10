package dev.petercp.raspicontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.classes.DateAxisValueFormatter;
import dev.petercp.raspicontroller.classes.Room;
import dev.petercp.raspicontroller.classes.UsageStatistics;
import dev.petercp.raspicontroller.fragments.DatePickerDialogFragment;
import dev.petercp.raspicontroller.interfaces.OnUsageStatisticsResponseListener;
import dev.petercp.raspicontroller.utils.ConfigManager;
import dev.petercp.raspicontroller.utils.DateTimeUtils;
import dev.petercp.raspicontroller.utils.NetworkHelper;

public class ChartActivity extends AppCompatActivity implements
        DatePickerDialogFragment.OnDatePickedListener,
        OnUsageStatisticsResponseListener {

    private static final int[] COLORS = {
            Color.rgb(0xF4, 0x43, 0x36), // Red
            Color.rgb(0x67, 0x3A, 0xB7), // Deep Blue
            Color.rgb(0xFF, 0xEB, 0x3B), // Yellow
            Color.rgb(0x21, 0x96, 0xF3), // Blue
            Color.rgb(0xFF, 0x57, 0x22), // Deep Orange
            Color.rgb(0x4C, 0xAF, 0x50), // Green
    };

    public static void startNew(Context context, Room room) {
        Intent intent = new Intent(context, ChartActivity.class);
        intent.putExtra("room", room);
        context.startActivity(intent);
    }

    public static void startNew(Context context, BaseWidget widget) {
        Intent intent = new Intent(context, ChartActivity.class);
        intent.putExtra("widget", widget);
        context.startActivity(intent);
    }

    private LineChart chart;
    private DatePickerDialogFragment fromPicker, toPicker;
    private TextView fromDisplay, toDisplay;

    private NetworkHelper networkHelper;
    private BaseWidget widget;
    private Room room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager.updateContextLocale(this);
        setContentView(R.layout.activity_chart);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_activity_chart);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getParcelable("widget") != null) {
                widget = extras.getParcelable("widget");
            }
            if (extras.getParcelable("room") != null) {
                room = extras.getParcelable("room");
            }

            if (room != null && widget != null) {
                throw new RuntimeException("Both room and widget arguments given, only one is expected.");
            }
            if (room == null && widget == null) {
                throw new RuntimeException("Expected a room or a widget argument, none given.");
            }
        }

        networkHelper = new NetworkHelper(this);

        fromDisplay = (TextView) findViewById(R.id.from_display);
        toDisplay = (TextView) findViewById(R.id.to_display);
        fromPicker = DatePickerDialogFragment.newInstance("from");
        toPicker = DatePickerDialogFragment.newInstance("to");

        chart = (LineChart) findViewById(R.id.chart);
        chart.getDescription().setText(getString(R.string.label_watts));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Only intervals of 1 day
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(new DateAxisValueFormatter(chart));

        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_refresh:
                updateData();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDatePicked(String tag, @Nullable DateTime date) {
        String text = date != null ? DateTimeUtils.toString(date) : "";
        if (tag.equals("from")) {
            fromDisplay.setText(text);
        } else if (tag.equals("to")) {
            toDisplay.setText(text);
        }
    }

    @Override
    public void onUsageStatisticsResponse(List<UsageStatistics> statistics) {
        setChartData(statistics);
    }

    public void onFromButtonClicked(View view) {
        fromPicker.show(getSupportFragmentManager(), null);
    }

    public void onToButtonClicked(View view) {
        toPicker.show(getSupportFragmentManager(), null);
    }

    private void updateData() {
        DateTime from = fromPicker.getDate(),
                to = toPicker.getDate();
        if (widget != null) {
            networkHelper.getWidgetUsage(widget, from, to, this);
        } else {
            networkHelper.getRoomUsage(room, from, to, this);
        }
    }

    private void setChartData(List<UsageStatistics> statistics) {
        UsageStatistics first = statistics.get(0);
        DateTime from = first.getFrom(),
                to = first.getTo();

        List<ILineDataSet> dataSets = new ArrayList<>();

        UsageStatistics usage;
        LineDataSet dataSet;
        int color;
        List<Entry> entries;
        for (int i = 0; i < statistics.size(); i++) {
            usage = statistics.get(i);
            entries = new ArrayList<>();
            for (UsageStatistics.UsageEntry point : usage.getUsages())
                entries.add(new BarEntry(DateTimeUtils.toDays(point.getDate()), point.getUsage()));
            dataSet = new LineDataSet(entries, usage.getWidget().getName());
            dataSet.setDrawCircleHole(false);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(5f);

            color = COLORS[i % COLORS.length];
            dataSet.setColor(color);
            dataSet.setCircleColor(color);
            dataSet.setFillColor(color);

            dataSets.add(dataSet);
        }
        LineData data = new LineData(dataSets);

        chart.setData(data);
        chart.invalidate();
        if (from != null)
            chart.getXAxis().setAxisMinimum(DateTimeUtils.toDays(from.minusDays(1)));
        if (to != null)
            chart.getXAxis().setAxisMaximum(DateTimeUtils.toDays(to.plusDays(1)));

        fromPicker.setDate(from);
        onDatePicked("from", from);
        toPicker.setDate(to);
        onDatePicked("to", to);
    }
}
