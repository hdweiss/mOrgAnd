package com.hdweiss.morgand.test;

import com.hdweiss.morgand.data.dao.OrgNode;

public class OrgNodeStubbed extends OrgNode {

    public int nextNodeLineNumber;
    public int siblingLineNumber;

    public OrgNodeStubbed(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public OrgNodeStubbed(int lineNumber, int nextNodeLineNumber, int siblingLineNumber) {
        super();
        this.lineNumber = lineNumber;
        this.nextNodeLineNumber = nextNodeLineNumber;
        this.siblingLineNumber = siblingLineNumber;
    }

    @Override
    public int getNextNodeLineNumber() {
        return nextNodeLineNumber;
    }

    @Override
    public int getSiblingLineNumber() {
        return siblingLineNumber;
    }
}
