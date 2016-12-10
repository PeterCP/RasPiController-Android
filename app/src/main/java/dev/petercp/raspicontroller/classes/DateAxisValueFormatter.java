package dev.petercp.raspicontroller.classes;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import org.joda.time.DateTime;

import dev.petercp.raspicontroller.utils.DateTimeUtils;

public class DateAxisValueFormatter implements IAxisValueFormatter {


    private BarLineChartBase<?> chart;

    public DateAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        DateTime date = DateTimeUtils.fromDays((int) value);

        if (chart.getVisibleXRange() > 30 * 6) {
            return date.monthOfYear().getAsShortText() + " " + date.year().getAsText();
        } else {
//            boolean showYear = date.year() == DateTime.now().year();
            return date.monthOfYear().getAsShortText() + " " + date.dayOfMonth().getAsShortText();
        }
    }
}
