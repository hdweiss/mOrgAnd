package com.hdweiss.morgand.gui.edit;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;
import com.hdweiss.morgand.utils.Utils;

import java.util.HashSet;

public class EditHeadingFragment extends DialogFragment implements TextView.OnEditorActionListener {

    private OrgNode node;

    private AutoCompleteTextView heading;
    private TextView inheritedTags;
    private AutoCompleteTextView tags;

    // Android requires empty constructor
    public EditHeadingFragment() {}

    public EditHeadingFragment(OrgNode parent) {
        this.node = parent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_heading_fragment, container, false);

        getDialog().setTitle(R.string.action_capture);

        tags = (AutoCompleteTextView) view.findViewById(R.id.tags);
        inheritedTags = (TextView) view.findViewById(R.id.inheritedTags);

        heading = (AutoCompleteTextView) view.findViewById(R.id.heading);
        heading.setOnEditorActionListener(this);
        heading.setThreshold(0);
        heading.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (node != null)
            populateAddView(node);
        populateAutocompletion();
    }

    private void populateEditView(OrgNode node) {
        heading.setText(node.getTitle());

        tags.setText(node.tags);
        inheritedTags.setText(node.inheritedTags);
    }

    private void populateAddView(OrgNode parent) {
        String combinedTags = OrgNodeUtils.combineTags(parent.tags, parent.inheritedTags, PreferenceUtils.getExcludedTags());
        inheritedTags.setText(combinedTags);
    }

    private void populateAutocompletion() {
        HashSet<String> todoKeywords = PreferenceUtils.getAllTodoKeywords();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, Utils.toList(todoKeywords));
        heading.setAdapter(adapter);
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            dismiss();
            return true;
        }

        return false;
    }
}
