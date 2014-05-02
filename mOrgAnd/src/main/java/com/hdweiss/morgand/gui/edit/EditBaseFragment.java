package com.hdweiss.morgand.gui.edit;

import android.app.DialogFragment;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public abstract class EditBaseFragment extends DialogFragment implements TextView.OnEditorActionListener  {



    public EditBaseFragment() {

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
