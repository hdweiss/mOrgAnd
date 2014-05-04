package com.hdweiss.morgand.gui.edit;

import android.app.DialogFragment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.gui.edit.controller.BaseEditController;
import com.hdweiss.morgand.gui.edit.controller.EditController;

public abstract class BaseEditFragment extends DialogFragment implements TextView.OnEditorActionListener  {

    public static BaseEditFragment getEditFragment(OrgNode node) {
        BaseEditController editController = new EditController(node);

        BaseEditFragment fragment;
        switch (node.type) {
            case Headline:
                fragment = new EditHeadingFragment(editController);
                break;

            case Date:
                fragment = new EditDateFragment(editController);
                break;

            default:
                fragment = new EditTextFragment(editController);
                break;
        }

        return fragment;
    }

    protected BaseEditController controller;

    public BaseEditFragment() {}

    public BaseEditFragment(BaseEditController controller) {
        this.controller = controller;
    }


    public abstract OrgNode getEditedNode();

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            if (controller != null) {
                OrgNode editedNode = getEditedNode();
                controller.save(editedNode);
            }
            dismiss();
            return true;
        }

        return false;
    }
}
