package com.hdweiss.morgand.gui.outline;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hdweiss.morgand.orgdata.OrgNode;
import com.hdweiss.morgand.utils.OrgNodeUtils;
import com.hdweiss.morgand.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class OutlineListView extends ListView {

	private Context context;
	private Activity activity;

	private OutlineAdapter adapter;
	private OutlineActionMode actionMode;
	private ActionMode activeActionMode = null;

    private int lastPositionClicked = -1;

	public OutlineListView(Context context, AttributeSet atts) {
		super(context, atts);
		this.context = activity;
		setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		setOnItemClickListener(outlineClickListener);
		setOnItemLongClickListener(outlineLongClickListener);
		this.actionMode = new OutlineActionMode(context);
		setAdapter(new OutlineAdapter(context));
	}
	
	public void setAdapter(OutlineAdapter adapter) {
		this.adapter = adapter;
		super.setAdapter(adapter);
	}


	public void setActivity(Activity activity) {
		this.activity = activity;
		this.context = activity;
	}

	public long[] getNodeState() {
		return this.adapter.getNodeState();
	}

    public ArrayList<Integer> getLevelState() {
        return this.adapter.getLevelState();
    }

    public boolean[] getExpandedState() {
        return this.adapter.getExpandedState();
    }

	public void setState(long[] state, ArrayList<Integer> levels, boolean[] expanded) {
		this.adapter.setState(state, levels, expanded);
	}
	
	public void refresh() {
		int position = getFirstVisiblePosition();
		this.adapter.refresh();
		setSelection(position);
	}
	
	public long getCheckedNodeId() {
		if(getCheckedItemPosition() == ListView.INVALID_POSITION)
			return -1;
		else {
			int position = getCheckedItemPosition();
			return adapter.getItemId(position);
		}
	}

    public void setData(List<OrgNode> nodes) {
        adapter.clear();
        if (nodes.size() > 0)
            adapter.insertAll(nodes, 0);
    }
	
	private OnItemClickListener outlineClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			if(activeActionMode != null)
				activeActionMode.finish();
			
			OrgNode node = adapter.getItem(position);
			if(node.getDisplayChildren().size() > 0) {
                if (PreferenceUtils.outlineExpandAll()) {
                    boolean doubleClicked = lastPositionClicked == position;
                    boolean collapsed = adapter.collapseExpandExpandAll(position, doubleClicked);
                    if (collapsed) {
                        lastPositionClicked = -1;
                        return;
                    }
                }
                else
				    adapter.collapseExpand(position);
			}
			else {
				boolean viewOnClick = PreferenceManager
						.getDefaultSharedPreferences(context).getBoolean(
								"viewOnClick", false);

                if (node.type == OrgNode.Type.Checkbox) {
                    OrgNodeUtils.toggleCheckbox(node);
                    adapter.notifyDataSetInvalidated();
                    return;
                }


				if (viewOnClick)
					OutlineActionMode.runViewNodeActivity(node.Id, context);
				else
					OutlineActionMode.runEditNodeActivity(node.Id, context);
				//setParentChecked(position);
			}

            lastPositionClicked = position;
		}
	};
	
	private OnItemLongClickListener outlineLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
				long id) {
			if(activity != null) {
				actionMode.initActionMode(OutlineListView.this, position);
				activeActionMode = activity.startActionMode(actionMode);
			}
			return true;
		}
	};
	
	@SuppressWarnings("unused")
	private void setParentChecked(int position) {
		int parentPos = adapter.findParent(position);
		if(parentPos >= 0)
			setItemChecked(parentPos, true);
	}

	public void collapseCurrent() {
		int position = getCheckedItemPosition();

		if (position == ListView.INVALID_POSITION)
			return;

		if (adapter.getExpanded(position)) // Item is expanded, collapse it
			adapter.collapseExpand(position);
		else {
			if(adapter.getLevel(position) == 0) { // Top level, collapse all entries
				adapter.collapseAll();
				setItemChecked(position, false);
			} else {									// Collapse parent
				int parent = adapter.findParent(position);

				if (parent >= 0) {
					adapter.collapseExpand(parent);
					setItemChecked(parent, true);
				}
			}
		}

		ensureCheckedItemVisible();
	}
	
	public void ensureCheckedItemVisible() {
		int position = getCheckedItemPosition();
		if(position == ListView.INVALID_POSITION)
			return;
		
		if(!(getLastVisiblePosition() >= position && getFirstVisiblePosition() <= position))
			setSelection(position - 2);
	}
}
