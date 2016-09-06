package com.domkoo.mygank;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.domkoo.mygank.base.BaseFragment;
import com.domkoo.mygank.base.StuffBaseFragment;
import com.domkoo.mygank.db.Image;
import com.domkoo.mygank.db.Stuff;
import com.domkoo.mygank.utils.CommonUtil;
import com.domkoo.mygank.utils.Constants;
import com.domkoo.mygank.widget.CollectionFragment;
import com.domkoo.mygank.widget.GirlsFragment;
import com.domkoo.mygank.widget.SearchFragment;
import com.domkoo.mygank.widget.SearchSuggestionProvider;
import com.domkoo.mygank.widget.StuffFragment;

import java.util.List;
import java.util.Map;

import io.realm.Realm;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.domkoo.mygank.utils.Constants.TYPE;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    private static final String CURR_TYPE = "curr_fragment_type";
    private static final int CLEAR_DONE = 0x36;
    private static final int CLEAR_ALL = 0x33;

    private CoordinatorLayout mCoordinatorLayout;
    GestureDetector mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            CommonUtil.makeSnackBar(mCoordinatorLayout, getResources().getString(R.string.main_double_taps), Snackbar.LENGTH_LONG);
            return true;
        }
    });
    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private Fragment mCurrFragment;
    private String mCurrFragmentType;
    private Bundle reenterState;

    private Handler mClearCacheHandler;
    private Realm mRealm;
    private DrawerLayout mDrawer;
    private SearchView mSearchView;
    private boolean mIsSearching;
    private ImageView avatar;
    private List<Image> mImages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearchView = (SearchView) findViewById(R.id.searchview);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.nav_girls);
        setSupportActionBar(mToolbar);


        mToolbar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        final FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = GirlsFragment.newInstance(TYPE.GIRLS.getApiName());
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment, TYPE.GIRLS.getId())
                    .commit();
            mCurrFragment = fragment;
            mCurrFragmentType = TYPE.GIRLS.getId();
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coor_layout);
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseFragment) mCurrFragment).smoothScrollToTop();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();




        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


//        avatar = (ImageView) findViewById(R.id.imageView);
//        avatar.setImageResource(R.mipmap.avatar);
        mRealm = Realm.getDefaultInstance();
        mImages = Image.all(mRealm);
        View view = navigationView.getHeaderView(0);
        avatar = (ImageView) view.findViewById(R.id.imageView);
        Glide.with(MainActivity.this)
                .load(R.mipmap.avatar)
                .bitmapTransform(new CropCircleTransformation(this))
                .dontAnimate()
                .into(avatar);
        if (mImages.size() > 0) {
            Glide.with(MainActivity.this)
                    .load(mImages.get(0).getUrl())
                    .bitmapTransform(new CropCircleTransformation(this))
                    .dontAnimate()
                    .into(avatar);
        }

        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (reenterState != null && TYPE.GIRLS.getId().equals(mCurrFragmentType)) {
                    GirlsFragment girlsFragment = (GirlsFragment) mCurrFragment;
                    int i = reenterState.getInt(ViewerActivity.INDEX, 0);
//                    Log.d(TAG, "onMapSharedElements: reenter from " + i);

                    sharedElements.clear();
                    sharedElements.put(girlsFragment.getImageUrlAt(i), girlsFragment.getImageViewAt(i));

                    reenterState = null;
                }
            }
        });

        mRealm = Realm.getDefaultInstance();
        mClearCacheHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case CLEAR_DONE:
                        ((BaseFragment) mCurrFragment).updateData();
                        break;
                    case CLEAR_ALL:
