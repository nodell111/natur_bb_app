package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.fragment.app.*;
import androidx.viewpager2.adapter.FragmentStateAdapter;



public class TabPagerAdapter extends FragmentStateAdapter {
    int tabCount;
    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity, int
            numberOfTabs) {
        super(fragmentActivity);
        this.tabCount = numberOfTabs;
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new fragment_BottomTab_parks();
            case 1:
                return new fragment_BottomTab_fav();
            default:
                return null;
        }
    }
    @Override
    public int getItemCount() {
        return tabCount;
    }
}
