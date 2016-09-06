package com.domkoo.mygank.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.domkoo.mygank.R;
import com.domkoo.mygank.base.StuffBaseAdapter;
import com.domkoo.mygank.db.Stuff;

import java.util.Date;

import io.realm.Realm;

public class StuffAdapter extends StuffBaseAdapter {
    private static final String TAG = "StuffAdapter";

    public StuffAdapter(Context context, Realm realm, String type) {
        super(context, realm, type);
    }

    public void updateInsertedData(int numImages, boolean isMore) {
        if (isMore)
            notifyItemRangeInserted(lastStuffsNum, numImages);
        else
            notifyItemRangeInserted(0, numImages);
        lastStuffsNum += numImages;
    }

    @Override
    protected void initStuffs(Realm realm, String mType) {
        mStuffs = Stuff.all(realm, mType);
    }

    @Override
    protected void bindColBtn(ImageButton likeBtn, final int position) {
        likeBtn.setTag(position);
        likeBtn.setImageResource(mStuffs.get(position).isLiked() ? R.drawable.like : R.drawable.unlike);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeBtn((ImageButton) v, position);
            }
        });
    }

    private void toggleLikeBtn(ImageButton likeBtn, int pos) {
        if (mStuffs.get(pos).isLiked()) {
            likeBtn.setImageResource(R.drawable.unlike);
            changeLiked(pos, false);
        } else {
            likeBtn.setImageResource(R.drawable.like);
            changeLiked(pos, true);
        }
    }

    private void changeLiked(final int pos, final boolean isLiked) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Stuff stuff = mStuffs.get(pos);
                stuff.setLiked(isLiked);
                stuff.setLastChanged(new Date());
            }
        });
        notifyItemChanged(pos);
    }
}
