package com.hdweiss.morgand.data.dao;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrgNodeRepository {

    public static OrgNode queryForId(Integer id) {
        return DatabaseHelper.getOrgNodeDao().queryForId(id);
    }

    public static void update(OrgNode node) {
        if (node.state != OrgNode.State.Added) // Keep the added state when updating nodes
            node.state = OrgNode.State.Updated;

        DatabaseHelper.getOrgNodeDao().update(node);
    }

    public static void create(OrgNode node) {
        node.state = OrgNode.State.Added;
        DatabaseHelper.getOrgNodeDao().create(node);
    }


    public static QueryBuilder<OrgNode, Integer> queryBuilder() {
        return DatabaseHelper.getOrgNodeDao().queryBuilder();
    }

    public static DeleteBuilder<OrgNode, Integer> deleteBuilder() {
        return DatabaseHelper.getOrgNodeDao().deleteBuilder();
    }


    public static void deleteAll() {
        try {
            deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<OrgNode> getRootNodes() {
        try {
            List<OrgNode> children = queryBuilder().where().isNull(OrgNode.PARENT_FIELD_NAME).and().ne(OrgNode.STATE_FIELD_NAME, OrgNode.State.Deleted).query();
            Collections.sort(children, new OrgNode.OrgNodeCompare());
            return children;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgNode>();
    }

    public static List<OrgNode> getScheduledNodes(String filename, boolean showHabits) {
        try {
            Where<OrgNode, Integer> query = queryBuilder().where().eq(OrgNode.FILE_FIELD_NAME, filename);
            query.and().like(OrgNode.TITLE_FIELD_NAME, "%<%>%");

            if (showHabits == false)
                query.and().not().like(OrgNode.TITLE_FIELD_NAME, "%:STYLE: habit%");

            return query.query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgNode>();
    }

    public static OrgNode getDefaultCaptureNode() {
        return new OrgNode(); // TODO
    }
}
