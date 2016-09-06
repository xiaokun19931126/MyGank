package com.domkoo.mygank.base;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;

import com.domkoo.mygank.R;
import com.domkoo.mygank.db.Stuff;

public abstract class StuffBaseFragment extends BaseFragment {
    @Override
    protected int getLastVisiblePos() {
        return ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.stuff_fragment;
    }

    @Override
    protected int getRefreshLayoutId() {
        return R.id.stuff_refresh_layout;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.stuff_recyclerview;
    }

    public class ShareListener implements AbsListView.MultiChoiceModeListener {
        private final Context context;
        private final Stuff stuff;
        private final View view;

        public ShareListener(Context context, Stuff stuff, View view) {
            this.context = context;
            this.stuff = stuff;
            this.view = view;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu, menu);
            view.setActivated(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_menu_share:
                    String textShared = stuff.getDesc() + "    " + stuff.getUrl() + " -- " + context.getString(R.string.share_msg);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_msg));
                    intent.putExtra(Intent.EXTRA_TEXT, textShared);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            view.setActivated(false);
        }
    }
}
