package com.hdweiss.morgand.orgdata;

import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

@DatabaseTable(tableName = "OrgNodes")
public class OrgNode {

    public enum Type {
        File, Directory, Date, Headline, Body, Drawer, Checkbox, Setting
    }

    public enum State {
        Clean, Dirty, Deleted
    }

    @DatabaseField(generatedId = true)
    public int Id;

    public static final String PARENT_FIELD_NAME = "parent";
    @DatabaseField(foreign = true, columnName = PARENT_FIELD_NAME, foreignAutoRefresh=true, canBeNull=true, maxForeignAutoRefreshLevel=3)
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


    public static final String TITLE_FIELD_NAME = "title";
    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    @DatabaseField
    public String tags;

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

    public boolean isEditable() {
        if (type == Type.File || type == Type.Directory)
            return false;

        if (file.isEditable() == false)
            return false;

        return true;
    }

    public List<OrgNodeDate> getOrgNodeDates() {
        ArrayList<OrgNodeDate> dates = new ArrayList<OrgNodeDate>();

        Matcher matcher = OrgNodeUtils.dateMatcher.matcher(title);
        while(matcher.find()) {
            String type = matcher.group(1);
            String startDate = matcher.group(2);
            String endDate = matcher.group(3);

            try {
                OrgNodeDate orgNodeDate = new OrgNodeDate(startDate);
                orgNodeDate.type = type != null ? type : "";

                orgNodeDate.setTitle(getTitle());
                dates.add(orgNodeDate);
            } catch (IllegalArgumentException ex) {}
        }

        return dates;
    }

    public String getTitle() {

        if (type != Type.Headline) {
            if (parent != null) {
                return parent.getTitle();
            }
            else {
                return "";
            }
        }

        return title.replaceAll("^\\** ", "");
    }

    public static class OrgNodeCompare implements Comparator<OrgNode> {
        @Override
        public int compare(OrgNode node1, OrgNode node2) {
            return node1.title.compareTo(node2.title);
        }
    }
}
