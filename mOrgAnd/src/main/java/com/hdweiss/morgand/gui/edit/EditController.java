package com.hdweiss.morgand.gui.edit;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;

public class EditController {

    public enum EditMode {Add, Edit}

    private EditMode mode;
    private OrgNode node;

    private EditController() {}

    public static EditController getEditNodeController(OrgNode node) {
        EditController editController = new EditController();
        editController.node = node;
        editController.mode = EditMode.Edit;
        return editController;
    }

    public static EditController getAddNodeController(OrgNode parent, OrgNode.Type type) {
        EditController editController = new EditController();
        editController.node = parent.addChild(type);
        editController.mode = EditMode.Add;
        return editController;
    }

    public OrgNode getEditNode() {
        return node;
    }

    public void save() {
        switch (mode) {
            case Add:
                node.state = OrgNode.State.Added;
                OrgNodeRepository.getDao().create(node);
                break;

            case Edit:
                node.state = OrgNode.State.Updated;
                OrgNodeRepository.getDao().update(node);
                break;
        }
    }
}
