package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.Theme.DefaultTheme;
import com.hdweiss.morgand.orgdata.OrgNode;

import java.util.regex.Pattern;

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

    public void setup(OrgNode node, boolean expanded, DefaultTheme theme) {

        SpannableStringBuilder titleSpan = new SpannableStringBuilder(node.title);

        if (levelFormatting)
            titleSpan.insert(0, applyLevelIndentation(node.getLevel(), titleSpan));

        setupChildrenIndicator(node, theme, titleSpan);
        titleView.setText(titleSpan);
    }

    public String applyLevelIndentation(long level, SpannableStringBuilder item) {
        String indentString = "";
        for(int i = 0; i < level; i++)
            indentString += "   ";

        return indentString;
    }

    public void setupChildrenIndicator(OrgNode node, DefaultTheme theme, SpannableStringBuilder titleSpan) {
        if (node.children.isEmpty() == false) {
            titleSpan.append("...");
            titleSpan.setSpan(new ForegroundColorSpan(theme.defaultForeground),
                    titleSpan.length() - "...".length(), titleSpan.length(), 0);
        }
    }

    public static final Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
}
