package com.hdweiss.morgand.utils;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.regex.Pattern;

public class OrgNodeUtils {

    public static final Pattern urlPattern = Pattern.compile("(?:\\[\\[([^\\]]+)\\](?:\\[([^\\]]+)\\])?\\])|(http(?:s?)://\\S+)"); // Match [[url]], [[url][alias]] and http(s)://url
    public static final Pattern dateMatcher = Pattern.compile("((?:SCHEDULED:|DEADLINE:)\\s?)?<([^>]+)>" + "(?:\\s*--\\s*<([^>]+)>)?");
    public static final Pattern todoPattern = Pattern.compile("\\*+\\s([A-Z]+)");
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
}
