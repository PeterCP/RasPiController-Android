package dev.petercp.raspicontroller.interfaces;

import java.util.List;

import dev.petercp.raspicontroller.classes.UsageStatistics;

public interface OnUsageStatisticsResponseListener {
    void onUsageStatisticsResponse(List<UsageStatistics> statistics);
}
