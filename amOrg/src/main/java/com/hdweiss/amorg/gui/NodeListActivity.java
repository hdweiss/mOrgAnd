package com.hdweiss.amorg.gui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.hdweiss.amorg.R;
import com.hdweiss.amorg.settings.SettingsActivity;


/**
 * An activity representing a list of Nodes. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NodeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NodeListFragment} and the item details
 * (if present) is a {@link NodeDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link NodeListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class NodeListActivity extends FragmentActivity
        implements NodeListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_node_list);

        if (findViewById(R.id.node_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((NodeListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.node_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
    }

    /**
     * Callback method from {@link NodeListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(NodeDetailFragment.ARG_ITEM_ID, id);
            NodeDetailFragment fragment = new NodeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.node_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, NodeDetailActivity.class);
            detailIntent.putExtra(NodeDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nodelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_settings:
                runShowSettings(null);
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    public void runShowSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}
