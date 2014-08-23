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
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.net.URISyntaxException;

public class JGitWrapper {

    private Git git;

    private final String localPath;
    private final String remotePath;

    private final String branch;

    private final String commitAuthor;
    private final String commitEmail;

    private CredentialsProvider credentialsProvider;

    private MergeStrategy mergeStrategy;

    // TODO Externalize strings
    public JGitWrapper(SharedPreferences preferences) throws Exception {
        localPath = preferences.getString("git_local_path", "");
        if (TextUtils.isEmpty(localPath))
            throw new IllegalArgumentException("Must specify local git path");

        String url = preferences.getString("git_url", "");
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("Must specify remote git url");
        try {
            URIish urIish = new URIish(url);
            if (urIish.getUser() == null) {
                String username = preferences.getString("git_username", "");
                urIish = urIish.setUser(username);
            }
            remotePath = urIish.toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid remote git url");
        }

        branch = preferences.getString("git_branch", "master");
        if (branch.isEmpty())
            throw new IllegalArgumentException("Must specify a git branch");

        commitAuthor = preferences.getString("git_commit_author", "");
        commitEmail = preferences.getString("git_commit_email", "");

        String mergeStrategyString = preferences.getString("git_merge_strategy", "theirs");
        mergeStrategy = MergeStrategy.get(mergeStrategyString);
        if (mergeStrategy == null)
            throw new IllegalArgumentException("Invalid merge strategy: " + mergeStrategyString);

        setupJGitAuthentication(preferences);
    }

    private void setupJGitAuthentication(SharedPreferences preferences) {
        String username = preferences.getString("git_username", "");
        String password = preferences.getString("git_password", "");
        String keyLocation = preferences.getString("git_key_path", "");

        JGitConfigSessionFactory session = new JGitConfigSessionFactory(username, password, keyLocation);
        SshSessionFactory.setInstance(session);

        credentialsProvider = new JGitCredentialsProvider(username, password);
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
                    .setCredentialsProvider(credentialsProvider)
                    .setURI(remotePath)
                    .setBranch(branch)
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

        boolean hasConflicts = false;

        try {
            hasConflicts = this.git.status().call().getConflicting().isEmpty() == false;
        } catch (Exception ex) {}

        if (hasConflicts)
            throw new IllegalStateException("Unresolved conflict(s) in git repository");

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
        fetch.setCredentialsProvider(credentialsProvider);
        if (monitor != null)
            fetch.setProgressMonitor(monitor);
        fetch.call();

        SyncState state = getSyncState(git);
        Ref fetchHead = git.getRepository().getRef("FETCH_HEAD");
        switch (state) {
            case Equal:
                // Do nothing
                Log.d("Git", "Local branch is up-to-date");
                break;

            case Ahead:
                Log.d("Git", "Local branch ahead, pushing changes to remote");
                git.push().setCredentialsProvider(credentialsProvider).setRemote(remotePath).call();
                break;

            case Behind:
                Log.d("Git", "Local branch behind, fast forwarding changes");
                MergeResult result = git.merge().include(fetchHead).setFastForward(MergeCommand.FastForwardMode.FF_ONLY).call();
                if (result.getMergeStatus().isSuccessful() == false)
                    throw new IllegalStateException("Fast forward failed on behind merge");
                break;

            case Diverged:
                Log.d("Git", "Branches are diverged, merging with strategy " + mergeStrategy.getName());
                MergeResult mergeResult = git.merge().include(fetchHead).setStrategy(mergeStrategy).call();
                if (mergeResult.getMergeStatus().isSuccessful()) {
                    git.push().setCredentialsProvider(credentialsProvider).setRemote(remotePath).call();
                } else
                    throw new IllegalStateException("Merge failed for diverged branches using strategy " + mergeStrategy.getName());
                break;
        }
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

    public void cleanup() {
        if (git != null)
            git.close();
    }
}
