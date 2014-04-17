package com.hdweiss.morgand.orgdata;

import android.content.Context;

import com.hdweiss.morgand.Application;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrgNodeRepository {

    public static List<OrgNode> getRootNodes() {
        try {
            List<OrgNode> children = getDao().queryBuilder().where().isNull(OrgNode.PARENT_FIELD_NAME).and().ne(OrgNode.STATE_FIELD_NAME, OrgNode.State.Deleted).query();
            Collections.sort(children, new OrgNode.OrgNodeCompare());
            return children;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgNode>();
    }

    public static RuntimeExceptionDao<OrgNode, Integer> getDao() {
        Context context = Application.getInstace();
        return OpenHelperManager.getHelper(context, DatabaseHelper.class).getRuntimeExceptionDao(OrgNode.class);
    }

    public static void deleteAll() {
        try {
            getDao().deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<OrgNode> getScheduledNodes(String filename, boolean showHabits) {
        try {
            Where<OrgNode, Integer> query = getDao().queryBuilder().where().eq(OrgNode.FILE_FIELD_NAME, filename);
            query.and().like(OrgNode.TITLE_FIELD_NAME, "%<%>%");

            if (showHabits == false)
                query.and().not().like(OrgNode.TITLE_FIELD_NAME, "%:STYLE: habit%");

            return query.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgNode>();
    }
}
