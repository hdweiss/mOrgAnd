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
        BufferedWriter writer = new BufferedWriter(new FileWriter(orgFile.path));
        for(String line: fileContent)
            writer.write(line);
        writer.close();
    }

    public void add(OrgNode node) {

    }

    public void replace(OrgNode node) {
        OrgNode nextNode = node.getNextNode();
        int endLineNumber = nextNode != null ? nextNode.lineNumber : fileContent.size() - 1;
        removeRange(node.lineNumber, endLineNumber);
        fileContent.add(node.lineNumber, node.toString());
    }


    private int removeRange(final int lineNumber, final int linesToDelete) {
        if (lineNumber < 0 || fileContent.size() < lineNumber + linesToDelete)
            throw new IllegalArgumentException("removeRange called with invalid arguments: lineNumber=" + lineNumber
            + " linesToDelete=" + linesToDelete);

        for (int i = 0; i < linesToDelete; i++)
            fileContent.remove(lineNumber + i);
        return linesToDelete;
    }

    public String toString() {
        return fileContent.toString();
    }
}
