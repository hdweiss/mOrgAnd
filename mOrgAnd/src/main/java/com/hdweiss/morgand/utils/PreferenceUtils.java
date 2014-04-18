package com.hdweiss.morgand.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.hdweiss.morgand.Application;

import java.util.HashSet;

public class PreferenceUtils {

    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(Application.getInstace());
    }

    public static String getThemeName() {
        return "Light";
    }

    public static HashSet<String> getExcludedTags() {
        return new HashSet<String>();
    }


    public static HashSet<String> getInactiveTodoKeywords() {
        return getHashSetFromPreferenceString("todo_inactive", "DONE",":");
    }

    public static HashSet<String> getActiveTodoKeywords() {
        return getHashSetFromPreferenceString("todo_active", "TODO:NEXT",":");
    }

    private static HashSet<String> getHashSetFromPreferenceString(final String key, final String defaultValue, final String delimiter) {
        HashSet<String> keywordHashset = new HashSet<String>();

        String activeKeywords = getPrefs().getString("", defaultValue);
        String[] keywords = activeKeywords.split(":");
        for(String keyword: keywords) {
            if (TextUtils.isEmpty(keyword) == false)
                keywordHashset.add(keyword);
        }
        return keywordHashset;
    }
}
