package com.clangpp.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by ytzhang on 11/6/14.
 */
public class SunshineSyncService extends Service {
    private static final Object syncAdapterLock = new Object();
    private static SunshineSyncAdapter sunshineSyncAdapter = null;
    private static final String LOG_TAG = SunshineSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate -- SunshineSyncService");
        synchronized (syncAdapterLock) {
            if (sunshineSyncAdapter == null) {
                sunshineSyncAdapter = new SunshineSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sunshineSyncAdapter.getSyncAdapterBinder();
    }
}
