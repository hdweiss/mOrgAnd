package com.hdweiss.morgand.orgdata;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "OrgHierarchy")
public class OrgHierarchy {

    public static final String TITLE_FIELD_NAME = "title";

    public enum Type {
        File, Folder, Date, Node, Drawer, Check
    };

    public enum State {
        Clean, Dirty, Deleted
    }

    @DatabaseField(generatedId = true)
    public int Id;

    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    @DatabaseField(foreign = true)
    public OrgHierarchy parent;

    @ForeignCollectionField(eager = false, foreignFieldName = "parent")
    public ForeignCollection<OrgHierarchy> children;

    @DatabaseField
    public Type type;

    @DatabaseField
    public int lineNumber;

    @DatabaseField
    public State state;


    public int getLevel() {
        return 0;
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
}
