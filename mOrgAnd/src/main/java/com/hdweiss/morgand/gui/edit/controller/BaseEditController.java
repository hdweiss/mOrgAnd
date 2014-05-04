package com.hdweiss.morgand.gui.edit.controller;

import com.hdweiss.morgand.data.dao.OrgNode;

public abstract class BaseEditController {
    public enum EditMode {Add, Edit}

    protected EditMode mode;
    protected OrgNode node;

    public abstract void save(OrgNode node);

    public EditMode getMode() {
        return this.mode;
    }

    public OrgNode getNode() {
        return this.node;
    }
}
