package com.domkoo.mygank.widget;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.domkoo.mygank.R;
import com.domkoo.mygank.WebViewActivity;
import com.domkoo.mygank.base.StuffBaseFragment;

public class CollectionFragment extends StuffBaseFragment {
    private static final String TAG = "CollectionFragment";
    private static final String TYPE = "col_type";

    public static CollectionFragment newInstance(String type) {
        Bundle args = new Bundle();
        args.putString(TYPE, type);

        CollectionFragment fragment = new CollectionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void initData() {
        super.initData();
        mType = getArguments().getString(TYPE);
    }

    @Override
    protected void loadingMore() {
        return;
    }

    @Override
    protected void fetchLatest() {
        setRefreshLayout(false);
        updateData();
    }

    @Override
    protected RecyclerView.Adapter initAdapter() {
        final CollectionAdapter adapter = new CollectionAdapter(getActivity(), mRealm, mType);
        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public boolean onItemLongClick(View v, int position) {
                if (mIsLoadingMore || mIsRefreshing)
                    return true;

                getActivity().startActionMode(new StuffFragment.ShareListener(getActivity(), adapter.getStuffAt(position), v));
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
}
