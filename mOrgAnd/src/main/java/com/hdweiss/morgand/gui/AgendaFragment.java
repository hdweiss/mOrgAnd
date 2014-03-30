package com.hdweiss.morgand.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.OrgNode;

import java.sql.SQLException;
import java.util.List;

public class AgendaFragment extends OutlineFragment {

    @Override
    public void refreshView() {
        try {
            List<OrgNode> orgNodes = OrgNode.getDao().queryBuilder().where().like(OrgNode.TITLE_FIELD_NAME, "%* NEXT%").query();
            listView.setData(orgNodes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
