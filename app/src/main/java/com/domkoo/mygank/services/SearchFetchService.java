package com.domkoo.mygank.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.domkoo.mygank.db.SearchBean;
import com.domkoo.mygank.net.GankAPI;
import com.domkoo.mygank.net.GankAPIService;
import com.domkoo.mygank.utils.Constants;
import com.domkoo.mygank.widget.SearchFragment;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class SearchFetchService extends IntentService {
    public static final String ACTION_UPDATE_RESULT = "com.ivor.meizhi.update_search";
    public static final String EXTRA_FETCHED = "search_fetched";
    public static final String EXTRA_TRIGGER = "search_trigger";
    public static final String EXTRA_TYPE = "search_type";
    public static final String EXTRA_EXCEPTION_CODE = "search_exception_code";
    public static final String ACTION_FETCH_REFRESH = "com.ivor.meizhi.fetch_search_refresh";
    public static final String ACTION_FETCH_MORE = "com.ivor.meizhi.fetch_search_more";
    private static final String TAG = "SearchFetchService";
    private Constants.NETWORK_EXCEPTION mExceptionCode;
    private LocalBroadcastManager mLocalBroadcastManager;

    public SearchFetchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExceptionCode = Constants.NETWORK_EXCEPTION.DEFAULT;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final String keyword = intent.getStringExtra(SearchFragment.KEYWORD);
        final String category = intent.getStringExtra(SearchFragment.CATEGORY);
        final int count = intent.getIntExtra(SearchFragment.COUNT, 10);
        final int page = intent.getIntExtra(SearchFragment.PAGE, 1);
        ArrayList<SearchBean> beans = null;
        try {
            if (ACTION_FETCH_REFRESH.equals(intent.getAction()))
                beans = fetchRefresh(keyword, category, count, page);
            else if (ACTION_FETCH_MORE.equals(intent.getAction()))
                beans = fetchMore(keyword, category, count, page);
        } catch (SocketTimeoutException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.TIMEOUT;
        } catch (UnknownHostException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.UNKNOWN_HOST;
        } catch (IOException e) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.IOEXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendResult(intent, beans);
    }

    private void sendResult(Intent intent, ArrayList<SearchBean> beans) {
        Intent broadcast = new Intent(ACTION_UPDATE_RESULT);
        broadcast.putParcelableArrayListExtra(EXTRA_FETCHED, beans)
                .putExtra(EXTRA_TRIGGER, intent.getAction())
                .putExtra(EXTRA_EXCEPTION_CODE, mExceptionCode);

        mLocalBroadcastManager.sendBroadcast(broadcast);
    }

    private ArrayList<SearchBean> fetchMore(String keyword, String category, int count, int page) throws IOException {
        Response<GankAPI.Result<List<SearchBean>>> response = GankAPIService.getInstance().search(
                keyword,
                category,
                count,
                page
        ).execute();
        return handleResponse(response);
    }

    private ArrayList<SearchBean> fetchRefresh(String keyword, String category, int count, int page) throws IOException {
        Response<GankAPI.Result<List<SearchBean>>> response = GankAPIService.getInstance().search(
                keyword,
                category,
                count,
                page
        ).execute();
        return handleResponse(response);
    }

    private ArrayList<SearchBean> handleResponse(Response<GankAPI.Result<List<SearchBean>>> response) {
        String code = Integer.toString(response.code());
        if (response.isSuccessful() && !response.body().error) {
            return (ArrayList<SearchBean>) response.body().results;
        } else if (code.startsWith("4")) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.HTTP4XX;
            return new ArrayList<>();
        } else if (code.startsWith("5")) {
            mExceptionCode = Constants.NETWORK_EXCEPTION.HTTP5XX;
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
