package edu.poly.nhtr.Adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class TabLayoutAdapter extends FragmentStateAdapter {

    private final List<Class<? extends Fragment>> fragmentClasses;
    private final Bundle fragmentArgs;

    public TabLayoutAdapter(@NonNull FragmentActivity fragmentActivity, List<Class<? extends Fragment>> fragmentClasses, Bundle fragmentArgs) {
        super(fragmentActivity);
        this.fragmentClasses = fragmentClasses;
        this.fragmentArgs = fragmentArgs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        try {
            Fragment fragment = fragmentClasses.get(position).newInstance();
            fragment.setArguments(fragmentArgs);
            return fragment;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot instantiate fragment", e);
        }
    }

    @Override
    public int getItemCount() {
        return fragmentClasses.size();
    }

    @Override
    public long getItemId(int position) {
        // Return a unique ID for each position to force recreation
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        // Always return true so that fragments are recreated
        return true;
    }
}
