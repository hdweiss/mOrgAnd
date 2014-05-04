package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.data.dao.OrgNode;
import com.hdweiss.morgand.data.dao.OrgNodeRepository;
import com.hdweiss.morgand.gui.theme.DefaultTheme;
import com.hdweiss.morgand.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OutlineAdapter extends ArrayAdapter<OrgNode> {

	private ArrayList<Boolean> expanded = new ArrayList<Boolean>();
    private ArrayList<Integer> level = new ArrayList<Integer>();

	private DefaultTheme theme;

	private boolean agendaMode = false;

	public OutlineAdapter(Context context) {
		super(context, R.layout.outline_item);

		this.theme = DefaultTheme.getTheme(context);
		init();
	}

	public void init() {
		clear();
		
		for (OrgNode node : new ArrayList<OrgNode>())
			add(node);
		
		notifyDataSetInvalidated();
	}
	
	
	public long[] getNodeState() {
		int count = getCount();
		long[] state = new long[count];
		
		for(int i = 0; i < count; i++)
			state[i] = getItem(i).Id;
		
		return state;
	}

    public ArrayList<Integer> getLevelState() {
        return this.level;
    }

    public boolean[] getExpandedState() {
        return Utils.toPrimitiveArray(this.expanded);
    }

	public void setState(long[] state, ArrayList<Integer> levels, boolean[] expanded) {
		clear();
		
		for(int i = 0; i < state.length; i++) {
            try {
                OrgNode node = OrgNodeRepository.queryForId((int) state[i]);
                add(node);
            } catch(Exception ex) {}
		}

        this.expanded.clear();
        for(boolean expand: expanded)
            this.expanded.add(expand);
        this.level = levels;
	}
	
	public void refresh() {
		ArrayList<Long> expandedNodeIds = new ArrayList<Long>();
		int size = this.expanded.size();
		for(int i = 0; i < size; i++) {
			if(this.expanded.get(i))
				expandedNodeIds.add(getItemId(i));
		}
		
		init();
		
		expandNodes(expandedNodeIds);
	}

	private void expandNodes(ArrayList<Long> nodeIds) {
		while (nodeIds.size() != 0) {
			Long nodeId = nodeIds.get(0);
			for (int nodesPosition = 0; nodesPosition < getCount(); nodesPosition++) {
				if (getItemId(nodesPosition) == nodeId) {
					expand(nodesPosition);
					break;
				}
			}
			nodeIds.remove(0);
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {				
		OutlineItem outlineItem = (OutlineItem) convertView;
		if (convertView == null)
			outlineItem = new OutlineItem(getContext());

		outlineItem.setAgendaMode(agendaMode);
        outlineItem.setup(getItem(position), getExpanded(position), getLevel(position), theme);

		return outlineItem;
	}

	public void setAgendaMode(boolean agendaMode) {
		this.agendaMode = agendaMode;
	}

	@Override
	public void clear() {
		super.clear();
		this.expanded.clear();
        this.level.clear();
	}

	@Override
	public void add(OrgNode node) {
		super.add(node);
		this.expanded.add(false);
        this.level.add(0);
	}

	@Override
	public void insert(OrgNode node, int index) {
		super.insert(node, index);
		this.expanded.add(index, false);

        int level = index > 0 ? this.level.get(index - 1) + 1 : 0;
        this.level.add(index, level);
	}
	
	public void insertAll(Collection<OrgNode> nodes, int position) {
        ArrayList<OrgNode> orgNodes = new ArrayList<OrgNode>(nodes);
        Collections.reverse(orgNodes);
		for(OrgNode node: orgNodes)
			insert(node, position);
		notifyDataSetInvalidated();
	}

	@Override
	public void remove(OrgNode node) {
		int position = getPosition(node);
		this.expanded.remove(position);
        this.level.remove(position);
		super.remove(node);
	}

	public boolean getExpanded(int position) {
		if(position < 0 || position > this.expanded.size())
			return false;
		
		return this.expanded.get(position);
	}

    public int getLevel(int position) {
        if (position < 0 || position > this.level.size())
            return 0;

        return this.level.get(position);
    }

	public void collapseExpand(int position) {
		if(position >= getCount() || position >= this.expanded.size() || position < 0)
			return;
		
		if(this.expanded.get(position))
			collapse(position);
		else
			expand(position);
	}

    /**
     * @param doubleClicked Whether user clicked node the second time
     * @return true expandAll was called
     */
    public boolean collapseExpandExpandAll(int position, boolean doubleClicked) {
        if(position >= getCount() || position >= this.expanded.size() || position < 0)
            return false;

        if(this.expanded.get(position)) {
            if (doubleClicked) {
                expandAll(position);
                return true;
            } else {
                collapse(position);
                return false;
            }
        }
        else {
            expand(position);
            return false;
        }
    }
	
	public void collapse(int position) {
		int activePos = position + 1;
		while(activePos < this.expanded.size()) {
			if(getLevel(activePos) <= getLevel(position))
				break;
			collapse(activePos);
			remove(getItem(activePos));
		}
		this.expanded.set(position, false);
	}

    public void collapseAll() {
        for(int activePos = 0; activePos < expanded.size(); activePos++) {
            if (expanded.get(activePos))
                collapse(activePos);
        }
    }


	public ArrayList<OrgNode> expand(int position) {
		OrgNode node = getItem(position);
        ArrayList<OrgNode> children = node.getDisplayChildren();
        if (node.type == OrgNode.Type.Directory)
            Collections.sort(children, new OrgNode.OrgNodeCompare());

		insertAll(children, position + 1);
		this.expanded.set(position, true);
        return children;
	}

    public void expandAll(int position) {
        collapse(position); // TODO Hack

        ArrayList<OrgNode> expandedChildren = expand(position);

        for(OrgNode node: expandedChildren) {
            int nodePosition = getPosition(node);
            expandAll(nodePosition);
        }
    }

	@Override
	public long getItemId(int position) {
		return getItem(position).Id;
	}
	
	public int findParent(int position) {
		if(position >= getCount() || position < 0)
			return -1;
		
		for(int activePos = position - 1; activePos >= 0; activePos--) {
			if(getLevel(activePos) < getLevel(position))
				return activePos;
		}
		
		return -1;
	}
}
