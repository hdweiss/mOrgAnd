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
                setupTitle(theme.settingsForeground);
                tagsView.setText("");
                break;

            case Drawer:
                setupDrawerTitle(expanded);
                tagsView.setText("");
                break;

            case Directory:
                setupTitle(theme.directoryForeground);
                tagsView.setText("");
                break;

            case File:
            case Headline:
                setupHeadlineTitle();
                setupTags();
                break;

            case Date:
            case Body:
            case Checkbox:
            default:
                setupTitle(theme.defaultForeground);
                tagsView.setText("");
                break;
        }
    }

    private void setupTitle(int color) {
        setupTitle(color, node.title);
    }

    private void setupTitle(int color, String title) {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder(title);
        titleSpan.setSpan(new ForegroundColorSpan(color), 0, titleSpan.length(), 0);
        titleView.setText(titleSpan);
    }

    private void setupDrawerTitle(boolean expanded) {
        int firstNewlineIndex = node.title.indexOf('\n');

        String drawerTitle;
        if (expanded)
            drawerTitle = node.title;
        else
            drawerTitle = firstNewlineIndex > 0 ? node.title.substring(0, firstNewlineIndex) : node.title;
        setupTitle(theme.drawerForeground, drawerTitle);
    }

    private void setupHeadlineTitle() {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder();
        if (agendaMode) {
            final String title = node.title.replaceFirst("^\\**", "*");
            titleSpan.append(title);
        } else {
            titleSpan.append(node.title);
            titleView.setPadding(node.getLevel() * 5, titleView.getPaddingTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }

        setupChildIndicator(titleSpan);
        titleView.setText(titleSpan);
    }

    private void setupChildIndicator(SpannableStringBuilder titleSpan) {
        if (node.getDisplayChildren().isEmpty() == false) {
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
}
