package com.edsel.mbtatracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class pageAdapter extends FragmentStatePagerAdapter {

    int numTabs;

    public pageAdapter(FragmentManager manager, int numTabs){
        super(manager);
        this.numTabs = numTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){

            case 0:
                return new mapFragment();
            case 1:
                return new profileFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return numTabs;
    }
}
