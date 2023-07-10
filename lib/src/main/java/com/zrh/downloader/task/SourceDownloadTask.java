package com.zrh.downloader.task;

import androidx.annotation.NonNull;

import com.zrh.downloader.DownloadCallback;
import com.zrh.downloader.engine.DownloadEngine;
import com.zrh.downloader.engine.Request;

import java.io.File;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class SourceDownloadTask extends StateTask implements DownloadCallback {
    private final DownloadEngine mEngine;
    private final String url;
    private final File outputDir;
    private final String fileName;

    public SourceDownloadTask(DownloadEngine engine, String url, File outputDir, String fileName) {
        this.mEngine = engine;
        this.url = url;
        this.outputDir = outputDir;
        this.fileName = fileName;
    }

    @Override
    public void stop() {
        clearCallbacks();
    }

    @Override
    public void start() {
        if (isRunning()) return;
        setRunning();
        String tempName = fileName + ".temp";
        mEngine.execute(new Request(url, new File(outputDir, tempName), this));
    }

    @Override
    public void onCompleted(@NonNull File file) {
        File outputFile = new File(outputDir, fileName);
        file.renameTo(outputFile);

        setResult(outputFile);
        notifyCompleted(outputFile);
    }

    @Override
    public void onError(int code, @NonNull String msg) {
        setError(code, msg);

        notifyError(code, msg);
    }

    @Override
    public void onProgress(float percent) {
        notifyProgress(percent);
    }
}
