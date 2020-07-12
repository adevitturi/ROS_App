package com.github.ros.android.voicecommands;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/** And adapter that gives the fragments corresponding to each page. */
public class SlidePageAdapter extends FragmentStatePagerAdapter {
    public static final int PAGE_COUNT = 4;

    public static final int INFO_PAGE = 0;
    public static final int RECORDING_PAGE = 1;
    public static final int SETTINGS_PAGE = 2;
    public static final int LOCATIONS_PAGE = 3;

    private static final String RECORDING_PAGE_TITLE = "Command";
    private static final String SETTINGS_PAGE_TITLE = "Settings";
    private static final String LOCATIONS_PAGE_TITLE = "Locations";
    private static final String INFO_PAGE_TITLE = "Info";

    public SlidePageAdapter(FragmentManager fm) {
        super(fm);
    }

    /** Gets the fragments. */
    @Override
    public Fragment getItem(int i) {
        switch(i) {
            case INFO_PAGE: {
                return new InfoFragment();
            }
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

    /** Gets the page count. */
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    /** Gets the titles. */
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position) {
            case INFO_PAGE: {
                return INFO_PAGE_TITLE;
            }
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