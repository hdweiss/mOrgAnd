package com.hdweiss.morgand.orgdata;

import android.content.Context;

import com.hdweiss.morgand.Application;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "OrgFiles")
public class OrgFile {

    public static final String AGENDA_FILE_ALIAS = "Agenda Views";

    @DatabaseField(id = true)
    public String path = "";

    @DatabaseField
    public long lastModified;

    public static final String NODE_FIELD_NAME = "node";
    @DatabaseField(foreign = true, columnName = NODE_FIELD_NAME)
    public OrgNode node;

    public static RuntimeExceptionDao<OrgFile, String> getDao() {
        Context context = Application.getInstace();
        return OpenHelperManager.getHelper(context, DatabaseHelper.class).getRuntimeExceptionDao(OrgFile.class);
    }
}
