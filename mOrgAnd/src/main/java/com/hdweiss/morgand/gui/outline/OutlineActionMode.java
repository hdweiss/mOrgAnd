package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.orgdata.OrgFile;
import com.hdweiss.morgand.orgdata.OrgHierarchy;


public class OutlineActionMode implements ActionMode.Callback {

	private Context context;

	private ListView list;
	private OutlineAdapter adapter;
	private int listPosition;
	private OrgHierarchy node;

	public OutlineActionMode(Context context) {
		super();
		this.context = context;
	}
	
	public void initActionMode(ListView list, int position, int restorePosition) {
		initActionMode(list, position);
		this.listPosition = restorePosition;
	}
	
	public void initActionMode(ListView list, int position) {
		list.setItemChecked(position, true);
		this.list = list;
		this.adapter = (OutlineAdapter) list.getAdapter();
		this.listPosition = position;
		this.node = adapter.getItem(position);
	}
	
	@Override
	public void onDestroyActionMode(ActionMode mode) {
		this.list.setItemChecked(this.listPosition, true);
	}
	
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
		
		if (this.node != null && this.node.Id >= 0 && node.isNodeEditable()) {
	        inflater.inflate(R.menu.outline_node, menu);
		}
		else if(this.node != null && this.node.type == OrgHierarchy.Type.File) {
			if(this.node.title.equals(OrgFile.AGENDA_FILE_ALIAS))
		        inflater.inflate(R.menu.outline_file_uneditable, menu);
			else
				inflater.inflate(R.menu.outline_file, menu);
		} else
	        inflater.inflate(R.menu.outline_node_uneditable, menu);
        
        return true;
	}
	
	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_edit:
			runEditNodeActivity(node.Id, context);
			break;
		case R.id.menu_delete:
			runDeleteNode();
			break;
		case R.id.menu_delete_file:
			runDeleteFileNode();
			break;
		case R.id.menu_clockin:
			runTimeClockingService();
			break;
		case R.id.menu_archive:
			runArchiveNode(false);
			break;
		case R.id.menu_view:
			runViewNodeActivity();
			break;

		case R.id.menu_capturechild:
			runCaptureActivity(node.Id, context);
			break;
			
		default:
			mode.finish();
			return false;
		}

		mode.finish();
		return true;
	}

	
	public static void runEditNodeActivity(long nodeId, Context context) {

	}
	
	public static  void runCaptureActivity(long id, Context context) {

	}
	
	private void runDeleteNode() {	

	}
	
	private void runArchiveNode(final boolean archiveToSibling) {	

	}

	private void archiveNode(boolean archiveToSibling) {		

	}
	
	private void runDeleteFileNode() {

	}
	
	private void deleteFileNode() {

	}
	
	public static void runViewNodeActivity(long nodeId, Context context) {

	}
	
	private void runViewNodeActivity() {		
	}
	
	private void runTimeClockingService() {

	}
	
	private void runRecover() {
    }
}
