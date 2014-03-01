package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.Theme.DefaultTheme;
import com.hdweiss.morgand.orgdata.OrgHierarchy;

public class OutlineItem extends RelativeLayout implements Checkable {

    private TextView titleView;

    private boolean levelFormatting;

    public OutlineItem(Context context) {
        super(context);
        View.inflate(context, R.layout.outline_item, this);
        titleView = (TextView) findViewById(R.id.title);
    }

    @Override
    public void setChecked(boolean checked) {
        if(checked)
            setBackgroundResource(R.drawable.outline_item_selected);
        else
            setBackgroundResource(0);
    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {
    }

    public void setLevelFormating(boolean enabled) {
        this.levelFormatting = enabled;
    }

    public void setup(OrgHierarchy node, boolean expanded, DefaultTheme theme) {
        titleView.setText(node.title);
    }
}
