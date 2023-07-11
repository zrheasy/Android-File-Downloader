package com.zrh.downloader;

import com.zrh.downloader.engine.DownloadEngine;

/**
 * @author zrh
 * @date 2023/7/11
 */
public class DownloadConfig {
    private HeaderProvider headerProvider;
    private DownloadEngine downloadEngine;
    private Logger logger;

    public HeaderProvider getHeaderProvider() {
        return headerProvider;
    }

    public void setHeaderProvider(HeaderProvider headerProvider) {
        this.headerProvider = headerProvider;
    }

    public DownloadEngine getDownloadEngine() {
        return downloadEngine;
    }

    public void setDownloadEngine(DownloadEngine downloadEngine) {
        this.downloadEngine = downloadEngine;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
