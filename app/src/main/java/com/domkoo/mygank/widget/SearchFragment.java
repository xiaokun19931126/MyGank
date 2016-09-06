package com.domkoo.mygank.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.domkoo.mygank.R;
import com.domkoo.mygank.WebViewActivity;
import com.domkoo.mygank.base.BaseFragment;
import com.domkoo.mygank.db.SearchBean;
import com.domkoo.mygank.services.SearchFetchService;
import com.domkoo.mygank.utils.CommonUtil;
import com.domkoo.mygank.utils.Constants;

import java.util.ArrayList;

public class SearchFragment extends BaseFragment {
    public static final String COUNT = "count";
    public static final String KEYWORD = "keyword";
    public static final String CATEGORY = "category";
    public static final String PAGE = "page";
    private static final String TAG = "SearchFragment";
    private String mKeyword;
    private String mCategory;
    private int mCount;
    private int mPage;
    private UpdateSearchReceiver mUpdateSearchReceiver;

    public static SearchFragment newInstance(String keyword, String category) {
        return newInstance(keyword, category, 10);
    }

    public static SearchFragment newInstance(String keyword, String category, int count) {
        Bundle args = new Bundle();
        args.putString(KEYWORD, keyword);
        args.putString(CATEGORY, category);
        args.putInt(COUNT, count);
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mUpdateSearchReceiver, new IntentFilter(SearchFetchService.ACTION_UPDATE_RESULT));
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocalBroadcastManager.unregisterReceiver(mUpdateSearchReceiver);
    }

    @Override
    protected void initData() {
        super.initData();
        mKeyword = getArguments().getString(KEYWORD);
        mCategory = getArguments().getString(CATEGORY);
        mCount = getArguments().getInt(COUNT, 10);
        mPage = 1;
        mUpdateSearchReceiver = new UpdateSearchReceiver();
    }

    @Override
    protected void loadingMore() {
        Intent intent = new Intent(getActivity(), SearchFetchService.class);
        intent.setAction(SearchFetchService.ACTION_FETCH_MORE);
        intent.putExtra(KEYWORD, mKeyword);
        intent.putExtra(CATEGORY, mCategory);
        intent.putExtra(COUNT, mCount);
        intent.putExtra(PAGE, mPage);
        getActivity().startService(intent);

        mIsLoadingMore = true;
        setRefreshLayout(true);
    }

    @Override
    protected void fetchLatest() {
        if (mIsLoadingMore || mIsRefreshing)
            return;

        Intent intent = new Intent(getActivity(), SearchFetchService.class);
        intent.setAction(SearchFetchService.ACTION_FETCH_REFRESH);
        intent.putExtra(KEYWORD, mKeyword);
        intent.putExtra(CATEGORY, mCategory);
        intent.putExtra(COUNT, mCount);
        intent.putExtra(PAGE, 1);
        getActivity().startService(intent);

        mIsRefreshing = true;
        setRefreshLayout(true);
    }

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
    protected RecyclerView.Adapter initAdapter() {
        final SearchAdapter adapter = new SearchAdapter(getActivity(), mRealm);
        adapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (mIsLoadingMore || mIsRefreshing)
                    return;
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.WEB_URL, adapter.getStuffAt(pos).getUrl());
                intent.putExtra(WebViewActivity.TITLE, adapter.getStuffAt(pos).getDesc());
                getContext().startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
//                CommonUtil.openUrl(getActivity(), adapter.getStuffAt(pos).getUrl());
            }

            @Override
            public void onItemLongClick(final View view, final int pos) {
            }
        });
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.stuff_recyclerview;
    }

    public void search(String keyword, String category, int count) {
        this.mKeyword = keyword;
        this.mCategory = category;
        this.mCount = count;
        this.mPage = 1;
        ((SearchAdapter) mAdapter).clearData();
        fetchLatest();
        mRefreshLayout.setRefreshing(true);
    }

    private class UpdateSearchReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ArrayList<SearchBean> beans = intent.getParcelableArrayListExtra(SearchFetchService.EXTRA_FETCHED);
            final String trigger = intent.getStringExtra(SearchFetchService.EXTRA_TRIGGER);
            final String type = intent.getStringExtra(SearchFetchService.EXTRA_TYPE);
            final Constants.NETWORK_EXCEPTION networkException = (Constants.NETWORK_EXCEPTION) intent.getSerializableExtra(SearchFetchService.EXTRA_EXCEPTION_CODE);

            setRefreshLayout(false);

            if (networkException.getTipsResId() != 0) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(networkException.getTipsResId()), Snackbar.LENGTH_SHORT);
                setFetchingFlagsFalse();
                return;
            }
            int fetched = beans.size();
            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);
            if (fetched == 0 && trigger.equals(SearchFetchService.ACTION_FETCH_MORE)) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_no_more), Snackbar.LENGTH_SHORT);
                mIsNoMore = true;
            }

            if (mIsRefreshing) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_refreshed), Snackbar.LENGTH_SHORT);
                mRecyclerView.smoothScrollToPosition(0);
                mPage = 2;
            } else if (mIsLoadingMore) {
                mPage++;
            }
            setFetchingFlagsFalse();

            if (null == mAdapter || fetched == 0)
                return;
            ((SearchAdapter) mAdapter).updateInsertedData(beans, trigger.equals(SearchFetchService.ACTION_FETCH_MORE));
        }
    }
}
