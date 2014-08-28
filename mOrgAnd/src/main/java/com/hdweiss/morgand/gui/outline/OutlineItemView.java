package com.hdweiss.morgand.gui.outline;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.OrgNodeUtils;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.gui.theme.DefaultTheme;
import com.hdweiss.morgand.settings.PreferenceUtils;

import java.util.regex.Matcher;

public class OutlineItemView extends RelativeLayout implements Checkable {

    private TextView titleView;
    private TextView tagsView;
    private boolean agendaMode;

    private OrgNode node;
    private DefaultTheme theme;

    public OutlineItemView(Context context) {
        super(context);
        View.inflate(context, R.layout.outline_item, this);
        titleView = (TextView) findViewById(R.id.title);
        titleView.setOnTouchListener(urlClickListener);

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

    public void setup(OrgNode node, boolean expanded, int level, DefaultTheme theme) {
        if (node == null)
            return;

        this.node = node;
        this.theme = theme;

        titleView.setPadding(level * 5, titleView.getPaddingTop(), titleView.getPaddingRight(), titleView.getPaddingBottom());

//        if (node.type == OrgNode.Type.Table) {
//            titleView.setTypeface(Typeface.MONOSPACE);
//            titleView.setTextSize(android.R.style.);
//        }
//        else {
//            titleView.setTextAppearance(getContext(), android.R.attr.textAppearanceMedium);
//        }

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
                setupHeadlineTitle(expanded, level);
                setupTags();
                break;

            case Date:
                setupTitle(theme.drawer);
                tagsView.setText("");
                break;

            case Body:
            case Checkbox:
            case Table:
            default:
                String formattedTitle = applyNewlineFormatting(node.title);
                SpannableStringBuilder titleSpan = setupTitle(theme.defaultForeground, formattedTitle);
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
        titleView.setMovementMethod(null);
        titleView.setFocusable(false);

        return titleSpan;
    }


    // TODO Mark up COMMENT and Archive nodes
    private void setupHeadlineTitle(boolean expanded, int level) {
        SpannableStringBuilder titleSpan = new SpannableStringBuilder();

        for(int i = 0; i < level; i++)
            titleSpan.append("*");
        titleSpan.append(" ");
        titleSpan.append(node.title);

        setupUrls(titleSpan);
        setupTodoKeyword(titleSpan);
        setupPriority(titleSpan);
        if (expanded == false)
            setupChildIndicator(titleSpan);
        titleView.setText(titleSpan);
    }

    private void setupTodoKeyword(SpannableStringBuilder titleSpan) {
        Matcher matcher = OrgNodeUtils.headingPattern.matcher(titleSpan);
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

    private String applyNewlineFormatting(String text) {
        StringBuilder builder = new StringBuilder();

        for(String line: text.trim().split("\n")) {
            if (TextUtils.isEmpty(line))
                builder.append("\n");
            else if (line.startsWith("- ") || line.startsWith("+ "))
                builder.append("\n").append(line);
            else if (line.startsWith("|") && line.endsWith("|"))
                builder.append("\n").append(line);
            else if (line.matches("^\\d+(\\.|\\)).*"))
                builder.append("\n").append(line);
            else
                builder.append(line);
        }

        return builder.toString();
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
                    try {
                        Application.getInstace().getApplicationContext().startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            };

            stringBuilder.setSpan(clickable, beginIndex, currentIndex, 0);
            matcher = OrgNodeUtils.urlPattern.matcher(stringBuilder);
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

    /**
     * Taken from: http://stackoverflow.com/a/17246463
     * Allows clickable urls in listview without making textview focusable.
     */
    private OnTouchListener urlClickListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            boolean ret = false;
            CharSequence text = ((TextView) v).getText();
            Spannable stext = Spannable.Factory.getInstance().newSpannable(text);
            TextView widget = (TextView) v;
            int action = event.getAction();

            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();

                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                x += widget.getScrollX();
                y += widget.getScrollY();

                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] link = stext.getSpans(off, off, ClickableSpan.class);

                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    }
                    ret = true;
                }
            }
            return ret;
        }
    };
}
