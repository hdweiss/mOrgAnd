package com.hdweiss.morgand.synchronizer.writer;

import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OrgFileWriter {

    private final OrgFile orgFile;
    public ArrayList<String> fileContent;

    public OrgFileWriter(OrgFile orgFile) throws IOException {
        this.orgFile = orgFile;
        fileContent = FileUtils.fileToArrayList(orgFile.path);
    }

    /**
     * Constructor for unit testing.
     */
    public OrgFileWriter(ArrayList<String> fileContent) {
        this.orgFile = null;
        this.fileContent = fileContent;
    }


    public void write() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(orgFile.path, false));
        for(String line: fileContent)
            writer.write(line);
        writer.close();
    }


    public void add(OrgNode node) {
        if (node == null) throw new IllegalArgumentException("Got null node as argument");
        if (node.parent == null) throw new IllegalArgumentException("Got node with null parent as argument: " + node.title);
        if (node.parent.type == OrgNode.Type.Directory) throw new IllegalArgumentException("Got node with invalid parent type");

        int index = node.parent.getSiblingLineNumber() - 1;
        if (index < 0) throw new IllegalArgumentException("Got node with parent lineNumber less than 0: " + node.parent.title);

        add(index, node.toStringRecursively());
    }

    public void delete(OrgNode node) {
        if (node == null) throw new IllegalArgumentException("Got null node as argument");
        if (node.lineNumber < 0) throw new IllegalArgumentException("Node's lineNumber can't be less than 0: " + node.title);

        int startIndex = node.lineNumber - 1;
        int endIndex = node.getSiblingLineNumber() - 1;
        removeRange(startIndex, endIndex);
    }

    public void overwrite(OrgNode node) {
        int startIndex = node.lineNumber - 1;
        int endIndex = node.getNextNodeLineNumber() - 1;
        removeRange(startIndex, endIndex);
        add(startIndex, node.toString());
    }


    private void removeRange(final int from, final int to) {
        if (from < 0 || from > to) throw new IllegalArgumentException("Can't remove range from=" + from + " to=" + to + " fileContent.size()=" + fileContent.size());

        for (int linesToDelete = to - from; linesToDelete > 0 && from < fileContent.size(); linesToDelete--)
            fileContent.remove(from);
    }

    private void add(final int index, final String content) {
        if (index < 0) throw new IllegalArgumentException("Can't add contents with negative index index=" + index + ", content=" + content);

        if (index <= fileContent.size())
            fileContent.add(index, content);
        else
            fileContent.add(content);
    }

    public String toString() {
        return fileContent.toString();
    }
}
