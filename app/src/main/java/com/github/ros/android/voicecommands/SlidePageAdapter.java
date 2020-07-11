package com.github.ros.android.voicecommands;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class SlidePageAdapter extends FragmentStatePagerAdapter {
    public static final int RECORDING_PAGE = 0;
    public static final int SETTINGS_PAGE = 1;
    public static final int LOCATIONS_PAGE = 2;

    private static final String RECORDING_PAGE_TITLE = "Command";
    private static final String SETTINGS_PAGE_TITLE = "Settings";
    private static final String LOCATIONS_PAGE_TITLE = "Locations";

    public SlidePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case RECORDING_PAGE: {
                return new CommandFragment();
            }
            case SETTINGS_PAGE: {
                return new SettingsFragment();
            }
            case LOCATIONS_PAGE: {
                return new LocationsFragment();
            }
            default:{
                throw new IllegalArgumentException("invalid entry");
            }
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case RECORDING_PAGE: {
                return RECORDING_PAGE_TITLE;
            }
            case SETTINGS_PAGE: {
                return SETTINGS_PAGE_TITLE;
            }case LOCATIONS_PAGE: {
                return LOCATIONS_PAGE_TITLE;
            }
            default:{
                throw new IllegalArgumentException("invalid entry");
            }
        }
    }
}