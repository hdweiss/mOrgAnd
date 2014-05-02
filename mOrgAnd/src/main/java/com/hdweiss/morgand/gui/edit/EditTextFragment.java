package com.hdweiss.morgand.gui.edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.hdweiss.morgand.R;

public class EditTextFragment extends EditBaseFragment {

    private String text;
    private EditText editText;

    public EditTextFragment() {}

    public EditTextFragment(String text) {
        this.text = text;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_text_fragment, container, false);

        editText = (EditText) view.findViewById(R.id.editText);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (text != null)
            editText.setText(text);

        getDialog().setTitle(R.string.action_edit);
    }
}
