package com.hdweiss.morgand.synchronizer.parser;

import android.text.TextUtils;

import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.Where;

import java.io.File;
import java.io.FileFilter;
import java.sql.SQLException;
import java.util.ArrayList;

public class OrgRepository {

    private String path;
    private OrgNodeRepository nodeDao;
    private RuntimeExceptionDao<OrgFile, String> fileDao;

    public OrgRepository(String path) {
        if (TextUtils.isEmpty(path))
            throw new IllegalArgumentException("Path can't be empty");

        this.path = path;
        this.nodeDao = new OrgNodeRepository();
        this.fileDao = OrgFile.getDao();
    }

    /**
     * @return List of OrgFiles that have been modified. Both file and file.node need to be updated/created.
     */
    public ArrayList<OrgFile> getModifiedFiles() {
        File rootFolder = new File(path);

        if (rootFolder.exists() == false)
            throw new IllegalArgumentException("Folder " + path + " does not exist");

        if (rootFolder.canRead() == false)
            throw new IllegalArgumentException("Can't parse " + path);

        ArrayList<OrgFile> modifiedOrgFiles = new ArrayList<OrgFile>();
        getModifiedFiles(rootFolder, null, modifiedOrgFiles);
        return modifiedOrgFiles;
    }

    private void getModifiedFiles(File parentFile, OrgNode parent, ArrayList<OrgFile> modifiedOrgFiles) {
        for (File file : parentFile.listFiles()) {
            if (file.isDirectory() && file.isHidden() == false && hasOrgFiles(file)) {
                OrgNode directoryNode = findOrCreateDirectoryNode(file, parent);
                getModifiedFiles(file, directoryNode, modifiedOrgFiles);
            } else if (file.isFile() && file.getName().endsWith(".org")) {
                OrgFile orgFile = getOrCreateOrgFile(file, parent);
                if (orgFile != null)
                    modifiedOrgFiles.add(orgFile);
            }
        }
    }


    private boolean hasOrgFiles(File file) {
        if (file.listFiles() == null)
            return false;

        for(File subFile: file.listFiles()) {
            if (file.isDirectory()) {
                if (hasOrgFiles(subFile))
                    return true;
            }
        }

        File[] files = file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".org");
            }
        });

        return files.length > 0;
    }

    private OrgNode findOrCreateDirectoryNode(File file, OrgNode parent) {
        try {

            Where<OrgNode,Integer> query = nodeDao.queryBuilder().where();
            if (parent != null)
                query.eq(OrgNode.PARENT_FIELD_NAME, parent).and();
            else
                query.isNull(OrgNode.PARENT_FIELD_NAME).and();
            query.eq(OrgNode.TITLE_FIELD_NAME, file.getName());
            OrgNode node = query.queryForFirst();
            if (node != null)
                return node;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        OrgNode node = new OrgNode();
        node.type = OrgNode.Type.Directory;
        node.parent = parent;
        node.title = file.getName();
        node.level = 0;
        nodeDao.create(node);
        return node;
    }

    private OrgFile getOrCreateOrgFile(File file, OrgNode parent) {
        OrgFile orgFile = fileDao.queryForId(file.getAbsolutePath());

        if (orgFile == null) {
            orgFile = new OrgFile();
            orgFile.path = file.getAbsolutePath();
        } else {
            if (file.lastModified() <= orgFile.lastModified)
                return null;

            try {
                nodeDao.delete(orgFile);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        orgFile.node = getRootNode(file, orgFile, parent);
        return orgFile;
    }

    private OrgNode getRootNode(File file, OrgFile orgFile, OrgNode parent) {
        OrgNode rootNode = new OrgNode();
        rootNode.type = OrgNode.Type.Headline;
        rootNode.title = file.getName();
        rootNode.file = orgFile;
        rootNode.parent = parent;
        rootNode.level = 0;
        return rootNode;
    }
}
