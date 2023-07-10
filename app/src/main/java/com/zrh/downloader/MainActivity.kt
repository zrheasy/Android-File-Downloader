package com.zrh.downloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.zrh.downloader.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private val url = "https://pic2.zhimg.com/v2-696b347aa5b02a943706d5de13dc6ec1_r.jpg"
    private lateinit var mBinding: ActivityMainBinding
    private var downloadHandle: DownloadHandle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        mBinding.editUrl.setText(url)
        mBinding.btnDownload.setOnClickListener {
            download()
        }
    }

    private fun download() {
        val url = mBinding.editUrl.text.toString()
        if (url.isEmpty()) {
            toast("请输入下载地址")
            return
        }
        mBinding.tvResult.text = "下载中..."
        mBinding.btnDownload.isEnabled = false
        val cache = if (externalCacheDir != null) externalCacheDir else cacheDir
        val outputDir = File(cache, "test")
        val fileName = getName(url)
        downloadHandle = FileDownloader.download(url, outputDir, fileName, false)
        downloadHandle!!.setCallback(object : DownloadCallback {
            override fun onCompleted(file: File) {
                mBinding.tvResult.text = "下载成功：${file.absoluteFile}"
                mBinding.btnDownload.isEnabled = true
            }

            override fun onError(code: Int, msg: String) {
                mBinding.tvResult.text = "下载失败: $code $msg"
                mBinding.btnDownload.isEnabled = true
            }

            override fun onProgress(percent: Float) {
                mBinding.tvResult.text = "下载中...：${String.format("%.1f", percent)}%"
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadHandle?.stop()
    }

    private fun getName(url: String): String {
        val array = url.split("/")
        if (array.isNotEmpty()) {
            return array[array.size - 1]
        }
        return System.currentTimeMillis().toString()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}