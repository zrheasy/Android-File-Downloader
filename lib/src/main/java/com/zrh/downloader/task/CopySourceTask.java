package com.zrh.downloader.task;

import com.zrh.downloader.ErrorCode;
import com.zrh.downloader.utils.FileUtils;

import java.io.File;
import java.util.concurrent.ExecutorService;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class CopySourceTask extends StateTask implements Runnable {
    private final File source;
    private final File outputDir;
    private final String fileName;
    private final ExecutorService executor;

    public CopySourceTask(ExecutorService executor, File source, File outputDir, String fileName) {
        this.source = source;
        this.outputDir = outputDir;
        this.fileName = fileName;
        this.executor = executor;
    }

    @Override
    public void stop() {
        clearCallbacks();
    }

    @Override
    public void start() {
        File outputFile = new File(outputDir, fileName);
        if (outputFile.exists()) {
            onCompleted(outputFile);
            return;
        }

        if (isRunning()) return;
        setRunning();
        executor.submit(this);
    }

    @Override
    public void run() {
        if (!outputDir.exists()) outputDir.mkdirs();
        File outputFile = new File(outputDir, fileName);
        boolean result = FileUtils.copy(source, outputFile);
        if (result) {
            onCompleted(outputFile);
        } else {
            onError();
        }
    }

    private void onCompleted(File file) {
        setResult(file);
        notifyCompleted(file);
    }

    private void onError() {
        String msg = "copy source error";
        setError(ErrorCode.COPY_SOURCE_ERROR, msg);
        notifyError(ErrorCode.COPY_SOURCE_ERROR, msg);
    }

}
