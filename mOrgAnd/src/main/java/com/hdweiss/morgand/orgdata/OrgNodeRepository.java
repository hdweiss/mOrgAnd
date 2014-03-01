package com.hdweiss.morgand.orgdata;

import android.content.Context;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class OrgNodeRepository {

    private DatabaseHelper db;
    Dao<OrgNode, Integer> orgNodeDao;

    public OrgNodeRepository(Context ctx)
    {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            db = dbManager.getHelper(ctx);
            orgNodeDao = db.getOrgNodeDao();
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }
    }

    public int create(OrgNode node)
    {
        try {
            return orgNodeDao.create(node);
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }
        return 0;
    }

    public int update(OrgNode node)
    {
        try {
            return orgNodeDao.update(node);
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }
        return 0;
    }

    public int delete(OrgNode node)
    {
        try {
            return orgNodeDao.delete(node);
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }
        return 0;
    }

    public List getAll()
    {
        try {
            return orgNodeDao.queryForAll();
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }
        return null;
    }

    public String allToString() {
        StringBuilder returnString = new StringBuilder();
        try {
            Iterator<OrgNode> it = orgNodeDao.queryForAll().iterator();

            while(it.hasNext()) {
                OrgNode node = it.next();
                returnString.append(node.toString());
                returnString.append(": " + node.children.size());
                returnString.append("\n");
            }
        } catch (SQLException e) {
            // TODO: Exception Handling
            e.printStackTrace();
        }

        return returnString.toString();
    }
}
