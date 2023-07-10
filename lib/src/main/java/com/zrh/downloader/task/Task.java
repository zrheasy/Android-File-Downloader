package com.zrh.downloader.task;

import com.zrh.downloader.DownloadCallback;

/**
 * @author zrh
 * @date 2023/7/10
 */
public interface Task {
    void addCallback(DownloadCallback callback);

    void removeCallback(DownloadCallback callback);

    void stop();

    void start();
}
