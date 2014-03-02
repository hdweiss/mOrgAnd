package com.hdweiss.morgand.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.outline.OutlineListView;
import com.hdweiss.morgand.orgdata.OrgNode;
import com.j256.ormlite.android.apptools.OpenHelperManager;

public class OutlineFragment extends Fragment {

    private final static String OUTLINE_NODES = "nodes";
    private final static String OUTLINE_CHECKED_POS = "selection";
    private final static String OUTLINE_SCROLL_POS = "scrollPosition";

    private OutlineListView listView;

    @Override
    public void onDestroy() {
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_outline, container, false);
        listView = (OutlineListView) rootView.findViewById(R.id.list);
        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setActivity(getActivity());
        listView.setData(OrgNode.getRootNodes());
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null)
            return;

        long[] state = savedInstanceState.getLongArray(OUTLINE_NODES);
        if(state != null)
            listView.setState(state);

        int checkedPos= savedInstanceState.getInt(OUTLINE_CHECKED_POS, 0);
        listView.setItemChecked(checkedPos, true);

        int scrollPos = savedInstanceState.getInt(OUTLINE_SCROLL_POS, 0);
        listView.setSelection(scrollPos);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray(OUTLINE_NODES, listView.getState());
        outState.putInt(OUTLINE_CHECKED_POS, listView.getCheckedItemPosition());
        outState.putInt(OUTLINE_SCROLL_POS, listView.getFirstVisiblePosition());
    }
}
