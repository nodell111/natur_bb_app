package com.natalie.naturbb;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.natalie.naturbb.fragments.MapsFragment;
import com.natalie.naturbb.fragments.listfragment;

public class MyViewPageAdapter extends FragmentStateAdapter {
    public MyViewPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment (int position) {
        switch (position){
            case 0:
                return new listfragment();
            case 1:
                return new MapsFragment();
            default:
                return new listfragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
