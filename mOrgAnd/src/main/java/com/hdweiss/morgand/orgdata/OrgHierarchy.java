package com.hdweiss.morgand.orgdata;

import android.content.Context;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "OrgHierarchy")
public class OrgHierarchy {


    public enum Type {
        File, Folder, Date, Node, Drawer, Check
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
    public OrgHierarchy parent;

    @ForeignCollectionField(eager = false, foreignFieldName = PARENT_FIELD_NAME)
    public ForeignCollection<OrgHierarchy> children;

    @DatabaseField
    public Type type;

    @DatabaseField
    public int lineNumber;

    public static final String STATE_FIELD_NAME = "state";
    @DatabaseField(columnName = STATE_FIELD_NAME)
    public State state = State.Clean;


    public int getLevel() {
        int level = 0;
        OrgHierarchy currentParent = parent;
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

        for(OrgHierarchy child : children) {
            builder.append(child.toString()).append("\n");
        }

        return builder.toString();
    }

    public boolean isNodeEditable() {
        return true;
    }

    public static List<OrgHierarchy> getRootNodes(Context context) {
        try {
            return getDao(context).queryBuilder().where().isNull(PARENT_FIELD_NAME).and().ne(STATE_FIELD_NAME, State.Deleted).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<OrgHierarchy>();
    }

    public static RuntimeExceptionDao<OrgHierarchy, Integer> getDao(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class).getOrgHieararchyDao();
    }
}
