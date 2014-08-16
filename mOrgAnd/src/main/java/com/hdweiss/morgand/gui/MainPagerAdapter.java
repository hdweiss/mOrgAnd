package com.hdweiss.morgand.gui;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.outline.OutlineFragment;

import java.util.Locale;

public class MainPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
        this.context = Application.getInstace();
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        switch (position) {
            case 0:
                return new OutlineFragment();
            case 1:
                return new AgendaFragment();

            default:
                return new AgendaFragment();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_outline).toUpperCase(l);
            case 1:
                return context.getString(R.string.title_agenda).toUpperCase(l);
        }
        return null;
    }
}
