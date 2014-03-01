package com.hdweiss.morgand.orgdata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "OrgFiles")
public class OrgFile {

    public static final String AGENDA_FILE_ALIAS = "Agenda Views";

    @DatabaseField(generatedId = true)
    public int Id;

    @DatabaseField
    public String name = "";

    @DatabaseField
    public String path = "";

    @DatabaseField
    public Date lastTime;
}
