package com.hdweiss.morgand.orgdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper  extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "mOrgAnd.db";
    private static final int DATABASE_VERSION = 3;

    private RuntimeExceptionDao<OrgHierarchy, Integer> orgHierarchyRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, OrgHierarchy.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        createTestData();
    }

    private void createTestData() {
        RuntimeExceptionDao<OrgHierarchy, Integer> dao = getOrgHieararchyDao();

        OrgHierarchy parent = new OrgHierarchy();
        parent.title = "Parent";
        dao.create(parent);

        OrgHierarchy child = new OrgHierarchy();
        child.title = "Child";
        child.parent = parent;
        dao.create(child);

        Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, OrgHierarchy.class, true);

            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
        orgHierarchyRuntimeDao = null;
    }

    public RuntimeExceptionDao<OrgHierarchy, Integer> getOrgHieararchyDao() {
        if (orgHierarchyRuntimeDao == null) {
            orgHierarchyRuntimeDao = getRuntimeExceptionDao(OrgHierarchy.class);
        }
        return orgHierarchyRuntimeDao;
    }
}
