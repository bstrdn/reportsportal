package ru.bstrdn.report.web;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class WebUtil {
    public static String getStartDate () {
        return LocalDate.now().minusMonths(1)
                .with(TemporalAdjusters.firstDayOfMonth()).toString();
    }
    public static String getEndDate () {
        return LocalDate.now().minusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth()).toString();
    }
}
