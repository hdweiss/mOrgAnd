package com.hdweiss.morgand.orgdata;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class OrgFileWriter {

    private final OrgFile orgFile;

    private ArrayList<String> fileContent = new ArrayList<String>();

    public OrgFileWriter(OrgFile orgFile, ArrayList<String> fileContent) {
        this.orgFile = orgFile;
        this.fileContent = fileContent;
    }

    public OrgFileWriter(OrgFile orgFile) {
        this.orgFile = orgFile;
        read();
    }

    private void read() {
        String fileName = orgFile.path;
        try {
            String line;
            BufferedReader in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null)
                fileContent.add(line);
        } catch (IOException e) {
            Log.e("OrgFileWriter", "Failed to read file", e);
        }
    }

    public void write() {
        String fileName = orgFile.path;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(fileContent.toString()); // TODO This might be slow
            writer.close();
        } catch (IOException e) {
            Log.e("OrgFileWriter", "Failed to write file", e);
        }
    }


    public void replaceHeading(int linenumber, OrgNode node) {
        if (OrgFileParser.numberOfStars(fileContent.get(linenumber)) <= 0)
            throw new IllegalArgumentException("Heading not found on line number");
        fileContent.remove(linenumber);
        fileContent.add(linenumber, node.toString());
    }

    public void replacePayload(int linenumber, OrgNode node) {
        if (OrgFileParser.numberOfStars(fileContent.get(linenumber)) > 0)
            throw new IllegalArgumentException("Heading found on line number instead of payload");

        removePayload(linenumber);
        fileContent.add(linenumber, node.toStringRecursively());
    }

    private int removePayload(final int linenumber) {
        int nextHeadingIndex = linenumber + 1;
        while (true) {
            if (fileContent.size() <= nextHeadingIndex)
                break;

            int numberOfStars = OrgFileParser.numberOfStars(fileContent.get(nextHeadingIndex));
            if (numberOfStars > 0)
                break;

            nextHeadingIndex++;
        }

        int linesToDelete = nextHeadingIndex - linenumber;
        for (int i = 0; i < linesToDelete; i++)
            fileContent.remove(linenumber + i);
        return linesToDelete;
    }

    public String toString() {
        return fileContent.toString();
    }
}
