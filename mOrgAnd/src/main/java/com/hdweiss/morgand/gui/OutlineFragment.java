package com.hdweiss.morgand.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.DatabaseHelper;
import com.hdweiss.morgand.orgdata.OrgHierarchy;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.sql.SQLException;

public class OutlineFragment extends Fragment {

    private TextView textView;

    private RuntimeExceptionDao<OrgHierarchy, Integer> orgHierDao;

    @Override
    public void onDestroy() {
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        textView = (TextView) rootView.findViewById(R.id.section_label);

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        orgHierDao = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class).getOrgHieararchyDao();

        try {
            OrgHierarchy hier = orgHierDao.queryBuilder().where().eq(OrgHierarchy.TITLE_FIELD_NAME, "Parent").queryForFirst();
            textView.setText(hier.toStringRecursively());
        } catch (SQLException ex) {}
    }
}
