package com.hdweiss.morgand.utils;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrgNodeUtils {

    public static final Pattern urlPattern = Pattern.compile("\\[\\[[^\\]]*\\]\\[([^\\]]*)\\]\\]");
    public static final Pattern dateMatcher = Pattern.compile("((?:SCHEDULED:|DEADLINE:)\\s?)?<([^>]+)>" + "(?:\\s*--\\s*<([^>]+)>)?");
    public static final Pattern todoPattern = Pattern.compile("\\*+\\s([A-Z]+)");

    /**
     * Removes org urls ([[url][title]]) and replaces them with just the title.
     */
    public static String ExtractUrls(StringBuilder contentBuilder) {
        Matcher matcher = urlPattern.matcher(contentBuilder);
        while (matcher.find()) {
            contentBuilder.delete(matcher.start(), matcher.end());
            contentBuilder.insert(matcher.start(), matcher.group(1));
            matcher = urlPattern.matcher(contentBuilder);
        }

        return contentBuilder.toString();
    }

    public static String combineTags(String tags, String inheritedTags, HashSet<String> excludedTags) {
        String combinedTags = "";
        if (TextUtils.isEmpty(tags) == false)
            combinedTags += tags;

        if (TextUtils.isEmpty(inheritedTags) == false)
            combinedTags += inheritedTags;

        if (excludedTags == null || TextUtils.isEmpty(combinedTags))
            return combinedTags;

        StringBuilder result = new StringBuilder();
        for (String tag: combinedTags.split(":")) {
            if (excludedTags.contains(tag) == false && TextUtils.isEmpty(tag) == false) {
                result.append(tag);
                result.append(":");
            }
        }

        if(!TextUtils.isEmpty(result))
            result.deleteCharAt(result.lastIndexOf(":"));

        return result.toString();
    }
}
