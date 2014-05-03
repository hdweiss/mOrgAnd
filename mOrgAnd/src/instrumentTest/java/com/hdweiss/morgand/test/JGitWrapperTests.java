package com.hdweiss.morgand.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.hdweiss.morgand.synchronizer.git.JGitWrapper;

import java.io.File;

public class JGitWrapperTests extends AndroidTestCase {

    private String localPath;

    private final String testFile = "README.md";

    private JGitWrapper jGitWrapper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        this.localPath = preferences.getString("git_local_path", "");
        jGitWrapper = new JGitWrapper(preferences);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        jGitWrapper.getGit().close();
    }


    public void testGitSetup() throws Exception {
        assertTrue(new File(localPath).exists());
        assertTrue(new File(localPath + "/.git").exists());
        assertTrue(jGitWrapper.getGit().branchList().call().size() > 0);
    }

    public void testCommitAndPush() throws Exception {
        String orgContents = TestUtils.readFileAsString(localPath + "/" + testFile);
        TestUtils.writeStringAsFile(orgContents + "\nmorgand", localPath + "/" + testFile);
        jGitWrapper.commitAllChanges("Automatic commit");
        jGitWrapper.updateChanges();
    }
}
