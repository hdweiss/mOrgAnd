package com.hdweiss.morgand.gui.edit.controller;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;

public class EditController extends BaseEditController {

    public EditController(OrgNode node) {
        mode = EditMode.Edit;
        this.node = node;
    }

    @Override
    public void save(OrgNode node) {
        OrgNodeRepository.update(node);
    }
}
