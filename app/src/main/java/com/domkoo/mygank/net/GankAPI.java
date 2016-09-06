package com.domkoo.mygank.net;

import com.domkoo.mygank.db.Image;
import com.domkoo.mygank.db.SearchBean;
import com.domkoo.mygank.db.Stuff;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface GankAPI {
    String BASE_URL = "http://gank.io/api/";
    String LATEST_GIRLS_URL = BASE_URL + "data/%E7%A6%8F%E5%88%A9/10/1";
    String LATEST_ANDROID_URL = BASE_URL + "data/Android/20/1";
    String LATEST_IOS_URL = BASE_URL + "data/iOS/20/1";
    String LATEST_APP_URL = BASE_URL + "data/App/20/1";
    String LATEST_WEB_URL = BASE_URL + "data/%E5%89%8D%E7%AB%AF/20/1";
    String LATEST_OTHERS_URL = BASE_URL + "data/%E6%8B%93%E5%B1%95%E8%B5%84%E6%BA%90/20/1";
    String LATEST_FUN_URL = BASE_URL + "data/%E7%9E%8E%E6%8E%A8%E8%8D%90/20/1";
    String DAYLY_DATA_URL = BASE_URL + "day/";

    @GET("search/query/{keyword}/category/{category}/count/{count}/page/{page}")
    Call<Result<List<SearchBean>>> search(
            @Path("keyword") String keyword,
            @Path("category") String category,
            @Path("count") int count,
            @Path("page") int page);

    @GET("data/%E7%A6%8F%E5%88%A9/{count}/1")
    Call<Result<List<Image>>> latestGirls(@Path("count") int count);

    @GET("day/{date}")
    Call<Result<Girls>> dayGirls(@Path("date") String date);

    @GET("data/{type}/{count}/1")
    Call<Result<List<Stuff>>> latestStuff(@Path("type") String type, @Path("count") int count);

    @GET("day/{date}")
    Call<Result<Androids>> dayAndroids(@Path("date") String date);

    @GET("day/{date}")
    Call<Result<IOSs>> dayIOSs(@Path("date") String date);

    @GET("day/{date}")
    Call<Result<Webs>> dayWebs(@Path("date") String date);

    @GET("day/{date}")
    Call<Result<Funs>> dayFuns(@Path("date") String date);

    @GET("day/{date}")
    Call<Result<Apps>> dayApps(@Path("date") String date);

    @GET("day/{date}")
    Call<Result<Others>> dayOthers(@Path("date") String date);

    class Result<T> {
        public boolean error;
        public T results;
    }

    class Girls{
        @SerializedName("福利")
        public List<Image> images;
    }

    class Androids{
        @SerializedName("Android")
        public List<Stuff> stuffs;
    }

    class IOSs{
        @SerializedName("iOS")
        public List<Stuff> stuffs;
    }

    class Funs{
        @SerializedName("瞎推荐")
        public List<Stuff> stuffs;
    }

    class Others{
        @SerializedName("扩展资源")
        public List<Stuff> stuffs;
    }

    class Webs{
        @SerializedName("前端")
        public List<Stuff> stuffs;
    }

    class Apps{
        @SerializedName("App")
        public List<Stuff> stuffs;
    }
}
