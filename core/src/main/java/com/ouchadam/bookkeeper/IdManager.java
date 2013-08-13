package com.ouchadam.bookkeeper;

import android.content.SharedPreferences;
import com.ouchadam.bookkeeper.progress.OnDownloadFinishedListener;

import java.util.List;
import java.util.Map;

public class IdManager implements OnDownloadFinishedListener {

    private ActiveDownloadFetcher downloaderHelper;
    private final SharedPreferences sharedPreferences;

    public interface BookKeeperRestorer {
        void onRestore(DownloadId downloadId, long itemId);
    }

    IdManager(ActiveDownloadFetcher downloaderHelper, SharedPreferences sharedPreferences) {
        this.downloaderHelper = downloaderHelper;
        this.sharedPreferences = sharedPreferences;
    }

    public void addWithItem(DownloadId downloadId, long itemId) {
        sharedPreferences.edit().putLong(idToKey(downloadId), itemId).apply();
    }

    @Override
    public void onFinish(DownloadId downloadId) {
        removeKey(idToKey(downloadId));
    }

    private void removeKey(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    private String idToKey(DownloadId downloadId) {
        return downloadId.toKey();
    }

    public void restore(final BookKeeperRestorer bookKeeperRestorer) {
        if (sharedPreferences.getAll() != null && !sharedPreferences.getAll().isEmpty()) {
            downloaderHelper.getActiveDownloadIds(new ActiveDownloadFetcher.OnActiveDownloads() {
                @Override
                public void on(List<DownloadId> activeDownloadIds) {
                    handleActiveDownloadIds(activeDownloadIds, bookKeeperRestorer);
                }
            });
        }
    }

    private void handleActiveDownloadIds(List<DownloadId> activeDownloadIds, BookKeeperRestorer bookKeeperRestorer) {
        Map<String, Long> keys = (Map<String, Long>) sharedPreferences.getAll();
        for (Map.Entry<String, Long> entry : keys.entrySet()) {
            String key = entry.getKey();
            long itemId = getLong(entry);
            DownloadId downloadId = new DownloadId(Long.parseLong(key));
            if (pruneIds(activeDownloadIds, key)) {
                bookKeeperRestorer.onRestore(downloadId, itemId);
            } else {
                removeKey(key);
            }
        }
    }

    private long getLong(Map.Entry<String, Long> entry) {
        return entry.getValue();
    }

    private boolean pruneIds(List<DownloadId> downloadIds, String key) {
        return hasId(downloadIds, new DownloadId(keyToId(key)));
    }

    private long keyToId(String key) {
        return Long.valueOf(key);
    }

    private boolean hasId(List<DownloadId> downloadIds, DownloadId idFromPreferences) {
        for (DownloadId currentId : downloadIds) {
            if (currentId.equals(idFromPreferences)) {
                return true;
            }
        }
        return false;
    }

}
