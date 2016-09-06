package com.domkoo.mygank.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.domkoo.mygank.R;
import com.domkoo.mygank.WebViewActivity;
import com.domkoo.mygank.base.StuffBaseFragment;
import com.domkoo.mygank.services.StuffFetchService;
import com.domkoo.mygank.utils.CommonUtil;
import com.domkoo.mygank.utils.Constants;

public class StuffFragment extends StuffBaseFragment {
    public static final String SERVICE_TYPE = "service_type";
    private static final String TAG = "StuffFragment";
    private static final String TYPE = "type";

    private UpdateResultReceiver updateResultReceiver;

    public static StuffFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(TYPE, type);

        StuffFragment fragment = new StuffFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + mType);
        mLocalBroadcastManager.registerReceiver(updateResultReceiver, new IntentFilter(StuffFetchService.ACTION_UPDATE_RESULT));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");

        mLocalBroadcastManager.unregisterReceiver(updateResultReceiver);
    }

    @Override
    protected void initData() {
        super.initData();
        mType = getArguments().getString(TYPE);
        updateResultReceiver = new UpdateResultReceiver();
    }

    @Override
    protected void loadingMore() {
        if (mIsLoadingMore)
            return;

        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_MORE).putExtra(SERVICE_TYPE, mType);
        getActivity().startService(intent);

        mIsLoadingMore = true;
        setRefreshLayout(true);
    }

    @Override
    protected void fetchLatest() {
        if (mIsRefreshing)
            return;

        Intent intent = new Intent(getActivity(), StuffFetchService.class);
        intent.setAction(StuffFetchService.ACTION_FETCH_REFRESH).putExtra(SERVICE_TYPE, mType);
        getActivity().startService(intent);

        mIsRefreshing = true;
        setRefreshLayout(true);
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        final StuffAdapter adapter = new StuffAdapter(getActivity(), mRealm, mType);
        adapter.setOnItemClickListener(new StuffAdapter.OnItemClickListener() {
            @Override
            public boolean onItemLongClick(View v, int position) {
                if (mIsLoadingMore || mIsRefreshing)
                    return true;

                getActivity().startActionMode(new ShareListener(getActivity(), adapter.getStuffAt(position), v));
                return true;
            }

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
        });
        return adapter;
    }

    private class UpdateResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int fetched = intent.getIntExtra(StuffFetchService.EXTRA_FETCHED, 0);
            final String trigger = intent.getStringExtra(StuffFetchService.EXTRA_TRIGGER);
            final String type = intent.getStringExtra(StuffFetchService.EXTRA_TYPE);
            final Constants.NETWORK_EXCEPTION networkException = (Constants.NETWORK_EXCEPTION) intent.getSerializableExtra(StuffFetchService.EXTRA_EXCEPTION_CODE);

            if (!type.equals(mType)) {
                return;
            }

            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);
            if (fetched == 0 && trigger.equals(StuffFetchService.ACTION_FETCH_MORE)) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_no_more), Snackbar.LENGTH_SHORT);
                mIsNoMore = true;
            }

            setRefreshLayout(false);

            if (networkException.getTipsResId() != 0) {
                // 显示异常提示
                CommonUtil.makeSnackBar(mRefreshLayout, getString(networkException.getTipsResId()), Snackbar.LENGTH_SHORT);
                setFetchingFlagsFalse();
                return;
            }

            if (mIsRefreshing) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_refreshed), Snackbar.LENGTH_SHORT);
                mRecyclerView.smoothScrollToPosition(0);
            }
            setFetchingFlagsFalse();

            if (null == mAdapter || fetched == 0)
                return;
            ((StuffAdapter) mAdapter).updateInsertedData(fetched, trigger.equals(StuffFetchService.ACTION_FETCH_MORE));
        }
    }
}
