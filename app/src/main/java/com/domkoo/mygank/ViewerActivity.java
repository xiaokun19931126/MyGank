package com.domkoo.mygank;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.domkoo.mygank.db.Image;
import com.domkoo.mygank.utils.CommonUtil;
import com.domkoo.mygank.utils.PicUtil;
import com.domkoo.mygank.widget.GirlsFragment;
import com.domkoo.mygank.widget.ViewerFragment;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;

public class ViewerActivity extends AppCompatActivity {
    public static final String TAG = "ViewerActivity";
    public static final String INDEX = "index";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 111;
    private static final String MSG_URL = "msg_url";
    private static final String SHARE_TITLE = "share_title";
    private static final String SHARE_TEXT = "share_text";
    private static final String SHARE_URL = "share_url";
    private ViewPager mViewPager;
    private final Handler mMsgHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case PicUtil.SAVE_DONE_TOAST:
                    String filepath = msg.getData().getString(PicUtil.FILEPATH);
                    CommonUtil.makeSnackBar(mViewPager, getString(R.string.pic_saved) + filepath, Snackbar.LENGTH_LONG);
                    break;
                default:
                    break;
            }
        }
    };
    private List<Image> mImages;
    private int mPos;
    private int mSavedPicPos = -1;
    private Toolbar mToolbar;
    private Realm mRealm;
    private FragmentStatePagerAdapter mAdapter;
    private boolean mIsHidden;
    private HandlerThread mThread;
    private Handler mSavePicHandler;
    private Handler mShareHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.viewer_pager_layout);


        initToolbar();
        mPos = getIntent().getIntExtra(GirlsFragment.POSTION, 0);
        mRealm = Realm.getDefaultInstance();

        mImages = Image.all(mRealm);
        mViewPager = (ViewPager) findViewById(R.id.viewer_pager);
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        mAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return ViewerFragment.newInstance(
                        mImages.get(position).getUrl(),
                        position == mPos);
            }

            @Override
            public int getCount() {
                return mImages.size();
            }
        };
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                hideToolbar();
            }
        });
        mViewPager.setCurrentItem(mPos);

        // 避免图片在进行 Shared Element Transition 时盖过 Toolbar
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setSharedElementsUseOverlay(false);
        }

        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                Image image = mImages.get(mViewPager.getCurrentItem());
                sharedElements.clear();
                sharedElements.put(image.getUrl(), ((ViewerFragment) mAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem())).getSharedElement());
            }
        });

        mThread = new HandlerThread("save-and-share");
        mThread.start();
        mSavePicHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                final String url = msg.getData().getString(MSG_URL);
                try {
                    PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, mMsgHandler);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        mShareHandler = new Handler(mThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                final String title = msg.getData().getString(SHARE_TITLE);
                final String text = msg.getData().getString(SHARE_TEXT);
                final String url = msg.getData().getString(SHARE_URL);
                shareMsg(title, text, url);
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeAllChangeListeners();
        mRealm.close();
        mThread.quit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.viewer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                supportFinishAfterTransition();
                finishActivity();
                return true;
            case R.id.img_save:
                mSavedPicPos = mViewPager.getCurrentItem();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                } else {
                    savePicAt(mSavedPicPos);
                }
                return true;
            case R.id.img_share:
                Message message = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putString(SHARE_TITLE, getString(R.string.share_msg));
                bundle.putString(SHARE_TEXT, null);
                bundle.putString(SHARE_URL, mImages.get(mViewPager.getCurrentItem()).getUrl());
                message.setData(bundle);
                mShareHandler.sendMessage(message);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(INDEX, mViewPager.getCurrentItem());
        setResult(RESULT_OK, data);

        super.supportFinishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
        finishActivity();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.viewer_toolbar);
        mToolbar.setTitle(R.string.nav_girls);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        setSupportActionBar(mToolbar);
    }

    private void savePicAt(int pos) {
        if (pos < 0)
            return;

        Message message = Message.obtain();
        String url = mImages.get(pos).getUrl();
        Bundle bundle = new Bundle();
        bundle.putString(MSG_URL, url);
        message.setData(bundle);
        mSavePicHandler.sendMessage(message);
    }

    public void shareMsg(String msgTitle, String msgText, String url) {
        String imgPath = PicUtil.getImgPathFromUrl(url);

        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain");
        } else {
            File file = new File(imgPath);
            if (!file.exists()) {
                try {
                    PicUtil.saveBitmapFromUrl(ViewerActivity.this, url, mMsgHandler);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (file.exists() && file.isFile()) {
                intent.setType("image/jpg");
                Uri uri = Uri.fromFile(file);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void toggleToolbar() {
        if (mIsHidden) {
            showToolbar();
        } else {
            hideToolbar();
        }
    }

    public void hideToolbar() {
        if (mIsHidden)
            return;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mToolbar.getLayoutParams();
        mToolbar.animate().translationY(-(mToolbar.getHeight() + lp.topMargin)).setInterpolator(new AccelerateInterpolator(2));
        mIsHidden = true;
    }

    public void showToolbar() {
        if (!mIsHidden)
            return;

        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        mIsHidden = false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            savePicAt(mSavedPicPos);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
    }

}
