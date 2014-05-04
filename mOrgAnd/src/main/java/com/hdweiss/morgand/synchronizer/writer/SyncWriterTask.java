package com.hdweiss.morgand.synchronizer.writer;

import android.content.Context;

import com.hdweiss.morgand.data.dao.OrgFile;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.events.SyncEvent;
import com.hdweiss.morgand.gui.SynchronizerNotification;
import com.hdweiss.morgand.synchronizer.git.SyncGitTask;
import com.hdweiss.morgand.utils.SafeAsyncTask;
import com.hdweiss.morgand.utils.Utils;
import com.j256.ormlite.stmt.Where;

import java.util.List;

public class SyncWriterTask extends SafeAsyncTask<OrgFile, SyncEvent, Void> {

    public SyncWriterTask(Context context) {
        super(context, ReportMode.Log);
    }

    @Override
    protected Void safeDoInBackground(OrgFile... files) throws Exception {

        if (files.length > 0) {
            for (OrgFile file : files)
                writeChanges(file);
        } else {
            for(OrgFile file : OrgFile.getAllFiles())
                writeChanges(file);
        }

        return null;
    }

    private void writeChanges(OrgFile file) throws Exception {
        OrgFileWriter writer = new OrgFileWriter(file);

        Where<OrgNode, Integer> builder = OrgNodeRepository.queryBuilder().orderBy(OrgNode.LINENUMBER_FIELD_NAME, false).where();
        builder.eq(OrgNode.FILE_FIELD_NAME, file);
        builder.and().ne(OrgNode.STATE_FIELD_NAME, OrgNode.State.Clean);

        List<OrgNode> dirtyNodes = builder.query();

        if (dirtyNodes.isEmpty())
            return;

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
