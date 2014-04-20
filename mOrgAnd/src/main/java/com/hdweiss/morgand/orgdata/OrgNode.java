package com.hdweiss.morgand.orgdata;

import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return title + "\t" + tags;
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

    public String getProperty(String propertyName) {
        if (type == Type.Headline) {
            Pattern propertyPattern = Pattern.compile(":" + propertyName + ":\\s+(.+)");
            for(OrgNode child: children) {
                if (child.type == Type.Drawer) {
                    Matcher matcher = propertyPattern.matcher(child.title);
                    if (matcher.find()) {
                        return matcher.group(1).trim();
                    }
                }
            }

            return null;
        }

        if (parent != null)
            return parent.getProperty(propertyName);
        return null;
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

    public String getTodo() {
        if (type == Type.Headline) {

            Matcher matcher = OrgNodeUtils.todoPattern.matcher(title);
            if (matcher.find())
                return matcher.group(1);
        }
        return "";
    }

    public String getBody() {
        if (type == Type.Headline) {
            StringBuilder body = new StringBuilder();
            for(OrgNode child: children) {
                if (child.type == Type.Body)
                    body.append(child.title);
            }
            return body.toString();
        } else if (parent != null)
            return  parent.getBody();

        return title;
    }

    public ArrayList<OrgNode> getDisplayChildren() {
        ArrayList<OrgNode> nodes = new ArrayList<OrgNode>();
        boolean showSettings = PreferenceUtils.showSettings();
        boolean showDrawers = PreferenceUtils.showDrawers();
        for(OrgNode node: children) {
            switch (node.type) {
                case Drawer:
                    if (showDrawers)
                        nodes.add(node);
                    break;

                case Setting:
                    if (showSettings)
                        nodes.add(node);
                    break;

                default:
                    nodes.add(node);
            }
        }
        return nodes;
    }

    public OrgNode addChild(Type type, String title) {
        OrgNode node = new OrgNode();
        node.parent = this;
        node.file = this.file;
        node.type = type;
        node.title = title;

        OrgNodeRepository.getDao().create(node);
        return node;
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


    public static class OrgNodeCompare implements Comparator<OrgNode> {
        @Override
        public int compare(OrgNode node1, OrgNode node2) {
            return node1.title.compareTo(node2.title);
        }
    }
}
