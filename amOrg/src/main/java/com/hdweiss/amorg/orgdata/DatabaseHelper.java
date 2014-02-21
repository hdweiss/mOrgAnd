package com.hdweiss.amorg.orgdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper  extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "amOrg.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 2;

    // the DAO object we use to access the SimpleData table
    private Dao<OrgNode, Integer> orgNodeDao = null;
    private RuntimeExceptionDao<OrgNode, Integer> orgNodeRuntimeDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onCreate");
            TableUtils.createTable(connectionSource, OrgNode.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        // here we try inserting data in the on-create as a test
        RuntimeExceptionDao<OrgNode, Integer> dao = getOrgNodeDataDao();
        // create some entries in the onCreate
        OrgNode node = new OrgNode();
        node.name = "test node";
        dao.create(node);

        OrgNode childNode = new OrgNode();
        childNode.name = "child node";
        node.children.add(childNode);
        dao.create(childNode);
        Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate");
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(DatabaseHelper.class.getName(), "onUpgrade");
            TableUtils.dropTable(connectionSource, OrgNode.class, true);
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */
    public Dao<OrgNode, Integer> getOrgNodeDao() throws SQLException {
        if (orgNodeDao == null) {
            orgNodeDao = getDao(OrgNode.class);
        }
        return orgNodeDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
    public RuntimeExceptionDao<OrgNode, Integer> getOrgNodeDataDao() {
        if (orgNodeRuntimeDao == null) {
            orgNodeRuntimeDao = getRuntimeExceptionDao(OrgNode.class);
        }
        return orgNodeRuntimeDao;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        orgNodeDao = null;
    }
}
