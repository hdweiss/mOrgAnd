package com.hdweiss.morgand.gui.outline;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.edit.EditDateFragment;
import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.orgdata.OrgNodeRepository;
import com.hdweiss.morgand.synchronizer.DataUpdatedEvent;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class OutlineFragment extends Fragment {

    private final static String OUTLINE_NODES = "nodes";
    private final static String OUTLINE_LEVELS = "levels";
    private final static String OUTLINE_EXPANDED = "expanded";
    private final static String OUTLINE_CHECKED_POS = "selection";
    private final static String OUTLINE_SCROLL_POS = "scrollPosition";

    protected OutlineListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application.getBus().register(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        Application.getBus().unregister(this);
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
        refreshView();
    }


    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState == null)
            return;

        long[] state = savedInstanceState.getLongArray(OUTLINE_NODES);
        ArrayList<Integer> levels = savedInstanceState.getIntegerArrayList(OUTLINE_LEVELS);
        boolean[] expanded = savedInstanceState.getBooleanArray(OUTLINE_EXPANDED);
        if(state != null)
            listView.setState(state, levels, expanded);

        int checkedPos= savedInstanceState.getInt(OUTLINE_CHECKED_POS, 0);
        listView.setItemChecked(checkedPos, true);

        int scrollPos = savedInstanceState.getInt(OUTLINE_SCROLL_POS, 0);
        listView.setSelection(scrollPos);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray(OUTLINE_NODES, listView.getNodeState());
        outState.putIntegerArrayList(OUTLINE_LEVELS, listView.getLevelState());
        outState.putBooleanArray(OUTLINE_EXPANDED, listView.getExpandedState());
        outState.putInt(OUTLINE_CHECKED_POS, listView.getCheckedItemPosition());
        outState.putInt(OUTLINE_SCROLL_POS, listView.getFirstVisiblePosition());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.outline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if (listView != null)
                    listView.collapseCurrent();
                break;

            case R.id.add_child:
                int position = listView.getCheckedItemPosition();
                if (position < 0)
                    return true;

                OrgNode node = (OrgNode) listView.getAdapter().getItem(position);

                EditDateFragment editHeadingFragment = new EditDateFragment();
                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
                fragmentTransaction.add(editHeadingFragment, "dialog");
                fragmentTransaction.commit();

//                EditHeadingFragment editHeadingFragment = new EditHeadingFragment(node);
//                FragmentTransaction fragmentTransaction = getActivity().getFragmentManager().beginTransaction();
//                fragmentTransaction.add(editHeadingFragment, "dialog");
//                fragmentTransaction.commit();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Subscribe
    public void refreshView(DataUpdatedEvent event) {
        refreshView();
    }
    protected void refreshView() {
        listView.setData(OrgNodeRepository.getRootNodes());
    }
}
