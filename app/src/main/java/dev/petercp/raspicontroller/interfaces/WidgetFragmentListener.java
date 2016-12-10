package dev.petercp.raspicontroller.interfaces;

import dev.petercp.raspicontroller.classes.BaseWidget;

public interface WidgetFragmentListener {
    void onWidgetUpdateRequested(BaseWidget widget, OnSingleWidgetResponseListener listener);
    void onWidgetValueUpdated(BaseWidget widget, OnSingleWidgetResponseListener listener);
    void onWidgetDeleteRequested(BaseWidget widget, OnSingleWidgetResponseListener listener);
}
