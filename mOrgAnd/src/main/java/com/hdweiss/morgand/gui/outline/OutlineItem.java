package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.Theme.DefaultTheme;
import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;

import java.util.regex.Matcher;

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
                setupTitle(theme.drawer);
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
                SpannableStringBuilder titleSpan = setupTitle(theme.defaultForeground);
                setupUrls(titleSpan);
                titleView.setText(titleSpan);
                tagsView.setText("");
                break;
        }
    }

    private SpannableStringBuilder setupTitle(int color) {
        return setupTitle(color, node.title);
    }

    private SpannableStringBuilder setupTitle(int color, String title) {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder(title);
        titleSpan.setSpan(new ForegroundColorSpan(color), 0, titleSpan.length(), 0);
        titleView.setText(titleSpan);
        return titleSpan;
    }


    // TODO Mark up COMMENT and Archive nodes
    private void setupHeadlineTitle() {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder();

        if (agendaMode) {
            final String title = node.title.replaceFirst("^\\**", "*");
            titleSpan.append(title);
        } else {
            titleSpan.append(node.title);
            titleView.setPadding(node.getLevel() * 5, titleView.getPaddingTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());
        }

        setupUrls(titleSpan);
        setupTodoKeyword(titleSpan);
        setupPriority(titleSpan);
        setupChildIndicator(titleSpan);
        titleView.setText(titleSpan);
    }

    private void setupTodoKeyword(SpannableStringBuilder titleSpan) {
        Matcher matcher = OrgNodeUtils.todoPattern.matcher(titleSpan);
        if (matcher.find()) {
            String todoKeyword = matcher.group(1);

            if (PreferenceUtils.getActiveTodoKeywords().contains(todoKeyword))
                titleSpan.setSpan(new ForegroundColorSpan(theme.todoKeyword), matcher.start(1), matcher.end(1), 0);
            else if (PreferenceUtils.getInactiveTodoKeywords().contains(todoKeyword))
                titleSpan.setSpan(new ForegroundColorSpan(theme.inactiveTodoKeyword), matcher.start(1), matcher.end(1), 0);
        }
    }

    private void setupPriority(SpannableStringBuilder titleSpan) {
        Matcher matcher = OrgNodeUtils.prioritiesPattern.matcher(titleSpan);
        if (matcher.find()) {
            if (PreferenceUtils.getPriorties().contains(matcher.group(1)))
                titleSpan.setSpan(new ForegroundColorSpan(theme.priority), matcher.start(), matcher.end(), 0);
        }
    }

    private void setupChildIndicator(SpannableStringBuilder titleSpan) {
        if (node.getDisplayChildren().isEmpty() == false) {
            titleSpan.append("...");
            titleSpan.setSpan(new ForegroundColorSpan(theme.defaultForeground),
                    titleSpan.length() - "...".length(), titleSpan.length(), 0);
        }
    }

    private void setupUrls(SpannableStringBuilder stringBuilder) {
        Matcher matcher = OrgNodeUtils.urlPattern.matcher(stringBuilder);
        int currentIndex = 0;
        while(matcher.find(currentIndex)) {
            int beginIndex = matcher.start();
            final String url = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
            String alias = matcher.group(2) != null ? matcher.group(2) : url;

            stringBuilder.delete(matcher.start(), matcher.end());

            currentIndex = beginIndex + alias.length();
            stringBuilder.insert(beginIndex, alias);

            ClickableSpan clickable = new ClickableSpan() {
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Application.getInstace().getApplicationContext().startActivity(intent);
                }
            };

            stringBuilder.setSpan(clickable, beginIndex, currentIndex, 0);
            matcher = OrgNodeUtils.urlPattern.matcher(stringBuilder);
        }

        if (currentIndex > 0) {
            titleView.setLinksClickable(true);
            titleView.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            titleView.setLinksClickable(false);
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
