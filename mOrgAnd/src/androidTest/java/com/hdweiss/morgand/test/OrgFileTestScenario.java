package com.hdweiss.morgand.test;

import java.util.ArrayList;

public class OrgFileTestScenario {

    public ArrayList<String> file;

    public OrgNodeStubbed heading;
    public OrgNodeStubbed subheading;
    public OrgNodeStubbed content;
    public OrgNodeStubbed subheading2;
    public OrgNodeStubbed content2;
    public OrgNodeStubbed subsubheading1;
    public OrgNodeStubbed subheading3;
    public OrgNodeStubbed heading2;
    public OrgNodeStubbed content3;

    public OrgFileTestScenario() {
        setup();
    }

    public void setup() {
        file = new ArrayList<String>();

        file.add("* Heading");
        heading = new OrgNodeStubbed(file.size());

        file.add("** sub heading 1");
        subheading = new OrgNodeStubbed(file.size());
        file.add("content 1");
        content = new OrgNodeStubbed(file.size());
        file.add("content 1");

        file.add("** sub heading 2");
        subheading2 = new OrgNodeStubbed(file.size());
        file.add("content 2");
        content2 = new OrgNodeStubbed(file.size());
        file.add("content 2");

        file.add("*** sub sub heading 1");
        subsubheading1 = new OrgNodeStubbed(file.size());

        file.add("** sub heading 3");
        subheading3 = new OrgNodeStubbed(file.size());

        file.add("* heading 2");
        heading2 = new OrgNodeStubbed(file.size());
        file.add("contents 3");
        content3 = new OrgNodeStubbed(file.size());

        heading.nextNodeLineNumber = subheading.lineNumber;
        heading.siblingLineNumber = heading2.lineNumber;

        subheading.nextNodeLineNumber = content.lineNumber;
        subheading.siblingLineNumber = subheading2.lineNumber;

        content.nextNodeLineNumber = subheading2.lineNumber;
        content.siblingLineNumber = subheading2.lineNumber;

        subheading2.nextNodeLineNumber = content2.lineNumber;
        subheading2.siblingLineNumber = subheading3.lineNumber;

        content2.nextNodeLineNumber = subsubheading1.lineNumber;
        content2.siblingLineNumber = subsubheading1.lineNumber;

        subsubheading1.nextNodeLineNumber = subheading3.lineNumber;
        subsubheading1.siblingLineNumber = subheading3.lineNumber;

        heading2.nextNodeLineNumber = content3.lineNumber;
        heading2.siblingLineNumber = Integer.MAX_VALUE;

        content3.nextNodeLineNumber = Integer.MAX_VALUE;
        content3.siblingLineNumber = Integer.MAX_VALUE;
    }
}
