package com.hdweiss.morgand.gui;

import com.hdweiss.morgand.gui.outline.OutlineAdapter;
import com.hdweiss.morgand.gui.outline.OutlineFragment;
import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.orgdata.OrgNodeRepository;
import com.hdweiss.morgand.synchronizer.DataUpdatedEvent;
import com.squareup.otto.Subscribe;

import java.sql.SQLException;
import java.util.List;

public class AgendaFragment extends OutlineFragment {

    @Subscribe
    public void refreshView(DataUpdatedEvent event) {
        refreshView();
    }

    @Override
    protected void refreshView() {
        try {
            List<OrgNode> orgNodes = OrgNodeRepository.getDao().queryBuilder().where().like(OrgNode.TITLE_FIELD_NAME, "%* NEXT%")
                    .and().eq(OrgNode.FILE_FIELD_NAME, "/sdcard/morg/GTD.org").query();
            listView.setData(orgNodes);
            ((OutlineAdapter) listView.getAdapter()).setAgendaMode(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
