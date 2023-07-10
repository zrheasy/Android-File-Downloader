package com.zrh.downloader.task;

import androidx.annotation.NonNull;

import com.zrh.downloader.DownloadCallback;
import com.zrh.downloader.ErrorCode;
import com.zrh.downloader.utils.FileUtils;

import java.io.File;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class FileDownloadTask extends StateTask implements DownloadCallback {
    private final File outputDir;
    private final String fileName;

    public FileDownloadTask(Task sourceDownloadTask, File outputDir, String fileName) {
        this.outputDir = outputDir;
        this.fileName = fileName;
        sourceDownloadTask.addCallback(this);
    }

    @Override
    public void stop() {
        clearCallbacks();
    }

    @Override
    public void start() {
        // waiting source download complete
    }

    @Override
    public void onCompleted(@NonNull File file) {
        if (!outputDir.exists()) outputDir.mkdirs();
        File outputFile = new File(outputDir, fileName);
        boolean result = FileUtils.copy(file, outputFile);
        if (result) {
            setResult(outputFile);
            notifyCompleted(outputFile);
        } else {
            onError(ErrorCode.COPY_SOURCE_ERROR, "copy source error");
        }
    }

    @Override
    public void onError(int code, @NonNull String msg) {
        setError(code, msg);
        notifyError(ErrorCode.COPY_SOURCE_ERROR, msg);
    }

    @Override
    public void onProgress(float percent) {
        notifyProgress(percent);
    }
}
