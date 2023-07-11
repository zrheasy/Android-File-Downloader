package com.zrh.downloader.task;

import androidx.annotation.NonNull;

import com.zrh.downloader.DownloadCallback;
import com.zrh.downloader.HeaderProvider;
import com.zrh.downloader.engine.DownloadEngine;
import com.zrh.downloader.engine.Request;

import java.io.File;
import java.util.Map;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class SourceDownloadTask extends StateTask implements DownloadCallback {
    private final DownloadEngine mEngine;
    private final String url;
    private final File outputDir;
    private final String fileName;
    private Request request;
    private final HeaderProvider provider;

    public SourceDownloadTask(DownloadEngine engine,
                              String url,
                              File outputDir,
                              String fileName,
                              HeaderProvider provider) {
        this.mEngine = engine;
        this.url = url;
        this.outputDir = outputDir;
        this.fileName = fileName;
        this.provider = provider;
    }

    @Override
    public void stop() {
        clearCallbacks();
        if (request != null) request.cancel();
    }

    @Override
    public void start() {
        if (isRunning()) return;
        setRunning();
        String tempName = fileName + ".temp";
        Map<String, String> headers = provider.getHeaders(url);
        request = new Request(url, new File(outputDir, tempName), headers, this);
        mEngine.execute(request);
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
