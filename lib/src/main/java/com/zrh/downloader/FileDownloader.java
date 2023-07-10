package com.zrh.downloader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zrh.downloader.engine.DownloadEngine;
import com.zrh.downloader.engine.URLDownloadEngine;
import com.zrh.downloader.task.FileDownloadTask;
import com.zrh.downloader.task.Task;
import com.zrh.downloader.task.CopySourceTask;
import com.zrh.downloader.task.SourceDownloadTask;
import com.zrh.downloader.utils.MD5Utils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.jvm.Synchronized;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class FileDownloader {
    private static final String cacheName = "download";
    private static final Map<String, Task> runningTasks = new ConcurrentHashMap<>();
    private static final ExecutorService copyExecutor = Executors.newCachedThreadPool();
    private static DownloadEngine downloadEngine = new URLDownloadEngine();
    private static File sourceDir;

    public static void init(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        File cache = externalCacheDir != null ? externalCacheDir : context.getCacheDir();
        sourceDir = new File(cache, cacheName);
        if (!sourceDir.exists()) {
            sourceDir.mkdirs();
        }
    }

    public static File getSourceDir(){
        return sourceDir;
    }

    public static void setDownloadEngine(DownloadEngine downloadEngine) {
        FileDownloader.downloadEngine = downloadEngine;
    }

    public static DownloadHandle download(String url, File outputDir, String fileName, boolean useCache) {

        Task task = start(url, outputDir, fileName, useCache);

        return new DownloadHandle(task);
    }

    public static void preload(List<String> urls, File outputDir, List<String> fileNames, boolean useCache) {
        for (int i = 0; i < urls.size(); i++) {
            download(urls.get(i), outputDir, fileNames.get(i), useCache);
        }
    }

    @Synchronized
    private static Task start(String url, File outputDir, String fileName, boolean useCache) {
        String key = MD5Utils.md5(url);

        if (useCache) {
            File source = new File(sourceDir, key);
            if (source.exists()) {
                Task task = new CopySourceTask(copyExecutor, source, outputDir, fileName);
                task.start();
                return task;
            }
        }

        Task sourceDownloadTask = runningTasks.get(key);
        if (sourceDownloadTask == null) {
            sourceDownloadTask = new SourceDownloadTask(downloadEngine, url, sourceDir, key);
            sourceDownloadTask.addCallback(new RemoveTaskCallback(key));
            runningTasks.put(key, sourceDownloadTask);

            sourceDownloadTask.start();
        }

        return new FileDownloadTask(sourceDownloadTask, outputDir, fileName);
    }

    private static class RemoveTaskCallback implements DownloadCallback {

        private final String key;

        public RemoveTaskCallback(String key) {
            this.key = key;
        }

        @Override
        public void onCompleted(@NonNull File file) {
            runningTasks.remove(key);
        }

        @Override
        public void onError(int code, @NonNull String msg) {
            runningTasks.remove(key);
        }

        @Override
        public void onProgress(float percent) {

        }
    }
}
