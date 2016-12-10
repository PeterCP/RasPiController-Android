package dev.petercp.raspicontroller.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dev.petercp.raspicontroller.activities.ManagerActivity;
import dev.petercp.raspicontroller.R;

/**
 * Fragment that will be managed by {@link ManagerActivity}. The activity is used as
 * a manager and method dispatcher for this fragment.
 */
public abstract class ManagedFragment extends Fragment {

    private ManagerActivity activity;
    private boolean isRunning;

    /**
     * Make sure that the {@link ManagedFragment} is called from a {@link ManagerActivity} instance.
     * @param   context   The context provided by the Android runtime.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activity = (ManagerActivity) context;
        } catch (ClassCastException ex) {
            throw new RuntimeException(context.toString() + " must be an instance of ManagerActivity");
        }
    }

    /**
     * Make sure the {@link ManagerActivity} is freed on fragment detach.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        isRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Update the appbar title, FAB visibility, and navigation drawer in the {@link ManagerActivity}.
     * Should be called inside {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     */
    public void updateManagerActivityUI() {
        if (getManagerActivity() != null) {
            getManagerActivity().setAppbarTitle(getTitle());
            getManagerActivity().setIsFABVisible(hasFAB());
            if (getNavigationDrawerItemId() != null)
                getManagerActivity().setActiveNavigationItem(getNavigationDrawerItemId());
        }
    }

    /**
     * Returns the attached {@link ManagerActivity}, allowing the fragment to communicate with it.
     * @return   The attached {@link ManagerActivity}.
     */
    public ManagerActivity getManagerActivity() {
        return activity;
    }

    /**
     * Get the title to be set in the container activity's action bar. This method should be
     * overridden if you want to set a custom title. By default it returns the application's title.
     * @return   Title string.
     */
    public String getTitle() {
        return getContext().getString(R.string.app_name);
    }

    /**
     * Get the navigation drawer element id to set as active. If null is returned, then
     * no operation will be performed regarding the navigation drawer.
     * @return   Navigation drawer element id.
     */
    @Nullable
    @IdRes
    public Integer getNavigationDrawerItemId() {
        return null;
    }

    /**
     * Signals whether the fragment required a FAB.
     * @return   Boolean telling whether the fragment requires a FAB.
     */
    public boolean hasFAB() {
        return false;
    }

    /**
     * Handle the FAB being clicked. Override to add logic.
     * @param   view   View parameter passed to the parent callback.
     */
    public void onFABClicked(View view) {}
}
