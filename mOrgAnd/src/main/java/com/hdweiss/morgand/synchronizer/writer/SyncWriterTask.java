package com.hdweiss.morgand.synchronizer.writer;

import android.content.Context;
import android.util.Log;

import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.synchronizer.git.SyncGitTask;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;

import java.util.List;

public class SyncWriterTask extends SafeAsyncTask<OrgFile, SyncEvent, Void> {

    public SyncWriterTask(Context context) {
        super(context, ReportMode.Log);
    }

    @Override
    protected Void safeDoInBackground(OrgFile... files) throws Exception {
        Log.d("Writer", "Started synchronization");

        if (files.length > 0) {
            for (OrgFile file : files)
                writeChanges(file);
        } else {
            for(OrgFile file : OrgFile.getAllFiles())
                writeChanges(file);
        }

        Log.d("Writer", "Ended synchronization");
        return null;
    }

    private void writeChanges(OrgFile file) throws Exception {
        OrgFileWriter writer = new OrgFileWriter(file);

        List<OrgNode> dirtyNodes = OrgNodeRepository.getDirtyNodes(file);

        if (dirtyNodes.isEmpty())
            return;

        Log.d("Writer", "Writing changes to " + file.path);

        for(OrgNode node: dirtyNodes)
            applyChanges(writer, node);

        writer.write();

        for(OrgNode node: dirtyNodes) {
            node.state = OrgNode.State.Clean;
            OrgNodeRepository.update(node);
        }
    }

    private void applyChanges(OrgFileWriter writer, OrgNode node) {
        if (node.isNodeWritable() == false)
            return;

        switch (node.state) {
            case Added:
                writer.add(node);
                break;

            case Deleted:
                writer.delete(node);
                break;

            case Updated:
                writer.overwrite(node);
                break;

            default:
                break;
        }
    }

    @Override
    protected void onSuccess(Void aVoid) {
        new SyncGitTask(context).execute();
    }

    @Override
    protected void onError() {
        SynchronizerNotification notification = new SynchronizerNotification(context);
        notification.errorNotification(exception.getMessage() + "\n" + Utils.ExceptionTraceToString(exception));
    }
}
