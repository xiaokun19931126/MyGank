package com.domkoo.mygank.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.domkoo.mygank.db.Image;
import com.domkoo.mygank.net.GankAPI;
import com.domkoo.mygank.net.GankAPIService;
import com.domkoo.mygank.net.ImageFetcher;
import com.domkoo.mygank.utils.Constants;
import com.domkoo.mygank.utils.DateUtil;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import io.realm.RealmResults;

public class ImageFetchService extends IntentService implements ImageFetcher {
    public static final String ACTION_UPDATE_RESULT = "com.ivor.meizhi.girls_update_result";
    public static final String EXTRA_FETCHED = "girls_fetched";
    public static final String EXTRA_TRIGGER = "girls_trigger";
    public static final String EXTRA_EXCEPTION_CODE = "exception_code";
    public static final String ACTION_FETCH_REFRESH = "com.ivor.meizhi.girls_fetch_refresh";
    public static final String ACTION_FETCH_MORE = "com.ivor.meizhi.girls_fetch_more";
    private static final String TAG = "ImageFetchService";
    private LocalBroadcastManager localBroadcastManager;
    private Constants.NETWORK_EXCEPTION mExceptionCode;

    public ImageFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExceptionCode = Constants.NETWORK_EXCEPTION.DEFAULT;
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Image> latest = Image.all(realm);

        int fetched = 0;
        try {
            if (latest.isEmpty()) {
                fetched = fetchLatest(realm);
                Log.d(TAG, "no latest, fresh fetch");
            } else if (ACTION_FETCH_REFRESH.equals(intent.getAction())) {
                Log.d(TAG, "latest fetch: " + latest.first().getPublishedAt());
                fetched = fetchRefresh(realm, latest.first().getPublishedAt());
            } else if (ACTION_FETCH_MORE.equals(intent.getAction())) {
                Log.d(TAG, "earliest fetch: " + latest.last().getPublishedAt());
                fetched = fetchMore(realm, latest.last().getPublishedAt());
            }
        } catch (SocketTimeoutException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.TIMEOUT;
        } catch (UnknownHostException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.UNKNOWN_HOST;
        } catch (IOException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.IOEXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendResult(intent, realm, fetched);
    }

    private void sendResult(Intent intent, Realm realm, int fetched) {
        realm.close();

        Log.d(TAG, "finished fetching, actual fetched " + fetched);

        Intent broadcast = new Intent(ACTION_UPDATE_RESULT)
                .putExtra(EXTRA_FETCHED, fetched)
                .putExtra(EXTRA_EXCEPTION_CODE, mExceptionCode)
                .putExtra(EXTRA_TRIGGER, intent.getAction());

        localBroadcastManager.sendBroadcast(broadcast);
    }


    private int fetchLatest(final Realm realm) throws IOException {
        GankAPI.Result<List<Image>> result = GankAPIService.getInstance().latestGirls(10).execute().body();

        if (result.error)
            return 0;

        int resultSize = result.results.size();
        for (int i = 0; i < resultSize; i++) {
            if (!saveToDb(realm, result.results.get(i)))
                return i;
        }

        return resultSize;
    }

    private int fetchRefresh(Realm realm, Date publishedAt) throws IOException {
        String after = DateUtil.format(publishedAt);
        List<String> dates = DateUtil.generateSequenceDateTillToday(publishedAt);
        return fetch(realm, after, dates);
    }

    private int fetchMore(Realm realm, Date publishedAt) throws IOException {
        String before = DateUtil.format(publishedAt);
        List<String> dates = DateUtil.generateSequenceDateBefore(publishedAt, 20);
        return fetch(realm, before, dates);
    }

    private int fetch(Realm realm, String baseline, List<String> dates) throws IOException {
        int fetched = 0;

        for (String date : dates) {
            if (date.equals(baseline))
                continue;

            GankAPI.Result<GankAPI.Girls> girlsResult = GankAPIService.getInstance().dayGirls(date).execute().body();

            if (girlsResult.error || null == girlsResult.results || null == girlsResult.results.images)
                continue;

            for (Image image : girlsResult.results.images) {
                if (!saveToDb(realm, image))
                    return fetched;

                fetched++;
            }
        }
        return fetched;
    }

    private boolean saveToDb(Realm realm, final Image image) {
        realm.beginTransaction();

        try {
            realm.copyToRealm(Image.persist(image, this));
        } catch (Exception e) {
            Log.e(TAG, "Failed to fetch image", e);
            realm.cancelTransaction();
            return false;
        }

        realm.commitTransaction();
        return true;
    }

    @Override
    public void prefetchImage(String url, Point measured) throws IOException, InterruptedException, ExecutionException {
        Bitmap bitmap = Glide.with(this)
                .load(url).asBitmap()
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                .get();

        measured.x = bitmap.getWidth();
        measured.y = bitmap.getHeight();

//        Log.d(TAG, "pre-measured image: " + measured.x + " x " + measured.y + " " + url);
    }
}
