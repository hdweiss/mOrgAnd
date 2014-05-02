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
import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;
import com.hdweiss.morgand.utils.Utils;

import java.util.HashSet;

public class EditHeadingFragment extends EditBaseFragment {

    private OrgNode node;

    private AutoCompleteTextView headingView;
    private TextView inheritedTagsView;
    private AutoCompleteTextView tagsView;

    // Android requires empty constructor
    public EditHeadingFragment() {}

    public EditHeadingFragment(OrgNode parent) {
        this.node = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_heading_fragment, container, false);

        getDialog().setTitle(R.string.action_capture);

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

        if (node != null)
            populateEditView(node);
        populateAutocompletion();
    }

    private void populateEditView(OrgNode node) {
        populateView(node.getTitle(), node.tags, node.inheritedTags);
    }

    private void populateAddView(OrgNode parent) {
        String inheritedTags = OrgNodeUtils.combineTags(parent.tags, parent.inheritedTags, PreferenceUtils.getExcludedTags());
        populateView("", "", inheritedTags);
    }

    private void populateView(String heading, String tags, String inheritedTags) {
        headingView.setText(heading);

        if (TextUtils.isEmpty(inheritedTags))
            inheritedTagsView.setVisibility(View.GONE);
        else {
            inheritedTagsView.setVisibility(View.VISIBLE);
            inheritedTagsView.setText(inheritedTags);
        }

        tagsView.setText(tags);
    }

    private void populateAutocompletion() {
        HashSet<String> todoKeywords = PreferenceUtils.getAllTodoKeywords();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Utils.toList(todoKeywords));
        headingView.setAdapter(adapter);
    }
}
