package com.ouchadam.bookkeeper;

import android.content.Context;
import android.content.Intent;

import com.ouchadam.bookkeeper.bundle.Bundler;
import com.ouchadam.bookkeeper.bundle.DownloadableBundler;

public class BookKeeper {

    static final String BUNDLE = "bundle";

    public static void keep(Context context, Downloadable downloadable, DownloadWatcher... downloadWatchers) {
        initProgressReciever(context, downloadWatchers);
        startDownloadService(context, downloadable);
    }

    private static void initProgressReciever(Context context, DownloadWatcher[] downloadWatchers) {
        DownloadWatcherManager downloadWatcherManager = new DownloadWatcherManager(downloadWatchers);
        DownloadProgressReceiver downloadProgressReceiver = new DownloadProgressReceiver(downloadWatcherManager);
        downloadProgressReceiver.register(context);
    }

    private static void startDownloadService(Context context, Downloadable downloadable) {
        Intent service = createServiceIntent(context, downloadable);
        context.startService(service);
    }

    private static Intent createServiceIntent(Context context, Downloadable downloadable) {
        Intent service = new Intent(context, DownloadService.class);

        Bundler<Downloadable> bundler = new DownloadableBundler();
        service.putExtra(BUNDLE, bundler.to(downloadable));
        return service;
    }

}