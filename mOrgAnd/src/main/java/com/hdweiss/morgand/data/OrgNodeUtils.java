package com.hdweiss.morgand.data;

import android.text.TextUtils;

import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgNodeUtils {

    public static final Pattern urlPattern = Pattern.compile("(?:\\[\\[([^\\]]+)\\](?:\\[([^\\]]+)\\])?\\])|(http(?:s?)://\\S+)"); // Match [[url]], [[url][alias]] and http(s)://url
    public static final Pattern dateMatcher = Pattern.compile("((?:SCHEDULED:|DEADLINE:)\\s?)?<([^>]+)>" + "(?:\\s*--\\s*<([^>]+)>)?");
    public static final Pattern headingPattern = Pattern.compile("([A-Z]+)(:?\\s(.+))?");
    public static final Pattern prioritiesPattern = Pattern.compile("\\[#([^\\]]*)\\]");

    public static String combineTags(String tags, String inheritedTags, HashSet<String> excludedTags) {
        String combinedTags = "";
        if (TextUtils.isEmpty(tags) == false)
            combinedTags += tags;

        if (TextUtils.isEmpty(inheritedTags) == false)
            combinedTags += inheritedTags;

        if (excludedTags == null || TextUtils.isEmpty(combinedTags))
            return combinedTags;

        StringBuilder result = new StringBuilder();
        for (String tag : combinedTags.split(":")) {
            if (excludedTags.contains(tag) == false && TextUtils.isEmpty(tag) == false) {
                result.append(tag);
                result.append(":");
            }
        }

        if (!TextUtils.isEmpty(result))
            result.deleteCharAt(result.lastIndexOf(":"));

        return result.toString();
    }

    public static void toggleCheckbox(OrgNode node) {
        boolean checkedOff = node.title.contains("- [ ]");
        if (checkedOff) {
            node.title = node.title.replaceFirst("-\\s\\[\\s\\]", "- [X]");
        } else
            node.title = node.title.replaceFirst("-\\s\\[X\\]", "- [ ]");

        OrgNodeRepository.update(node);

        if (node.parent == null)
            return;

        updateCheckboxCookie(node.parent, checkedOff);
    }

    private static void updateCheckboxCookie(OrgNode node, boolean increment) {
        try {
            Matcher matcher = Pattern.compile("\\[(\\d+)/(\\d+)\\]").matcher(node.title);
            if (matcher.find()) {
                int currentAmount = Integer.parseInt(matcher.group(1));
                int total = Integer.parseInt(matcher.group(2));
                currentAmount = increment ? currentAmount + 1 : currentAmount - 1;

                if (currentAmount < 0)
                    return;

                node.title = node.title.replace(matcher.group(), "[" + currentAmount + "/" + total + "]");
                OrgNodeRepository.update(node);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
