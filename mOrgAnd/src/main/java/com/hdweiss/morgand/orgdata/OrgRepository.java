package com.hdweiss.morgand.orgdata;

import android.text.TextUtils;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class OrgRepository {

    private String path;
    private RuntimeExceptionDao<OrgNode, Integer> nodeDao;
    private RuntimeExceptionDao<OrgFile, String> fileDao;

    public OrgRepository(String path) {
        if (TextUtils.isEmpty(path))
            throw new IllegalArgumentException("Path can't be empty");

        this.path = path;
        this.nodeDao = OrgNode.getDao();
        this.fileDao = OrgFile.getDao();
    }

    public void read() {
        File rootFolder = new File(path);

        if (rootFolder.exists() == false)
            throw new IllegalArgumentException("Folder " + path + " does not exist");

        if (rootFolder.canRead() == false)
            throw new IllegalArgumentException("Can't read " + path);

        read(rootFolder, null);
    }

    private void read(File parentFile, OrgNode parent) {
        for (File file : parentFile.listFiles()) {
            if (file.isDirectory() && file.isHidden() == false) {
                OrgNode directoryNode = findOrCreateDirectoryNode(file, parent);
                read(file, directoryNode);
            } else if (file.isFile() && file.getName().endsWith(".org")) {
                parseOrgFile(file, parent);
            }
        }
    }

    private OrgNode findOrCreateDirectoryNode(File file, OrgNode parent) {
        try {
            OrgNode node =
                    nodeDao.queryBuilder().where().eq(OrgNode.PARENT_FIELD_NAME, parent).and().
                            eq(OrgNode.TITLE_FIELD_NAME, file.getName()).queryForFirst();
            if (node != null)
                return node;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        OrgNode node = new OrgNode();
        node.type = OrgNode.Type.Directory;
        node.parent = parent;
        node.title = file.getName();
        nodeDao.create(node);
        return node;
    }

    private void parseOrgFile(File file, OrgNode parent) {
        OrgFile orgFile = fileDao.queryForId(file.getAbsolutePath());

        if (orgFile == null) {
            orgFile = new OrgFile();
            orgFile.path = file.getAbsolutePath();
        } else {
            if (file.lastModified() <= orgFile.lastModified)
                return;

            try {
                nodeDao.deleteBuilder().where().eq(OrgNode.FILE_FIELD_NAME, orgFile).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            new OrgFileParser().parse(file, orgFile, parent);
            orgFile.lastModified = file.lastModified();
            fileDao.createOrUpdate(orgFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
