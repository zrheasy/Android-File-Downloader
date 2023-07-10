package com.zrh.downloader;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * @author zrh
 * @date 2023/7/10
 */
public interface DownloadCallback {
    void onCompleted(@NonNull File file);

    void onError(int code, @NonNull String msg);

    void onProgress(float percent);
}
