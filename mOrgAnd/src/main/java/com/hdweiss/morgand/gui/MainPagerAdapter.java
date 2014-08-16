package com.hdweiss.morgand.gui;

import android.content.Context;
import android.os.Bundle;
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
        Fragment fragment;
        Bundle argumentBundle = new Bundle();

        switch (position) {
            case 0:
                fragment = new OutlineFragment();
                break;

            case 1:
                fragment = new AgendaFragment();
                argumentBundle.putString("query", "NEXT%");
                break;

            case 2:
                fragment = new AgendaFragment();
                argumentBundle.putString("query", "TODO%");
                break;

            default:
                fragment = new OutlineFragment();
                break;
        }

        fragment.setArguments(argumentBundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        switch (position) {
            case 0:
                return context.getString(R.string.title_outline).toUpperCase(l);
            case 1:
                return "NEXTs";
            case 2:
                return "TODOs";
        }
        return null;
    }
}
