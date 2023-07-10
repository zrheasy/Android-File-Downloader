package com.zrh.downloader.engine;

import androidx.annotation.NonNull;

import com.zrh.downloader.DownloadCallback;

import java.io.File;

import kotlin.jvm.Volatile;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class Request {
    private final String url;
    private final File output;
    private final DownloadCallback callback;
    @Volatile
    private boolean isCanceled = false;

    public Request(String url, File output, DownloadCallback callback) {
        this.url = url;
        this.output = output;
        this.callback = callback;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    @NonNull
    public File getOutput() {
        return output;
    }

    @NonNull
    public DownloadCallback getCallback() {
        return callback;
    }

    public void cancel() {
        isCanceled = true;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
