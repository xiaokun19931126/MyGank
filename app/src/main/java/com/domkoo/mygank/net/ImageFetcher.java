package com.domkoo.mygank.net;

import android.graphics.Point;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public interface ImageFetcher {
    void prefetchImage(String url, Point measured)
            throws IOException, InterruptedException, ExecutionException;
}
