package com.hdweiss.morgand.gui.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgNode;

public class EditTextFragment extends BaseEditFragment {

    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_text_fragment, container, false);

        editText = (EditText) view.findViewById(R.id.editText);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (controller != null) {
            String text = controller.getNode().title;
            populateView(text);
        }
    }

    private void populateView(String text) {
        if (text != null)
            editText.setText(text);

        getDialog().setTitle(R.string.action_edit);
    }

    @Override
    public OrgNode getEditedNode() {
        OrgNode editNode = controller.getNode();
        editNode.title = editText.getText().toString();
        return editNode;
    }
}
