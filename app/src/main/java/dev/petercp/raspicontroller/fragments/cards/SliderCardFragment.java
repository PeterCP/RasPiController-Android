package dev.petercp.raspicontroller.fragments.cards;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;

import dev.petercp.raspicontroller.R;
import dev.petercp.raspicontroller.activities.ChartActivity;
import dev.petercp.raspicontroller.interfaces.OnSingleWidgetResponseListener;
import dev.petercp.raspicontroller.interfaces.WidgetFragmentListener;
import dev.petercp.raspicontroller.classes.BaseWidget;
import dev.petercp.raspicontroller.classes.SliderWidget;

public class SliderCardFragment extends Fragment implements
        View.OnClickListener,
        PopupMenu.OnMenuItemClickListener,
        SeekBar.OnSeekBarChangeListener {

    private static final String ARG_WIDGET = "widget";

    private View rootView;
    private SliderWidget widget;
    private WidgetFragmentListener listener;

    public SliderCardFragment() {}

    public static SliderCardFragment newInstance(SliderWidget widget) {
        SliderCardFragment fragment = new SliderCardFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_WIDGET, widget);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Base fragment methods
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            widget = getArguments().getParcelable(ARG_WIDGET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_slider, container, false);

        rootView.findViewById(R.id.options).setOnClickListener(this);
        rootView.findViewById(R.id.update).setOnClickListener(this);
        ((SeekBar) rootView.findViewById(R.id.value)).setOnSeekBarChangeListener(this);

        setValues();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof WidgetFragmentListener) {
            listener = (WidgetFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement WidgetFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    /**
     * Listener methods
     */

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                listener.onWidgetUpdateRequested(widget, new OnSingleWidgetResponseListener() {
                    @Override
                    public void onSingleWidgetResponse(BaseWidget widget) {
                        setWidget((SliderWidget) widget);
                    }
                });
                return true;
            case R.id.action_usage:
                ChartActivity.startNew(getContext(), widget);
                return true;
            case R.id.action_delete:
                listener.onWidgetDeleteRequested(widget, new OnSingleWidgetResponseListener() {
                    @Override
                    public void onSingleWidgetResponse(BaseWidget widget) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .remove(SliderCardFragment.this).commit();
                    }
                });
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.options:
                PopupMenu popupMenu = new PopupMenu(getContext(),
                        rootView.findViewById(R.id.context_toggle), Gravity.END);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.getMenuInflater().inflate(R.menu.card_context, popupMenu.getMenu());
                popupMenu.show();
                break;
            case R.id.update:
                SeekBar bar = (SeekBar) rootView.findViewById(R.id.value);
                widget.setValue(bar.getProgress());
                listener.onWidgetValueUpdated(widget, new OnSingleWidgetResponseListener() {
                    @Override
                    public void onSingleWidgetResponse(BaseWidget widget) {}
                });
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        TextView valueLabel = (TextView) rootView.findViewById(R.id.value_indicator);
        valueLabel.setText(String.valueOf(value));
    }

    @Override public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override public void onStopTrackingTouch(SeekBar seekBar) {}

    /**
     * Extra methods
     */

    private void setValues() {
        ((TextView) rootView.findViewById(R.id.name)).setText(widget.getName());
        ((SeekBar) rootView.findViewById(R.id.value)).setProgress(widget.getValue());
    }

    public void setWidget(SliderWidget widget) {
        this.widget = widget;
        setValues();
    }
}
