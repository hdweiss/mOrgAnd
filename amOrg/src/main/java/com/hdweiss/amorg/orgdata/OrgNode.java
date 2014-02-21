package com.hdweiss.amorg.orgdata;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "OrgNodes")
public class OrgNode {

    @DatabaseField(generatedId = true)
    public int Id;

    @DatabaseField
    public String name = "";

    @DatabaseField(foreign = true)
    public OrgNode parent;

    @ForeignCollectionField(eager = false, foreignFieldName = "parent")
    public ForeignCollection<OrgNode> children;

    public OrgNode() {
    }

    public String toString() {
        return name;
    }
}
