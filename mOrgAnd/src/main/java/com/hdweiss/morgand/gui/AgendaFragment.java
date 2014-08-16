package com.hdweiss.morgand.gui;

import android.view.Menu;
import android.view.MenuInflater;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.DataUpdatedEvent;
import com.hdweiss.morgand.gui.outline.OutlineAdapter;
import com.hdweiss.morgand.gui.outline.OutlineFragment;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;
import java.util.List;

public class AgendaFragment extends OutlineFragment {

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Empty options menu for now.
    }

    @Subscribe
    public void refreshView(DataUpdatedEvent event) {
        refreshView();
    }

    @Override
    protected void refreshView() {
        try {
            String query = getArguments().getString("query", "TODO%");
            List<OrgNode> orgNodes = OrgNodeRepository.queryBuilder().where().like(OrgNode.TITLE_FIELD_NAME, query)
                    .and().not().like(OrgNode.FILE_FIELD_NAME, "%mOrgAnd.wiki/Development/Todo.org").query();
            listView.setData(orgNodes);
            ((OutlineAdapter) listView.getAdapter()).setAgendaMode(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
