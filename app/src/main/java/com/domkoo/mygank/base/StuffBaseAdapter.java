package com.domkoo.mygank.base;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domkoo.mygank.R;
import com.domkoo.mygank.db.Stuff;
import com.domkoo.mygank.utils.DateUtil;

import io.realm.Realm;
import io.realm.RealmResults;

public abstract class StuffBaseAdapter extends RecyclerView.Adapter<StuffBaseAdapter.Viewholder> {
    private static final String TAG = "StuffBaseAdapter";
    protected final Context mContext;
    protected final Realm mRealm;
    protected final String mType;
    protected RealmResults<Stuff> mStuffs;
    protected int lastStuffsNum;
    private OnItemClickListener mOnItemClickListener;

    public StuffBaseAdapter(Context context, Realm realm, String type) {
        this.mContext = context;
        this.mRealm = realm;
        this.mType = type;
        initStuffs(mRealm, mType);
        lastStuffsNum = mStuffs.size();
        setHasStableIds(true);
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(mContext).inflate(R.layout.stuff_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Viewholder holder, final int position) {
        final Stuff stuff = mStuffs.get(position);
        holder.source.setText(stuff.getWho());
        holder.title.setText(stuff.getDesc());
        holder.date.setText(DateUtil.format(stuff.getPublishedAt()));
        holder.stuff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null)
                    mOnItemClickListener.onItemClick(v, position);
            }
        });
        holder.stuff.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOnItemClickListener != null)
                    return mOnItemClickListener.onItemLongClick(v, position);

                return false;
            }
        });

        bindColBtn(holder.likeBtn, position);
    }

    @Override
    public int getItemCount() {
        return mStuffs.size();
    }

    @Override
    public long getItemId(int position) {
        return mStuffs.get(position).getId().hashCode();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        boolean onItemLongClick(View v, int position);

        void onItemClick(View v, int position);
    }

    protected abstract void initStuffs(Realm realm, String mType);

    protected abstract void bindColBtn(ImageButton likeBtn, int position);

    public <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    public Stuff getStuffAt(int pos) {
        return mStuffs.get(pos);
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView title, source, date;
        LinearLayout stuff;
        ImageButton likeBtn;

        public Viewholder(View itemView) {
            super(itemView);
            title = $(itemView, R.id.stuff_title);
            source = $(itemView, R.id.stuff_author);
            date = $(itemView, R.id.stuff_date);
            stuff = $(itemView, R.id.stuff);
            likeBtn = $(itemView, R.id.like_btn);
        }
    }
}
