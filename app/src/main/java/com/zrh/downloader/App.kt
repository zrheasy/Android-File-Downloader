package com.zrh.downloader

import android.app.Application
import android.util.Log

/**
 *
 * @author zrh
 * @date 2023/7/10
 *
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FileDownloader.init(this, DownloadConfig().apply {
            setLogger { Log.d("FileDownloader", it) }
            setHeaderProvider {
                mapOf<String, String>("token" to "hello world!")
            }
        })
    }
}