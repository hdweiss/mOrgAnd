package com.hdweiss.morgand.gui.edit;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.DataUpdatedEvent;
import com.hdweiss.morgand.gui.edit.controller.AddController;
import com.hdweiss.morgand.gui.edit.controller.BaseEditController;
import com.hdweiss.morgand.gui.edit.controller.EditController;

public abstract class BaseEditFragment extends DialogFragment implements TextView.OnEditorActionListener  {

    public static BaseEditFragment getEditFragment(OrgNode node) {
        BaseEditFragment fragment;
        switch (node.type) {
            case Headline:
                fragment = new EditHeadingFragment();
                break;

            case Date:
                fragment = new EditDateFragment();
                break;

            default:
                fragment = new EditTextFragment();
                break;
        }

        setFragmentArguments(fragment, node.Id, BaseEditController.EditMode.Edit);
        return fragment;
    }

    public static BaseEditFragment getAddFragment(OrgNode node) {
        BaseEditFragment fragment = new EditHeadingFragment();
        setFragmentArguments(fragment, node.Id, BaseEditController.EditMode.Add);
        return fragment;
    }

    private static void setFragmentArguments(BaseEditFragment fragment, int nodeId, BaseEditController.EditMode mode) {
        Bundle argumentBundle = new Bundle();
        argumentBundle.putInt("nodeId", nodeId);
        argumentBundle.putString("mode", mode.name());

        fragment.setArguments(argumentBundle);
    }


    protected BaseEditController controller;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int nodeId = getArguments().getInt("nodeId", -1);
        if (nodeId == -1)
            throw new IllegalArgumentException("No nodeId given");

        OrgNode node = OrgNodeRepository.queryForId(nodeId);

        String editModeString = getArguments().getString("mode", "edit");
        BaseEditController.EditMode mode = BaseEditController.EditMode.valueOf(editModeString);

        switch (mode) {
            case Add:
                this.controller = new AddController(node, OrgNode.Type.Headline);
                break;

            case Edit:
            default:
                this.controller = new EditController(node);
                break;
        }
    }

    public abstract OrgNode getEditedNode();


    public void show(Activity activity) {
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.add(this, "dialog");
        fragmentTransaction.commit();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            if (controller != null) {
                OrgNode editedNode = getEditedNode();
                controller.save(editedNode);
                Application.getBus().post(new DataUpdatedEvent());
            }
            dismiss();
            return true;
        }

        return false;
    }
}
