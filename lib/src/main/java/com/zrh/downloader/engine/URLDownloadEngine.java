package com.zrh.downloader.engine;

import android.annotation.SuppressLint;

import com.zrh.downloader.DownloadCallback;
import com.zrh.downloader.ErrorCode;
import com.zrh.downloader.Logger;
import com.zrh.downloader.utils.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zrh
 * @date 2023/7/10
 */
public class URLDownloadEngine implements DownloadEngine {
    private static final int maxRedirectTimes = 10;
    private static final int connectTimeout = 24 * 1000;
    private static final int readTimeout = 24 * 1000;
    private final ExecutorService executor;
    private Logger logger;

    public URLDownloadEngine() {
        int coreSize = Runtime.getRuntime().availableProcessors();
        ArrayBlockingQueue<Runnable> blockingDeque = new ArrayBlockingQueue<>(50);
        executor = new ThreadPoolExecutor(coreSize * 2, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, blockingDeque);
    }

    public URLDownloadEngine(ExecutorService executor) {
        this.executor = executor;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    private void log(String msg) {
        if (logger != null) logger.log(msg);
    }

    @Override
    public void execute(Request request) {
        executor.submit(new DownloadTask(request));
    }

    private class DownloadTask implements Runnable {
        private final Request request;
        private String requestUrl;
        private int redirectTimes = 0;
        private long startAt;

        public DownloadTask(Request request) {
            this.request = request;
            requestUrl = request.getUrl();
        }

        private boolean isCanceled() {
            return request.isCanceled();
        }

        @Override
        public void run() {
            if (isCanceled()) return;
            try {
                startAt = System.currentTimeMillis();
                URL url = new URL(requestUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(connectTimeout);
                connection.setReadTimeout(readTimeout);
                connection.addRequestProperty("User-Agent", "FileDownloader");
                Map<String, String> headers = request.getHeaders();
                for (String key : headers.keySet()) {
                    connection.addRequestProperty(key, headers.get(key));
                }
                if (requestUrl.equals(request.getUrl())) {
                    log("Start >> " + requestUrl);
                    log("Headers = " + headers);
                } else {
                    log("Redirect >> " + requestUrl);
                }
                connection.connect();

                int code = connection.getResponseCode();
                if ((code / 100) == 3) {
                    String redirectUrl = connection.getHeaderField("Location");
                    if (redirectUrl == null || redirectUrl.isEmpty()) {
                        onError(code, "Location is empty");
                    } else if (redirectTimes >= maxRedirectTimes) {
                        onError(code, "Exceed max redirect times");
                    } else {
                        redirectTimes++;
                        requestUrl = redirectUrl;
                        this.run();
                    }
                    return;
                }

                if ((code / 100) != 2) {
                    onError(code, "Request error: " + connection.getResponseMessage());
                    return;
                }

                if (isCanceled()) return;
                long contentLength = getContentLength(connection);
                if (contentLength <= 0) {
                    onError(code, "Content-Length is invalid:" + contentLength);
                    return;
                }

                fetchData(connection.getInputStream(), contentLength);
            } catch (Exception e) {
                StringBuffer sb = new StringBuffer();
                if (requestUrl.equals(request.getUrl())) {
                    sb.append("OriginUrl=").append(request.getUrl()).append(' ')
                      .append("Error=").append(e);
                } else {
                    sb.append("OriginUrl=").append(request.getUrl()).append(' ')
                      .append("RedirectUrl=").append(requestUrl).append(' ')
                      .append("Error=").append(e);
                }
                onError(ErrorCode.CONNECTION_ERROR, sb.toString());
            }
        }

        private long getContentLength(HttpURLConnection connection) {
            String value = connection.getHeaderField("Content-Length");
            try {
                return Long.parseLong(value);
            } catch (Exception ignored) {}
            return -1;
        }

        private void fetchData(InputStream inputStream, long contentLength) {
            OutputStream outputStream = null;
            boolean success = false;
            try {
                outputStream = new FileOutputStream(request.getOutput());
                int readBytes = 0;
                int len;
                byte[] buffer = new byte[1024];
                while (!isCanceled() && (len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                    readBytes += len;
                    onProgress(readBytes * 100f / contentLength);
                }
                outputStream.flush();
                success = !isCanceled();
            } catch (Exception e) {
                onError(ErrorCode.FETCH_DATA_ERROR, e.toString());
            } finally {
                FileUtils.close(inputStream);
                FileUtils.close(outputStream);
            }
            if (success) onComplete(request.getOutput());
            else deleteTempFile();
        }

        private void deleteTempFile() {
            File temp = request.getOutput();
            if (temp.exists()) {
                temp.delete();
            }
        }

        private void onError(int code, String msg) {
            log("Error: " + code + " " + msg);
            log("End << " + request.getUrl());
            DownloadCallback callback = request.getCallback();
            callback.onError(code, msg);
        }

        @SuppressLint("DefaultLocale")
        private void onComplete(File file) {
            long cost = System.currentTimeMillis() - startAt;
            log(String.format("End(%dms) << ", cost) + request.getUrl());
            DownloadCallback callback = request.getCallback();
            callback.onCompleted(file);
        }

        private void onProgress(float percent) {
            DownloadCallback callback = request.getCallback();
            callback.onProgress(percent);
        }
    }
}
