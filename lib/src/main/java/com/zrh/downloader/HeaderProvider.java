package com.zrh.downloader;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * @author zrh
 * @date 2023/7/11
 */
public interface HeaderProvider {
    @NonNull
    Map<String, String> getHeaders(String url);
}
