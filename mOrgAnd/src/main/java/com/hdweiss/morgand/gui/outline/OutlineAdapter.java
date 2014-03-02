package com.hdweiss.morgand.gui.outline;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.hdweiss.morgand.R;
import com.hdweiss.morgand.gui.Theme.DefaultTheme;
import com.hdweiss.morgand.orgdata.OrgHierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OutlineAdapter extends ArrayAdapter<OrgHierarchy> {

	private ArrayList<Boolean> expanded = new ArrayList<Boolean>();

	private DefaultTheme theme;
	
	private boolean levelIndentation = true;
	
	public OutlineAdapter(Context context) {
		super(context, R.layout.outline_item);

		this.theme = DefaultTheme.getTheme(context);
		init();
	}

	public void init() {
		clear();
		
		for (OrgHierarchy node : new ArrayList<OrgHierarchy>())
			add(node);
		
		notifyDataSetInvalidated();
	}
	
	
	public long[] getState() {
		int count = getCount();
		long[] state = new long[count];
		
		for(int i = 0; i < count; i++)
			state[i] = getItem(i).Id;
		
		return state;
	}
	
	public void setState(long[] state) {
		clear();
		
		for(int i = 0; i < state.length; i++) {
            try {
                OrgHierarchy node = OrgHierarchy.getDao(getContext()).queryForId((int) state[i]);
                add(node);
            } catch(Exception ex) {}
		}
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

		outlineItem.setLevelFormating(levelIndentation);

        if (this.expanded.size() > position);
		    outlineItem.setup(getItem(position), this.expanded.get(position), theme);
		return outlineItem;
	}

	public void setLevelIndentation(boolean enabled) {
		this.levelIndentation = enabled;
	}
	
	@Override
	public void clear() {
		super.clear();
		this.expanded.clear();
	}

	@Override
	public void add(OrgHierarchy node) {
		super.add(node);
		this.expanded.add(false);
	}

	@Override
	public void insert(OrgHierarchy node, int index) {
		super.insert(node, index);
		this.expanded.add(index, false);
	}
	
	public void insertAll(Collection<OrgHierarchy> nodes, int position) {
        ArrayList<OrgHierarchy> orgHierarchies = new ArrayList<OrgHierarchy>(nodes);
        Collections.reverse(orgHierarchies);
		for(OrgHierarchy node: orgHierarchies)
			insert(node, position);
		notifyDataSetInvalidated();
	}

	@Override
	public void remove(OrgHierarchy node) {
		int position = getPosition(node);
		this.expanded.remove(position);
		super.remove(node);
	}

	public boolean getExpanded(int position) {
		if(position < 0 || position > this.expanded.size())
			return false;
		
		return this.expanded.get(position);
	}
	
	public void collapseExpand(int position) {
		if(position >= getCount() || position >= this.expanded.size() || position < 0)
			return;
		
		if(this.expanded.get(position))
			collapse(getItem(position), position);
		else
			expand(position);
	}
	
	public void collapse(OrgHierarchy node, int position) {
		int activePos = position + 1;
		while(activePos < this.expanded.size()) {
			if(getItem(activePos).getLevel() <= node.getLevel())
				break;
			collapse(getItem(activePos), activePos);
			remove(getItem(activePos));
		}
		this.expanded.set(position, false);
	}
	
	public void expand(int position) {
		OrgHierarchy node = getItem(position);
        new ArrayList<OrgHierarchy>(node.children);
		insertAll(node.children, position + 1);
		this.expanded.set(position, true);
	}
	
	@Override
	public long getItemId(int position) {
		return getItem(position).Id;
	}
	
	public int findParent(int position) {
		if(position >= getCount() || position < 0)
			return -1;
		
		long currentLevel = getItem(position).getLevel();
		for(int activePos = position - 1; activePos >= 0; activePos--) {
			if(getItem(activePos).getLevel() < currentLevel)
				return activePos;
		}
		
		return -1;
	}
}
