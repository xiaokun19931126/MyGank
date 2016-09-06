package com.domkoo.mygank.utils;

import com.domkoo.mygank.R;

public interface Constants {

    enum TYPE {
        GIRLS("GIRLS", "福利", R.string.nav_girls, R.id.nav_girls),
        ANDROID("ANDROID", "Android", R.string.nav_android, R.id.nav_android),
        IOS("IOS", "iOS", R.string.nav_ios, R.id.nav_ios),
        WEB("WEB", "前端", R.string.nav_web, R.id.nav_web),
        APP("APP", "App", R.string.nav_app, R.id.nav_app),
        FUN("FUN", "瞎推荐", R.string.nav_fun, R.id.nav_fun),
        OTHERS("OTHERS", "拓展资源", R.string.nav_others, R.id.nav_others),
        COLLECTIONS("COLLECTIONS", "Collections", R.string.nav_collections, R.id.nav_collections),
        SEARCH_RESULTS("SEARCH_RESULTS", "search_results", R.string.nav_search, 0);

        private final String id;
        private final String apiName;
        private final int strId;
        private final int resId;

        TYPE(String id, String apiName, int strId, int resId) {
            this.id = id;
            this.apiName = apiName;
            this.strId = strId;
            this.resId = resId;
        }

        @Override
        public String toString() {
            return id;
        }

        public String getId() {
            return id;
        }

        public String getApiName() {
            return apiName;
        }

        public int getStrId() {
            return strId;
        }

        public int getResId() {
            return resId;
        }
    }

    enum NETWORK_EXCEPTION {
        DEFAULT(0),
        UNKNOWN_HOST(R.string.network_not_avaiable),
        TIMEOUT(R.string.network_timeout),
        IOEXCEPTION(R.string.network_io),
        HTTP4XX(R.string.network_request_error),
        HTTP5XX(R.string.network_server_error);

        private int tipsResId;

        NETWORK_EXCEPTION(int tipsResId) {
            this.tipsResId = tipsResId;
        }

        public int getTipsResId() {
            return tipsResId;
        }
    }
}
