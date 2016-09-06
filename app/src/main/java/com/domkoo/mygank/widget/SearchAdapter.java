package com.domkoo.mygank.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.domkoo.mygank.R;
import com.domkoo.mygank.db.SearchBean;
import com.domkoo.mygank.db.Stuff;
import com.domkoo.mygank.utils.DateUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.Viewholder> {
    private static final String TAG = "SearchAdapter";
    private Context mContext;
    private List<SearchBean> mSearchBeens;
    private OnItemClickListener mOnItemClickListener;
    private Realm realm;

    public SearchAdapter(Context context, Realm realm) {
        this.mContext = context;
        this.mSearchBeens = new ArrayList<>();
        this.realm = realm;
        setHasStableIds(true);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @Override
    public Viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Viewholder(LayoutInflater.from(mContext).inflate(R.layout.search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Viewholder holder, final int position) {
        final SearchBean searchBean = mSearchBeens.get(position);
        holder.author.setText(searchBean.getWho());
        holder.title.setText(searchBean.getDesc());
        try {
            holder.date.setText(DateUtil.formatSearchDate(searchBean.getPublishedAt()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (null != mOnItemClickListener) {
            holder.stuff.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(v, position);
                }
            });

            holder.stuff.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemClickListener.onItemLongClick(v, position);
                    return true;
                }
            });
        }
        holder.webView.setTag(position);
        holder.webView.getSettings().setUseWideViewPort(true);
        holder.webView.getSettings().setLoadWithOverviewMode(true);
        holder.webView.getSettings().setDefaultFontSize(48);
        holder.webView.loadData(searchBean.getReadability(), "text/html; charset=UTF-8", "utf8");
        holder.webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mOnItemClickListener.onItemClick(view, position);
                        return true;
                    default:
                        return true;
                }
            }
        });

        holder.likeBtn.setTag(position);
        holder.likeBtn.setImageResource(isLiked(searchBean) ? R.drawable.like : R.drawable.unlike);
        holder.likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLikeBtn((ImageButton) v, searchBean);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSearchBeens.size();
    }

    @Override
    public long getItemId(int position) {
        return mSearchBeens.get(position).getUrl().hashCode();
    }

    private void toggleLikeBtn(ImageButton likeBtn, SearchBean bean) {
        if (bean.isLiked()) {
            likeBtn.setImageResource(R.drawable.unlike);
            changeLiked(bean, false);
        } else {
            likeBtn.setImageResource(R.drawable.like);
            changeLiked(bean, true);
        }
    }

    private void changeLiked(SearchBean bean, boolean isLiked) {
        bean.setLiked(isLiked);
        try {
            Stuff stuff = Stuff.checkSearch(realm, bean.getUrl());
            realm.beginTransaction();
            if (stuff == null) {
                stuff = Stuff.fromSearch(bean);
                stuff.setLastChanged(new Date());
                stuff.setLiked(isLiked);
                realm.copyToRealm(stuff);
            } else {
                stuff.setLiked(isLiked);
                stuff.setLastChanged(new Date());
            }
            realm.commitTransaction();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private boolean isLiked(SearchBean bean) {
        Stuff stuff = Stuff.checkSearch(realm, bean.getUrl());
        if (stuff != null) {
            bean.setLiked(stuff.isLiked());
            return stuff.isLiked();
        }
        return false;
    }

    public SearchBean getStuffAt(int pos) {
        return mSearchBeens.get(pos);
    }

    public void updateInsertedData(ArrayList<SearchBean> beans, boolean isMore) {
        if (isMore) {
            int oldSize = mSearchBeens.size();
            mSearchBeens.addAll(beans);
            notifyItemRangeInserted(oldSize, beans.size());
        } else {
            mSearchBeens.clear();
            mSearchBeens.addAll(beans);
            notifyDataSetChanged();
        }
    }

    public void clearData() {
        mSearchBeens.clear();
        notifyDataSetChanged();
    }

    private <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    public interface OnItemClickListener {

        void onItemClick(View view, int pos);

        void onItemLongClick(View view, int pos);

    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView title, author, date;
        LinearLayout stuff;
        ImageButton likeBtn;
        WebView webView;

        public Viewholder(final View itemView) {
            super(itemView);
            title = $(itemView, R.id.stuff_title);
            author = $(itemView, R.id.stuff_author);
            date = $(itemView, R.id.stuff_date);
            stuff = $(itemView, R.id.stuff);
            likeBtn = $(itemView, R.id.like_btn);
            webView = $(itemView, R.id.readability_wv);
        }
    }
}
