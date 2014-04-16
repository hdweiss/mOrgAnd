package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
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
    private TextView tagsView;
    private boolean agendaMode;

    private OrgNode node;
    private DefaultTheme theme;

    public OutlineItem(Context context) {
        super(context);
        View.inflate(context, R.layout.outline_item, this);
        titleView = (TextView) findViewById(R.id.title);
        tagsView = (TextView) findViewById(R.id.tags);
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

    public void setAgendaMode(boolean enabled) {
        this.agendaMode = enabled;
    }

    public void setup(OrgNode node, boolean expanded, DefaultTheme theme) {
        this.node = node;
        this.theme = theme;

        switch (node.type) {
            case Setting:
                setupSettingTitle();
                tagsView.setText("");
                break;

            case Drawer:
                setupDrawerTitle();
                tagsView.setText("");
                break;

            case File:
            case Directory:
            case Date:
            case Body:
            case Checkbox:
            default:
                setupHeadline();
                setupTags();
        }
    }

    private void setupDrawerTitle() {
        int firstNewlineIndex = node.title.indexOf('\n');
        String drawerTitle = firstNewlineIndex > 0 ? node.title.substring(0, firstNewlineIndex) : node.title;
        SpannableStringBuilder titleSpan = new SpannableStringBuilder(drawerTitle.trim());
        titleSpan.setSpan(new ForegroundColorSpan(theme.drawerForeground), 0, titleSpan.length(), 0);
        titleView.setText(titleSpan);
    }

    private void setupSettingTitle() {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder(node.title.trim());
        titleSpan.setSpan(new ForegroundColorSpan(theme.settingsForeground), 0, titleSpan.length(), 0);
        titleView.setText(titleSpan);
    }

    private void setupHeadline() {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder(node.title);

        if (agendaMode == false)
            titleView.setPadding(node.getLevel() * 5, titleView.getPaddingTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());

        setupChildrenIndicator(node, theme, titleSpan);
        titleView.setText(titleSpan);
    }

    private void setupChildrenIndicator(OrgNode node, DefaultTheme theme, SpannableStringBuilder titleSpan) {
        if (node.children.isEmpty() == false) {
            titleSpan.append("...");
            titleSpan.setSpan(new ForegroundColorSpan(theme.defaultForeground),
                    titleSpan.length() - "...".length(), titleSpan.length(), 0);
        }
    }

    private void setupTags() {
        SpannableStringBuilder tagsSpan = new SpannableStringBuilder();
        if (TextUtils.isEmpty(node.tags) == false)
            tagsSpan.append(node.tags).append("\n");

        if (agendaMode && TextUtils.isEmpty(node.inheritedTags) == false)
            tagsSpan.append(node.inheritedTags);

        if (TextUtils.isEmpty(tagsSpan) == false)
            tagsSpan.setSpan(new ForegroundColorSpan(theme.gray), 0, tagsSpan.length(), 0);
        tagsView.setText(tagsSpan);
    }

    public static final Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
}
