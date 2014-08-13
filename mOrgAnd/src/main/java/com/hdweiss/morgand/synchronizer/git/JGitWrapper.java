package com.hdweiss.morgand.synchronizer.git;

import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.hdweiss.morgand.utils.FileUtils;
import com.hdweiss.morgand.utils.Utils;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.SshSessionFactory;

import java.io.File;

public class JGitWrapper {

    private Git git;

    private final String localPath;
    private final String remotePath;

    private final String commitAuthor;
    private final String commitEmail;

    private final MergeStrategy mergeStrategy = MergeStrategy.OURS;

    // TODO Externalize strings
    public JGitWrapper(SharedPreferences preferences) throws Exception {
        localPath = preferences.getString("git_local_path", "");
        if (TextUtils.isEmpty(localPath))
            throw new IllegalArgumentException("Must specify local git path");

        remotePath = preferences.getString("git_url", "");
        if (TextUtils.isEmpty(remotePath))
            throw new IllegalArgumentException("Must specify remote git url");

        commitAuthor = preferences.getString("git_commit_author", "");
        commitEmail = preferences.getString("git_commit_email", "");

        setupJGitAuthentication(preferences);
    }

    private void setupJGitAuthentication(SharedPreferences preferences) {
        String username = preferences.getString("git_username", "");
        if (TextUtils.isEmpty(username))
            throw new IllegalArgumentException("Must specify git username");

        String password = preferences.getString("git_password", "");
        String keyLocation = preferences.getString("git_key_path", "");
        if (TextUtils.isEmpty(password) && TextUtils.isEmpty(keyLocation))
            throw new IllegalArgumentException("Must specify either git password or keyfile path");

        JGitConfigSessionFactory session = new JGitConfigSessionFactory(username, password, keyLocation);
        SshSessionFactory.setInstance(session);
    }

    private Git initGitRepo(ProgressMonitor monitor) throws Exception {
        if (new File(localPath).exists() == false)
            createNewRepo(monitor);

        FileRepository fileRepository = new FileRepository(localPath + "/.git");
        return new Git(fileRepository);
    }

    private void createNewRepo(ProgressMonitor monitor) throws GitAPIException, IllegalArgumentException {
        File localRepo = new File(localPath);
        if (localRepo.exists()) // Safety check so we don't accidentally delete directory
            throw new IllegalStateException("Directory already exists");

        try {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(remotePath)
                    .setDirectory(localRepo)
                    .setBare(false);
            if (monitor != null)
                cloneCommand.setProgressMonitor(monitor);
            cloneCommand.call();
        } catch (GitAPIException e) {
            FileUtils.deleteDirectory(localRepo);
            throw e;
        }
    }


    public Git getGit(ProgressMonitor monitor) throws Exception {
        if (this.git == null)
            this.git = initGitRepo(monitor);

        return this.git;
    }

    public void commitAllChanges(String commitMessage) throws Exception {
        Git git = getGit(null);

        Status status = git.status().call();

        if (status.getModified().isEmpty()) {
            Log.d("JGitWrapper", "No files modified, not committing");
            return;
        }

        git.add().addFilepattern(".").call();
        git.commit().setMessage(commitMessage).setAuthor(commitAuthor, commitEmail).call();
        Log.d("JGitWrapper", "Committed changes");
    }

    public void updateChanges(ProgressMonitor monitor) throws Exception {
        Git git = getGit(monitor);

        FetchCommand fetch = git.fetch();
        if (monitor != null)
            fetch.setProgressMonitor(monitor);
        fetch.call();

        SyncState state = getSyncState(git);
        Log.d("JGitWrapper", "Got sync state: " + state.name());
        Ref fetchHead = git.getRepository().getRef("FETCH_HEAD");
        switch (state) {
            case Equal:
                // Do nothing
                break;

            case Ahead:
                git.push().setRemote(remotePath).call();
                break;

            case Behind:
                MergeResult result = git.merge().include(fetchHead).setFastForward(MergeCommand.FastForwardMode.FF_ONLY).call(); // TODO Set remote refs
                if (result.getMergeStatus().isSuccessful() == false) {
                    abortMerge(git);
                }
                break;

            case Diverged:
                MergeResult mergeResult = git.merge().include(fetchHead).setStrategy(mergeStrategy).call();
                if (mergeResult.getMergeStatus().isSuccessful()) {
                    git.push().setRemote(remotePath).call();
                } else {
                    abortMerge(git);
                }
                break;
        }
    }

    private void abortMerge(Git git) throws GitAPIException {
        git.reset().setMode(ResetCommand.ResetType.HARD).call();
        throw new IllegalStateException("Merge failed");
    }

    private enum SyncState {
        Equal, Ahead, Behind, Diverged
    }

    private SyncState getSyncState(Git git) throws Exception {
        Ref fetchHead = git.getRepository().getRef("FETCH_HEAD");
        Ref head = git.getRepository().getRef("HEAD");

        if (fetchHead == null)
            throw new IllegalStateException("fetchHead not found!");

        if (head == null)
            throw new IllegalStateException("head not found!");

        Iterable<RevCommit> call = git.log().addRange(fetchHead.getObjectId(), head.getObjectId()).call();
        int originToHead = Utils.getIteratorSize(call.iterator());

        Iterable<RevCommit> call2 = git.log().addRange(head.getObjectId(), fetchHead.getObjectId()).call();
        int headToOrigin = Utils.getIteratorSize(call2.iterator());

        Log.d("JGitWrapper", "fetchHead->head: " + originToHead + " head->fetchHead: " + headToOrigin);

        if (originToHead == 0 && headToOrigin == 0)
            return SyncState.Equal;
        else if (originToHead == 0)
            return SyncState.Behind;
        else if (headToOrigin == 0)
            return SyncState.Ahead;
        else
            return SyncState.Diverged;
    }
}
