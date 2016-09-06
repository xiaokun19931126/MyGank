package com.domkoo.mygank;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.domkoo.mygank.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class AboutActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayMap<String, String> mLibsList;
    private List<String> mFeasList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mToolbar = $(R.id.about_toolbar);
        mRecyclerView = $(R.id.about_recyclerview);

        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        setSupportActionBar(mToolbar);
        if (NavUtils.getParentActivityName(this) != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView verTv = $(R.id.version_name);
        try {
            verTv.setText(String.format(getString(R.string.version_name), CommonUtil.getVersionName(this)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        initData();
        AboutAdapter adapter = new AboutAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this);
                }

                finishActivity();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initData() {
        mLibsList = new ArrayMap<>();
        mLibsList.put("bumptech / Glide", "https://github.com/bumptech/glide");
        mLibsList.put("Mike Ortiz / TouchImageView", "https://github.com/MikeOrtiz/TouchImageView");
        mLibsList.put("Realm / Realm Java", "https://realm.io/docs/java/latest/");
        mLibsList.put("Square / Retrofit", "https://github.com/square/retrofit");
        mLibsList.put("wasabeef / glide-transformations", "https://github.com/wasabeef/glide-transformations");
        mFeasList = new ArrayList<>();
        mFeasList.add("Splash");
        mFeasList.add("SnackBar");
        mFeasList.add("CardView");
        mFeasList.add("SearchView");
        mFeasList.add("CollapsingToolbarLayout");
        mFeasList.add("CropCircleTransformation");
        mFeasList.add("DrawerLayout");
        mFeasList.add("RecyclerView");
//        mFeasList.add("Shared Element Transition");
        mFeasList.add("Slide-in-out-animation");
        mFeasList.add("Immersive Mode");
    }

    private <T extends View> T $(int resId) {
        return (T) findViewById(resId);
    }

    private <T extends View> T $(View view, int resId) {
        return (T) view.findViewById(resId);
    }

    class AboutAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return viewType == 1
                    ? new ItemViewHolder(getLayoutInflater().inflate(R.layout.about_item, parent, false))
                    : new HeaderViewHolder(getLayoutInflater().inflate(R.layout.about_header, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (holder.getItemViewType() == 1) {
                if (position < mLibsList.size() + 1) {
                    ((ItemViewHolder) holder).textView.setText(mLibsList.keyAt(position - 1));
                } else {
                    ((ItemViewHolder) holder).textView.setText(mFeasList.get(position - 2 - mLibsList.size()));
                    ((ItemViewHolder) holder).textView.setClickable(false);
                }
            } else {
                ((HeaderViewHolder) holder).textView.setText(position == 0 ? R.string.about_libs_used : R.string.about_feas_used);
            }

        }

        @Override
        public int getItemCount() {
            return mLibsList.size() + mFeasList.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 || position == mLibsList.size() + 1 ? 0 : 1;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class HeaderViewHolder extends ViewHolder {
        TextView textView;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView;
        }
    }

    private class ItemViewHolder extends ViewHolder {
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = $(itemView, R.id.item_text);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = getAdapterPosition();
                    if (pos < mLibsList.size() + 1 && pos != 0) {
                        CommonUtil.openUrl(AboutActivity.this, mLibsList.valueAt(pos - 1));
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }
    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }
}
