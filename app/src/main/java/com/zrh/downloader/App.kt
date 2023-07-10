package com.zrh.downloader

import android.app.Application

/**
 *
 * @author zrh
 * @date 2023/7/10
 *
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        FileDownloader.init(this)
    }
}