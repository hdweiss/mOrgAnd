package com.hdweiss.morgand.gui.edit;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.settings.PreferenceUtils;
import com.hdweiss.morgand.utils.Utils;

import java.util.HashSet;

public class EditHeadingFragment extends BaseEditFragment {

    private AutoCompleteTextView headingView;
    private TextView inheritedTagsView;
    private AutoCompleteTextView tagsView;

    // Android requires empty constructor
    public EditHeadingFragment() { super();}

    public EditHeadingFragment(EditController controller) {
        super(controller);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_heading_fragment, container, false);

        tagsView = (AutoCompleteTextView) view.findViewById(R.id.tags);
        inheritedTagsView = (TextView) view.findViewById(R.id.inheritedTags);

        headingView = (AutoCompleteTextView) view.findViewById(R.id.heading);
        headingView.setOnEditorActionListener(this);
        headingView.setThreshold(0);
        headingView.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        OrgNode node = controller.getEditNode();
        populateView(node.getTitle(), node.tags, node.inheritedTags);
        populateAutocompletion();

        switch (controller.getMode()) {
            case Add:
                getDialog().setTitle(R.string.action_capture);
                break;

            case Edit:
                getDialog().setTitle(R.string.action_edit);
                break;
        }
    }

    private void populateView(String heading, String tags, String inheritedTags) {
        if (heading != null) {
            headingView.setText(heading);
            headingView.setSelection(headingView.getText().length());
        }

        if (TextUtils.isEmpty(inheritedTags))
            inheritedTagsView.setVisibility(View.GONE);
        else {
            inheritedTagsView.setVisibility(View.VISIBLE);
            inheritedTagsView.setText(inheritedTags);
        }

        if (tags != null)
            tagsView.setText(tags);
    }

    private void populateAutocompletion() {
        HashSet<String> todoKeywords = PreferenceUtils.getAllTodoKeywords();
        if (todoKeywords.size() == 0)
            return;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Utils.toList(todoKeywords));
        headingView.setAdapter(adapter);
    }

    @Override
    public OrgNode getEditedNode() {
        OrgNode editNode = controller.getEditNode();
        editNode.title = headingView.getText().toString();
        editNode.tags = tagsView.getText().toString();
        return editNode;
    }
}
