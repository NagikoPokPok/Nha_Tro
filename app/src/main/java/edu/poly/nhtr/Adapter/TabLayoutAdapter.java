package edu.poly.nhtr.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

import edu.poly.nhtr.fragment.RoomBillFragment;
import edu.poly.nhtr.fragment.RoomContractFragment;
import edu.poly.nhtr.fragment.RoomGuestFragment;
import edu.poly.nhtr.fragment.RoomServiceFragment;

public class TabLayoutAdapter extends FragmentPagerAdapter {

    private final ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    private final ArrayList<String> titles = new ArrayList<>();
    public TabLayoutAdapter(@NonNull FragmentManager fragment, int behavior) {
        super(fragment, behavior);
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentArrayList.add(fragment);
        titles.add(title);

    }

    @Override
    public int getCount() {
        return fragmentArrayList.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentArrayList.get(position);
    }
}
