package com.zrh.downloader;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.zrh.downloader.task.Task;

import java.io.File;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class DownloadHandle {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Task mTask;
    private DownloadCallback mCallback;

    public DownloadHandle(Task task) {
        this.mTask = task;
        DownloadCallback callback = new DownloadCallback() {
            @Override
            public void onCompleted(@NonNull File file) {
                if (mCallback == null) return;
                mainHandler.post(() -> {
                    mCallback.onCompleted(file);
                });
            }

            @Override
            public void onError(int code, @NonNull String msg) {
                if (mCallback == null) return;
                mainHandler.post(() -> {
                    mCallback.onError(code, msg);
                });
            }

            @Override
            public void onProgress(float percent) {
                if (mCallback == null) return;
                mainHandler.post(() -> {
                    mCallback.onProgress(percent);
                });
            }
        };
        task.addCallback(callback);
    }

    public void setCallback(DownloadCallback callback) {
        mCallback = callback;
    }

    public void stop() {
        mCallback = null;
        mainHandler.removeCallbacksAndMessages(null);
        mTask.stop();
    }
}
