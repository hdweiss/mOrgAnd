package com.hdweiss.morgand.orgdata;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "OrgAgendas")
public class OrgAgenda {

    @DatabaseField(generatedId = true)
    public int Id;

    public static final String TITLE_FIELD_NAME = "title";
    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    public static final String QUERY_FIELD_NAME = "query";
    @DatabaseField(columnName = QUERY_FIELD_NAME)
    public String query;


}
