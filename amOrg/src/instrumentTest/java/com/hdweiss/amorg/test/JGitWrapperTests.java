package com.hdweiss.amorg.test;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;

import com.hdweiss.amorg.synchronizer.JGitWrapper;

import java.io.File;

public class JGitWrapperTests extends AndroidTestCase {

    private final String localPath = "/sdcard/testrepo";

    private final String remotePathSSH = "git@github.com:hdweiss/test.git";
    private final String remotePath = "https://github.com/hdweiss/test.git";

    private final String testFile = "README.md";

    private JGitWrapper jGitWrapper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        jGitWrapper = new JGitWrapper(preferences);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }


    public void testGitSetup() throws Exception {
        assertTrue(new File(localPath).exists());
        assertTrue(new File(localPath + "/.git").exists());
        assertTrue(jGitWrapper.getGit().branchList().call().size() > 0);
    }

    public void testCommitAndPush() throws Exception {
        String orgContents = TestUtils.readFileAsString(localPath + "/" + testFile);
        TestUtils.writeStringAsFile(orgContents + "\nmoretest", localPath + "/" + testFile);
        jGitWrapper.commitAllChanges("Automatic commit");
        jGitWrapper.updateChanges();
    }
}
