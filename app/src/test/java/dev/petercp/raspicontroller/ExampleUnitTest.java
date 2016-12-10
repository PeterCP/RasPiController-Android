package dev.petercp.raspicontroller;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import dev.petercp.raspicontroller.utils.DateTimeUtils;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void test_doRandomStuff() {
        DateTime date1, date2;
        int days;
        date1 = DateTime.parse("2016-12-09", DateTimeFormat.forPattern("yyyy-MM-dd"));
        days = DateTimeUtils.toDays(date1);
        date2 = DateTimeUtils.fromDays(days);

        System.out.println(date1);
        System.out.println(days);
        System.out.println(date2);
    }
}