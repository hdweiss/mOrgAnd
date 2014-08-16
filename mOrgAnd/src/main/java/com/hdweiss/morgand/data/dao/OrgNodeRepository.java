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
        if (node.type != OrgNode.Type.Directory) // Directories should always have state=clean
            node.state = OrgNode.State.Added;

        DatabaseHelper.getOrgNodeDao().create(node);
    }

    public static void delete(OrgNode node) {
        if (node.type == OrgNode.Type.File || node.type == OrgNode.Type.Directory)
            deleteWithoutUpdate(node);
        else
            deleteWithUpdate(node);
    }

    private static void deleteWithUpdate(OrgNode node) {
        node.state = OrgNode.State.Deleted;
        DatabaseHelper.getOrgNodeDao().update(node);

        for(OrgNode child: node.children)
            deleteWithUpdate(child);
    }

    private static void deleteWithoutUpdate(OrgNode node) {
        for(OrgNode child: node.children)
            deleteWithoutUpdate(child);

        DatabaseHelper.getOrgNodeDao().delete(node);
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

    public static int delete(OrgFile orgFile) throws SQLException {
        DeleteBuilder<OrgNode, Integer> deleteBuilder = deleteBuilder();
        deleteBuilder.where().eq(OrgNode.FILE_FIELD_NAME, orgFile);
        return deleteBuilder.delete();
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

    public static List<OrgNode> getDirtyNodes(OrgFile file) throws SQLException {
        Where<OrgNode, Integer> builder = queryBuilder().orderBy(OrgNode.LINENUMBER_FIELD_NAME, false).where();
        builder.eq(OrgNode.FILE_FIELD_NAME, file);
        builder.and().ne(OrgNode.STATE_FIELD_NAME, OrgNode.State.Clean);
        return builder.query();
    }

    public static OrgNode getDefaultCaptureNode() {
        return new OrgNode(); // TODO
    }
}
