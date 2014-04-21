package com.hdweiss.morgand.orgdata;

import com.hdweiss.morgand.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OrgFileWriter {

    private final OrgFile orgFile;
    private ArrayList<String> fileContent;

    public OrgFileWriter(OrgFile orgFile) throws IOException {
        this.orgFile = orgFile;
        fileContent = FileUtils.fileToArrayList(orgFile.path);
    }

    /**
     * Constructor for unit testing.
     */
    public OrgFileWriter(OrgFile orgFile, ArrayList<String> fileContent) {
        this.orgFile = orgFile;
        this.fileContent = fileContent;
    }


    public void write() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(orgFile.path, false));
        for(String line: fileContent)
            writer.write(line);
        writer.close();
    }


    public void add(OrgNode node) {
        addRange(node.parent.lineNumber, node.toStringRecursively());
    }

    public void delete(OrgNode node) {
        if (node.lineNumber < 0)
            return;
        int nextNodeLineNumber = node.getNextNodeLineNumber();
        removeRange(node.lineNumber, nextNodeLineNumber - 1);
    }

    public void overwrite(OrgNode node) {
        delete(node);
        add(node);
    }

    private void addRange(final int from, String content) {

    }

    private void removeRange(final int from, final int to) {
        if (from < 0 || from > to)
            throw new IllegalArgumentException("Can't remove range from=" + from + " to=" + to + " fileContent.size()=" + fileContent.size());

        for (int i = from; i < to && i < fileContent.size(); i++)
            fileContent.remove(from + i);
    }

    public String toString() {
        return fileContent.toString();
    }
}
