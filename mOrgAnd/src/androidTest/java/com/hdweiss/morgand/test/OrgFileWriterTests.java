package com.hdweiss.morgand.test;

import android.test.AndroidTestCase;

import com.hdweiss.morgand.synchronizer.writer.OrgFileWriter;

import junit.framework.Assert;

import java.util.ArrayList;

public class OrgFileWriterTests extends AndroidTestCase {

    private OrgFileWriter writer;
    private OrgFileTestScenario scenario;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scenario = new OrgFileTestScenario();
        ArrayList<String> fileCopy = new ArrayList<String>(scenario.file);
        writer = new OrgFileWriter(fileCopy);
    }

    public void testDeleteHeading() {
        writer.delete(scenario.heading);

        Assert.assertEquals(debugString(), 2, writer.fileContent.size());
    }


    public void testOverwriteSubheading() {
        OrgNodeStubbed subheading = scenario.subheading;
        subheading.title = "** new title";
        writer.overwrite(subheading);

        Assert.assertEquals(debugString(), scenario.file.size(), writer.fileContent.size());

        String writtenLine = writer.fileContent.get(subheading.lineNumber - 1);
        Assert.assertEquals(debugString(), subheading.title, writtenLine);
    }

    public void testOverwriteContent() {
        OrgNodeStubbed content = scenario.content;
        content.title = "test";

        writer.overwrite(content);

        Assert.assertEquals(debugString(), scenario.file.size() - 1, writer.fileContent.size());

        String writtenLine = writer.fileContent.get(content.lineNumber - 1);
        Assert.assertEquals(debugString(), content.title, writtenLine);
    }

    public void testAddSubheading() {
        OrgNodeStubbed parent = scenario.heading;

        OrgNodeStubbed child = new OrgNodeStubbed(-1);
        child.title = "** added child";
        child.parent = parent;

        writer.add(child);

        Assert.assertEquals(debugString(), scenario.file.size() + 1, writer.fileContent.size());

        int expectedIndex = scenario.heading2.lineNumber - 2;
        String writtenLine = writer.fileContent.get(expectedIndex);

        Assert.assertEquals(debugString(), child.title, writtenLine);
    }


    private String debugString() {
        String message = "Before:\n" + scenario.file.toString() +
                "\nAfter:\n" + writer.fileContent.toString();
        return message;
    }
}
