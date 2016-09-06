package com.domkoo.mygank.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.realm.Realm;

public abstract class BaseFragment extends Fragment {
    private static final String TAG = "BaseFragment";
    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mRefreshLayout;
    protected LocalBroadcastManager mLocalBroadcastManager;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RecyclerView.Adapter mAdapter;
    protected boolean mIsLoadingMore;
    protected boolean mIsRefreshing;
    protected Realm mRealm;
    protected String mType;
    protected boolean mIsNoMore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
    }

    protected void initData() {
        mRealm = Realm.getDefaultInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);

        mRefreshLayout = $(view, getRefreshLayoutId());
        mRecyclerView = $(view, getRecyclerViewId());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mLayoutManager = getLayoutManager();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = initAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!mIsLoadingMore && dy > 0) {
                    int lastVisiblePos = getLastVisiblePos();
                    if (!mIsNoMore && lastVisiblePos + 1 == mAdapter.getItemCount()) {
                        loadingMore();
                    }
                }
            }
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        SwipeRefreshLayout.OnRefreshListener listener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchLatest();
            }
        };
        mRefreshLayout.setOnRefreshListener(listener);
        if (savedInstanceState == null)
            listener.onRefresh();

        // another way to call onRefresh
//        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//                mRefreshLayout.setRefreshing(true);
//            }
//        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
    }

    public void setRefreshLayout(final boolean state) {
        if (mRefreshLayout == null)
            return;

        mRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(state);
            }
        });
    }

    public void setFetchingFlagsFalse() {
        if (mIsRefreshing)
            mIsRefreshing = false;
        if (mIsLoadingMore)
            mIsLoadingMore = false;
    }

    public void smoothScrollToTop() {
        if (mLayoutManager != null)
            mLayoutManager.smoothScrollToPosition(mRecyclerView, null, 0);
    }

    public void updateData() {
        if (null == mAdapter)
            return;

        mAdapter.notifyDataSetChanged();
    }

    public boolean isFetching() {
        return mIsLoadingMore || mIsRefreshing;
    }

    protected <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    protected abstract void loadingMore();

    protected abstract void fetchLatest();

    protected abstract int getLastVisiblePos();

    protected abstract int getLayoutResId();

    protected abstract int getRefreshLayoutId();

    protected abstract RecyclerView.Adapter initAdapter();

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract int getRecyclerViewId();
}
