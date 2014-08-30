package com.hdweiss.morgand.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.hdweiss.morgand.Application;
import com.hdweiss.morgand.utils.FileUtils;

import java.io.File;
import java.util.HashSet;

public class PreferenceUtils {

    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(Application.getInstace());
    }

    public static void set(String key, String value) {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getThemeName() {
        return "Light";
    }

    public static HashSet<String> getExcludedTags() {
        return new HashSet<String>();
    }


    public static HashSet<String> getInactiveTodoKeywords() {
        return getHashSetFromPreferenceString("todo_inactive", "DONE", ":");
    }

    public static HashSet<String> getActiveTodoKeywords() {
        return getHashSetFromPreferenceString("todo_active", "TODO:NEXT", ":");
    }

    public static HashSet<String> getAllTodoKeywords() {
        HashSet<String> todoKeywords = getActiveTodoKeywords();
        todoKeywords.addAll(getInactiveTodoKeywords());
        return todoKeywords;
    }

    public static HashSet<String> getPriorties() {
        return getHashSetFromPreferenceString("priorities", "A:B:C", ":");
    }

    private static HashSet<String> getHashSetFromPreferenceString(final String key, final String defaultValue, final String delimiter) {
        HashSet<String> keywordHashset = new HashSet<String>();

        String activeKeywords = getPrefs().getString(key, defaultValue);
        String[] keywords = activeKeywords.split(delimiter);
        for(String keyword: keywords) {
            if (TextUtils.isEmpty(keyword) == false)
                keywordHashset.add(keyword);
        }
        return keywordHashset;
    }

    public static boolean syncCalendar() {
        return getPrefs().getBoolean("calendar_enabled", false);
    }

    public static boolean showDrawers() {
        return getPrefs().getBoolean("show_drawers", false);
    }

    public static boolean showSettings() {
        return getPrefs().getBoolean("show_settings", false);
    }

    public static boolean outlineExpandAll() {
        return getPrefs().getBoolean("outline_expandall", false);
    }

    public static String syncMode() {
        return getPrefs().getString("sync_mode", "git");
    }

    public static void setupGitToWiki() {
        SharedPreferences.Editor editor = getPrefs().edit();
        editor.remove("git_username");
        editor.remove("git_password");
        editor.remove("git_key_path");

        File externalDir = Application.getInstace().getExternalFilesDir(null);
        File file = new File(externalDir, "mOrgAnd.wiki");

        if (file.exists())
            FileUtils.deleteDirectory(file);

        editor.putString("sync_mode", "git");
        editor.putString("git_local_path", file.getAbsolutePath());
        editor.putString("git_url", "git://github.com/hdweiss/mOrgAnd.wiki");
        editor.putString("git_branch", "master");
        editor.commit();
    }
}
