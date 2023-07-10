package com.zrh.downloader.task;

import com.zrh.downloader.DownloadCallback;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kotlin.jvm.Volatile;

/**
 * @author zrh
 * @date 2023/7/10
 */
public abstract class StateTask implements Task {
    public static final int IDLE = 0;
    public static final int RUNNING = 1;
    public static final int COMPLETED = 2;
    public static final int ERROR = 3;

    @Volatile
    private int state = IDLE;
    private File result = null;
    private Error error = null;

    private final Set<DownloadCallback> mCallbacks = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void addCallback(DownloadCallback callback) {
        if (state == COMPLETED || state == ERROR) {
            if (getState() == COMPLETED) {
                callback.onCompleted(getResult());
            } else {
                Error error = getError();
                callback.onError(error.code, error.msg);
            }
            return;
        }

        mCallbacks.add(callback);
    }

    @Override
    public void removeCallback(DownloadCallback callback) {
        mCallbacks.remove(callback);
    }

    protected void clearCallbacks() {
        mCallbacks.clear();
    }

    protected void notifyCompleted(File result) {
        for (DownloadCallback callback : mCallbacks) {
            callback.onCompleted(result);
        }
    }

    protected void notifyError(int code, String msg) {
        for (DownloadCallback callback : mCallbacks) {
            callback.onError(code, msg);
        }
    }

    protected void notifyProgress(float percent) {
        for (DownloadCallback callback : mCallbacks) {
            callback.onProgress(percent);
        }
    }

    public int getState() {
        return state;
    }

    public void setRunning() {
        state = RUNNING;
    }

    public File getResult() {
        return result;
    }

    public void setResult(File result) {
        state = COMPLETED;
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(int code, String msg) {
        state = ERROR;
        this.error = new Error(code, msg);
    }

    public boolean isRunning() {
        return state == RUNNING;
    }

    public static class Error {
        int code;
        String msg;

        public Error(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }
}
