package com.hdweiss.morgand.gui.edit.controller;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;

public class AddController extends BaseEditController {

    public AddController(OrgNode parent, OrgNode.Type type) {
        mode = EditController.EditMode.Add;
        this.node = parent.addChild(type);
    }

    @Override
    public void save(OrgNode node) {
        node.state = OrgNode.State.Added;
        OrgNodeRepository.getDao().create(node);
    }
}
