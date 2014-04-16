package com.hdweiss.morgand.gui;

import com.hdweiss.morgand.gui.outline.OutlineAdapter;
import com.hdweiss.morgand.orgdata.OrgNode;

import java.sql.SQLException;
import java.util.List;

public class AgendaFragment extends OutlineFragment {

    @Override
    public void refreshView() {
        try {
            List<OrgNode> orgNodes = OrgNode.getDao().queryBuilder().where().like(OrgNode.TITLE_FIELD_NAME, "%* NEXT%").query();
            listView.setData(orgNodes);
            ((OutlineAdapter) listView.getAdapter()).setAgendaMode(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