//                        for (TYPE type : TYPE.values()) {
//                            Fragment fragment = getSupportFragmentManager().findFragmentByTag(type.getId());
//                            if (fragment == null)
//                                continue;
//
//                            ((BaseFragment) fragment).updateData();
//                        }
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TYPE.GIRLS.getId());
                        if (fragment != null)
                            ((BaseFragment) fragment).updateData();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            if (((BaseFragment) mCurrFragment).isFetching()) {
                CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.frag_is_fetching), Snackbar.LENGTH_SHORT);
                return;
            }

            String query = intent.getStringExtra(SearchManager.QUERY);
            final String safeText = CommonUtil.stringFilterStrict(query);
            if (safeText == null || safeText.length() == 0 || safeText.length() != query.length()) {
                CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.search_tips), Snackbar.LENGTH_LONG);
            } else {
                new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE)
                        .saveRecentQuery(safeText, null);
                TYPE type = getCurrSearchType();
                String searchCat;
                if (type == null)
                    searchCat = getString(R.string.api_all);
                else
                    searchCat = type.getApiName();
                switchToSearchResult(safeText, searchCat, 10);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: test");
        mRealm.close();
        CommonUtil.clearCache(getApplicationContext());


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURR_TYPE, mCurrFragmentType);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrFragmentType = savedInstanceState.getString(CURR_TYPE);
        hideAllExcept(mCurrFragmentType);
        mToolbar.setTitle(TYPE.valueOf(mCurrFragmentType).getStrId());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mIsSearching) {
            mIsSearching = false;
            hideSearchView();
        } else {
//            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
            return true;
        } else if (id == R.id.action_clear_cache) {
            if (((BaseFragment) mCurrFragment).isFetching())
                CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.frag_is_fetching), Snackbar.LENGTH_SHORT);
            else
                clearRealmType(mCurrFragmentType);
            return true;
        } else if (id == R.id.action_search) {
            mIsSearching = true;
            showSearchView();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();
        if (((BaseFragment) mCurrFragment).isFetching()) {
            CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.frag_is_fetching), Snackbar.LENGTH_SHORT);
            closeDrawer();
            return false;
        }

        if (id == TYPE.GIRLS.getResId()) {
            switchTo(manager, TYPE.GIRLS.getId(), GirlsFragment.newInstance(TYPE.GIRLS.getApiName()));
        } else if (id == TYPE.COLLECTIONS.getResId()) {
            switchTo(manager, TYPE.COLLECTIONS.getId(), CollectionFragment.newInstance(TYPE.COLLECTIONS.getApiName()));
        } else {
            for (TYPE type : TYPE.values()) {
                if (type.getResId() == id) {
                    switchTo(manager, type.getId(), StuffFragment.newInstance(type.getApiName()));
                    break;
                }
            }
        }

        closeDrawer();
        return true;
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        super.onActivityReenter(resultCode, data);

        if (TYPE.GIRLS.getId().equals(mCurrFragmentType)) {
            supportPostponeEnterTransition();

            reenterState = new Bundle(data.getExtras());

            final int index = reenterState.getInt(ViewerActivity.INDEX, 0);
            ((GirlsFragment) mCurrFragment).onActivityReenter(index);
        }
    }

    private void clearRealmType(final String typeId) {
        if (TYPE.COLLECTIONS.getId().equals(typeId)) {
            clearCacheSnackBar(R.string.clear_cache_all, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Image.clearImage(MainActivity.this, mRealm);
                    Stuff.clearAll(mRealm);
                    mClearCacheHandler.sendEmptyMessage(CLEAR_ALL);
                }
            });
        } else if (TYPE.SEARCH_RESULTS.getId().equals(typeId)) {
            CommonUtil.makeSnackBar(mCoordinatorLayout, getString(R.string.no_search_cache), Snackbar.LENGTH_SHORT);
        } else {
            final int strId = TYPE.valueOf(typeId).getStrId();
            final String apiName = TYPE.valueOf(typeId).getApiName();
            if (strId != -1) {
                clearCacheSnackBar(strId, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (TYPE.GIRLS.getApiName().equals(apiName))
                            Image.clearImage(MainActivity.this, mRealm);
                        else
                            Stuff.clearType(mRealm, apiName);
                        mClearCacheHandler.sendEmptyMessage(CLEAR_DONE);
                    }
                });
            }
        }
    }

    private void clearCacheSnackBar(int clearTipStrId, View.OnClickListener onClickListener) {
        CommonUtil.makeSnackBarWithAction(
                mCoordinatorLayout,
                String.format(getString(R.string.clear_type), getString(clearTipStrId)),
                Snackbar.LENGTH_SHORT,
                onClickListener,
                getString(R.string.confirm));
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void switchTo(FragmentManager manager, String type, Fragment addedFragment) {
        Fragment fragment = manager.findFragmentByTag(type);
        if (null != fragment) {
            hideAndShow(manager, fragment, type);
        } else {
            hideAndAdd(manager, addedFragment, type);
        }
        if (mIsSearching)
            hideSearchView();
    }

    private void switchToSearchResult(String keyword, String category, int count) {
        FragmentManager manager = getSupportFragmentManager();
        String searchTag = Constants.TYPE.SEARCH_RESULTS.getId();
        Fragment searchFragment = manager.findFragmentByTag(searchTag);
        if (searchFragment == null) {
            hideAndAdd(manager, SearchFragment.newInstance(keyword, category, count), searchTag);
        } else {
            hideAndShow(manager, searchFragment, searchTag);
            ((SearchFragment) searchFragment).search(keyword, category, count);
        }
    }

    private void hideAllExcept(String mCurrFragmentType) {
        FragmentManager manager = getSupportFragmentManager();
        for (TYPE type : TYPE.values()) {
            Fragment fragment = manager.findFragmentByTag(type.getId());
            if (fragment == null)
                continue;

            if (type.getId().equals(mCurrFragmentType)) {
                manager.beginTransaction().show(fragment).commit();
                mCurrFragment = fragment;
            } else {
                manager.beginTransaction().hide(fragment).commit();
            }
        }
    }

    private void hideAndAdd(FragmentManager manager, Fragment newFragment, String fragmentIdx) {
        manager.beginTransaction().hide(mCurrFragment).add(R.id.fragment_container, newFragment, fragmentIdx).commit();
        mCurrFragment = newFragment;
        mCurrFragmentType = fragmentIdx;
        mToolbar.setTitle(TYPE.valueOf(fragmentIdx).getStrId());
    }

    private void hideAndShow(FragmentManager manager, Fragment newFragment, String fragmentIdx) {
        manager.beginTransaction().hide(mCurrFragment).show(newFragment).commit();
        updateLikedData(newFragment, fragmentIdx);
        mCurrFragment = newFragment;
        mCurrFragmentType = fragmentIdx;
        mToolbar.setTitle(TYPE.valueOf(fragmentIdx).getStrId());
    }

    private void showSearchView() {
        if (mSearchView != null) {
            mSearchView.setVisibility(View.VISIBLE);
            int cx = mSearchView.getWidth() - (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24, mSearchView.getResources().getDisplayMetrics());
            int cy = mSearchView.getHeight() / 2;
            int finalRadius = Math.max(mSearchView.getWidth(), mSearchView.getHeight());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                ViewAnimationUtils.createCircularReveal(mSearchView, cx, cy, 0, finalRadius).start();
        }

        if (mToolbar != null)
            mToolbar.setVisibility(View.GONE);
        updateSearchHint();
    }

    private void hideSearchView() {
        if (mSearchView != null)
            mSearchView.setVisibility(View.GONE);
        if (mToolbar != null)
            mToolbar.setVisibility(View.VISIBLE);
    }

    private void updateSearchHint() {
        int navResId;
        TYPE type = getCurrSearchType();
        if (type == null)
            navResId = R.string.search_all;
        else
            navResId = type.getStrId();

        if (mSearchView != null)
            mSearchView.setQueryHint(String.format(getString(R.string.search), getString(navResId)));
    }

    private TYPE getCurrSearchType() {
        if (TYPE.GIRLS.getId().equals(mCurrFragmentType)
                || TYPE.COLLECTIONS.getId().equals(mCurrFragmentType)
                || TYPE.SEARCH_RESULTS.getId().equals(mCurrFragmentType))
            return null;
        else
            return TYPE.valueOf(mCurrFragmentType);
    }

    private void updateLikedData(Fragment newFragment, String fragmentIdx) {
        if (fragmentIdx.equals(TYPE.GIRLS.getId()) || fragmentIdx.equals(TYPE.SEARCH_RESULTS.getId())) {
            return;
        }
        ((StuffBaseFragment) newFragment).updateData();
    }

    private void exitClear() {
        String apiName = "";
        apiName = TYPE.ANDROID.getApiName();
        Stuff.clearType(mRealm, apiName);
        apiName = TYPE.IOS.getApiName();
        Stuff.clearType(mRealm, apiName);
        apiName = TYPE.WEB.getApiName();
        Stuff.clearType(mRealm, apiName);
        apiName = TYPE.APP.getApiName();
        Stuff.clearType(mRealm, apiName);
        apiName = TYPE.FUN.getApiName();
        Stuff.clearType(mRealm, apiName);
        apiName = TYPE.OTHERS.getApiName();
        Stuff.clearType(mRealm, apiName);
    }

}
