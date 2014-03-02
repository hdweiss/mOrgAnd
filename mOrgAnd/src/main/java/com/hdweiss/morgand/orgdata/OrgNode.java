package com.hdweiss.morgand.orgdata;

import android.content.Context;

import com.hdweiss.morgand.Application;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "OrgNodes")
public class OrgNode {


    public enum Type {
        File, Directory, Date, Heading, Body, Drawer, Check
    };

    public enum State {
        Clean, Dirty, Deleted
    }

    @DatabaseField(generatedId = true)
    public int Id;

    public static final String TITLE_FIELD_NAME = "title";
    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    public static final String PARENT_FIELD_NAME = "parent";
    @DatabaseField(foreign = true, columnName = PARENT_FIELD_NAME)
    public OrgNode parent;

    @ForeignCollectionField(eager = false, foreignFieldName = PARENT_FIELD_NAME)
    public ForeignCollection<OrgNode> children;

    @DatabaseField
    public Type type;

    @DatabaseField
    public int lineNumber;

    public static final String STATE_FIELD_NAME = "state";
    @DatabaseField(columnName = STATE_FIELD_NAME)
    public State state = State.Clean;

    public static final String FILE_FIELD_NAME = "file";
    @DatabaseField(foreign =  true, columnName = FILE_FIELD_NAME)
    public OrgFile file;

    @DatabaseField
    public String inheritedTags;

    public int getLevel() {
        int level = 0;
        OrgNode currentParent = parent;
        while(currentParent != null) {
            level++;
            currentParent = currentParent.parent;
        }

        return level;
    }

    public String toString() {
        return title + " (" + children.size() + ")";
    }

    public String toStringRecursively() {
        StringBuilder builder = new StringBuilder();

        builder.append(toString()).append("\n");

        for(OrgNode child : children) {
            builder.append(child.toString()).append("\n");
        }

        return builder.toString();
    }

    public boolean isNodeEditable() {
        return true;
    }

    public static List<OrgNode> getRootNodes() {
        try {
            return getDao().queryBuilder().where().isNull(PARENT_FIELD_NAME).and().ne(STATE_FIELD_NAME, State.Deleted).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgNode>();
    }

    public static RuntimeExceptionDao<OrgNode, Integer> getDao() {
        Context context = Application.getInstace();
        return OpenHelperManager.getHelper(context, DatabaseHelper.class).getRuntimeExceptionDao(OrgNode.class);
    }
}
