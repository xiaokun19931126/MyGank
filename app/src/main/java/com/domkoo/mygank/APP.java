package com.domkoo.mygank;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class APP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder(this)
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .build());
    }
}
