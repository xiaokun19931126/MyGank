package com.domkoo.mygank.widget;

import android.content.Context;
import android.view.View;
import android.widget.ImageButton;

import com.domkoo.mygank.R;
import com.domkoo.mygank.base.StuffBaseAdapter;
import com.domkoo.mygank.db.Stuff;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CollectionAdapter extends StuffBaseAdapter {
    private static final String TAG = "CollectionAdapter";

    public CollectionAdapter(Context context, Realm realm, String type) {
        super(context, realm, type);
        mStuffs.addChangeListener(new RealmChangeListener<RealmResults<Stuff>>() {
            @Override
            public void onChange(RealmResults<Stuff> element) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void initStuffs(Realm realm, String mType) {
        mStuffs = Stuff.collections(realm);
    }

    @Override
    protected void bindColBtn(ImageButton likeBtn, final int position) {
        likeBtn.setImageResource(R.drawable.like);
        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(position);
            }
        });
    }

    private void deleteItem(final int position) {
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Stuff stuff = mStuffs.get(position);
                if (stuff.isDeleted())
                    stuff.deleteFromRealm();
                else
                    stuff.setLiked(false);
            }
        });
    }
}
