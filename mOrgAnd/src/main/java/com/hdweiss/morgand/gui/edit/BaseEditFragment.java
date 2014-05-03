package com.hdweiss.morgand.gui.edit;

import android.app.DialogFragment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public abstract class BaseEditFragment extends DialogFragment implements TextView.OnEditorActionListener  {

    protected EditController controller;

    public BaseEditFragment() {}

    public BaseEditFragment(EditController controller) {
        this.controller = controller;
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            controller.save();
            dismiss();
            return true;
        }

        return false;
    }
}
