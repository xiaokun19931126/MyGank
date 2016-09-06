package com.domkoo.mygank.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.domkoo.mygank.R;
import com.domkoo.mygank.ViewerActivity;

import com.domkoo.mygank.TouchImageView;

public class ViewerFragment extends Fragment implements RequestListener<String, GlideDrawable> {
    public static final String TAG = "ViewerFragment";
    public static final String INITIAL_SHOWN = "initial_shown";
    public static final String URL = "url";
    private TouchImageView touchImageView;
    private String mUrl;
    private boolean mInitialShown;

    public static Fragment newInstance(String url, boolean initialShown) {
        Bundle args = new Bundle();
        args.putSerializable(URL, url);
        args.putBoolean(INITIAL_SHOWN, initialShown);

        ViewerFragment fragment = new ViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments().getString(URL);
        mInitialShown = getArguments().getBoolean(INITIAL_SHOWN, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewer_pager_item, container, false);
        touchImageView = (TouchImageView) view.findViewById(R.id.picture);
        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        touchImageView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                ((ViewerActivity) getActivity()).toggleToolbar();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                ((ViewerActivity) getActivity()).hideToolbar();
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ViewCompat.setTransitionName(touchImageView, mUrl);
    }

    @Override
    public void onResume() {
        super.onResume();
        Glide.with(this)
                .load(mUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade(0)
                .listener(this)
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        Log.e(TAG, "onException: ", e);
        maybeStartPostponedEnterTransition();
        return true;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        touchImageView.setImageDrawable(resource);
        maybeStartPostponedEnterTransition();
        return true;
    }

    private void maybeStartPostponedEnterTransition() {
        if (mInitialShown) {
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    public View getSharedElement() {
        return touchImageView;
    }
}
