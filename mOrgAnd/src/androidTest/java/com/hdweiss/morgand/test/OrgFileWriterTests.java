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

        String message = "Before:\n" + scenario.file.toString() +
         "\nAfter:\n" + writer.fileContent.toString();
        Assert.assertEquals(message, 2, writer.fileContent.size());
    }
}
