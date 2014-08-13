package com.hdweiss.morgand.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.DataUpdatedEvent;
import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.gui.outline.OutlineFragment;
import com.hdweiss.morgand.settings.SettingsActivity;
import com.hdweiss.morgand.synchronizer.calendar.CalendarWrapper;
import com.hdweiss.morgand.synchronizer.writer.SyncWriterTask;
import com.squareup.otto.Subscribe;

import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = initActionbar();
        initViewPager(actionBar);
    }

    private ActionBar initActionbar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setHomeButtonEnabled(true);
        //actionBar.setDisplayHomeAsUpEnabled(false);
        return actionBar;
    }

    private void initViewPager(final ActionBar actionBar) {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Application.getBus().register(this);
    }

    @Override
    protected void onPause() {
        Application.getBus().unregister(this);
        super.onPause();
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
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
                    return getString(R.string.title_outline).toUpperCase(l);
                case 1:
                    return getString(R.string.title_agenda).toUpperCase(l);
            }
            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;

            case R.id.action_sync:
                SyncWriterTask synchronizerTask = new SyncWriterTask(this);
                synchronizerTask.execute();
                break;

            case R.id.action_clearDB:
                OrgNodeRepository.deleteAll();
                OrgFile.deleteAll();
                Application.getBus().post(new DataUpdatedEvent());
                new CalendarWrapper(this).deleteEntries();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Subscribe
    public void updateSyncProgress(SyncEvent event) {
        if (event.state == SyncEvent.State.Done) {
            setProgress(Window.PROGRESS_END);
            return;
        }

        switch (event.state) {
            case Intermediate:
                setProgressBarVisibility(true);
                setProgressBarIndeterminate(true);
                break;

            case Progress:
                setProgressBarIndeterminate(false);
                int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * event.progress;
                setProgress(progress);
                break;

            case SecondaryProgress:
                setProgressBarIndeterminate(false);
                int secondaryProgress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * event.progress;
                setSecondaryProgress(secondaryProgress);
                break;
        }
    }
}
