package com.domkoo.mygank.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.domkoo.mygank.R;
import com.domkoo.mygank.ViewerActivity;
import com.domkoo.mygank.base.BaseFragment;
import com.domkoo.mygank.db.Image;
import com.domkoo.mygank.services.ImageFetchService;
import com.domkoo.mygank.utils.CommonUtil;
import com.domkoo.mygank.utils.Constants;

public class GirlsFragment extends BaseFragment {
    public static final String TAG = "GirlsFragment";
    public static final String POSTION = "viewer_position";
    private static final String TYPE = "girls_type";

    private final UpdateResultReceiver updateResultReceiver = new UpdateResultReceiver();

    public static GirlsFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(TYPE, type);

        GirlsFragment fragment = new GirlsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        mLocalBroadcastManager.registerReceiver(updateResultReceiver, new IntentFilter(ImageFetchService.ACTION_UPDATE_RESULT));
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
    }

    @Override
    protected void loadingMore() {
        if (mIsLoadingMore)
            return;

        Intent intent = new Intent(getActivity(), ImageFetchService.class);
        intent.setAction(ImageFetchService.ACTION_FETCH_MORE);
        getActivity().startService(intent);

        mIsLoadingMore = true;
        setRefreshLayout(true);
    }


    @Override
    protected void fetchLatest() {
        if (mIsRefreshing) {
            return;
        }

        Intent intent = new Intent(getActivity(), ImageFetchService.class);
        intent.setAction(ImageFetchService.ACTION_FETCH_REFRESH);
        getActivity().startService(intent);

        mIsRefreshing = true;
        setRefreshLayout(true);
    }

    @Override
    protected int getLastVisiblePos() {
        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) mLayoutManager;
        int[] lastPositions = layoutManager.findLastVisibleItemPositions(new int[layoutManager.getSpanCount()]);
        return getMaxPosition(lastPositions);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.girls_fragment;
    }

    @Override
    protected int getRefreshLayoutId() {
        return R.id.swipe_refresh_layout;
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        final GirlsAdapter adapter = new GirlsAdapter(getActivity(), mRealm);
        adapter.setOnItemClickListener(new GirlsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int pos) {
                if (mIsLoadingMore || mIsRefreshing) {
                    CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fetching_pic), Snackbar.LENGTH_LONG);
                    return;
                }

                Intent intent = new Intent(getActivity(), ViewerActivity.class);
                intent.putExtra(POSTION, pos);
//                getActivity().startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(
//                        getActivity(), view.findViewById(R.id.network_imageview),
//                        adapter.getUrlAt(pos)).toBundle());
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);

//                ActivityOptionsCompat options = ActivityOptionsCompat
//                        .makeSceneTransitionAnimation(getActivity(), view, adapter.getUrlAt(pos));
////                ActivityCompat.startActivityForResult(getActivity(), intent, 1, options.toBundle());
//
//                try {
//                    ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
//                } catch (IllegalArgumentException e) {
//                    e.printStackTrace();
//                    startActivity(intent);
//                }
            }

            @Override
            public void onItemLongClick(View view, int pos) {
                CommonUtil.makeSnackBar(mRefreshLayout, pos + getString(R.string.fragment_long_clicked), Snackbar.LENGTH_SHORT);
            }
        });
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.girls_recyclerview_id;
    }

    private int getMaxPosition(int[] positions) {
        int maxPosition = 0;
        int size = positions.length;
        for (int i = 0; i < size; i++) {
            maxPosition = Math.max(maxPosition, positions[i]);
        }
        return maxPosition;
    }

    public void onActivityReenter(final int index) {
        mRecyclerView.smoothScrollToPosition(index);
        mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().supportStartPostponedEnterTransition();
                return true;
            }
        });

    }

    public String getImageUrlAt(int i) {
        return Image.all(mRealm).get(i).getUrl();
    }

    public View getImageViewAt(int i) {
        return mLayoutManager.findViewByPosition(i);
    }

    private class UpdateResultReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int fetched = intent.getIntExtra(ImageFetchService.EXTRA_FETCHED, 0);
            final String trigger = intent.getStringExtra(ImageFetchService.EXTRA_TRIGGER);
            final Constants.NETWORK_EXCEPTION networkException = (Constants.NETWORK_EXCEPTION) intent.getSerializableExtra(ImageFetchService.EXTRA_EXCEPTION_CODE);
            Log.d(TAG, "fetched " + fetched + ", triggered by " + trigger);

            setRefreshLayout(false);

            if (networkException.getTipsResId() != 0) {
                // 显示异常提示
                CommonUtil.makeSnackBar(mRefreshLayout, getString(networkException.getTipsResId()), Snackbar.LENGTH_SHORT);
                setFetchingFlagsFalse();
                return;
            }

            if (mIsRefreshing) {
                CommonUtil.makeSnackBar(mRefreshLayout, getString(R.string.fragment_refreshed), Snackbar.LENGTH_SHORT);
                if (fetched > 0) {
                    ((GirlsAdapter) mAdapter).updateRefreshed(fetched);
                    mRecyclerView.smoothScrollToPosition(0);
                }
            }
            setFetchingFlagsFalse();
        }

    }
}
