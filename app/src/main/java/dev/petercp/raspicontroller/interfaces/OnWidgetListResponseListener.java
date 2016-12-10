package dev.petercp.raspicontroller.interfaces;

import java.util.List;

import dev.petercp.raspicontroller.classes.BaseWidget;

public interface OnWidgetListResponseListener {
    void onWidgetListResponse(List<BaseWidget> widgets);
}
