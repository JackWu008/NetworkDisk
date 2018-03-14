/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.lzzy.networkdisk.log;

import android.util.Log;

import com.lzy.okgo.model.Progress;
import com.lzy.okserver.download.DownloadListener;

import net.lzzy.networkdisk.utils.ApkUtils;
import net.lzzy.networkdisk.utils.ToastUtils;

import java.io.File;


public class LogDownloadListener extends DownloadListener {
    private String TAG = "LogDownloadListener";

    public LogDownloadListener() {
        super("LogDownloadListener");
    }

    @Override
    public void onStart(Progress progress) {
        Log.i(TAG, "onStart: "+progress);

    }

    @Override
    public void onProgress(Progress progress) {
        Log.i(TAG, "onProgress: "+progress);
        Log.i(TAG, "onProgress: "+progress.url);
    }

    @Override
    public void onError(Progress progress) {
        Log.i(TAG, "onError: "+progress);
        progress.exception.printStackTrace();

    }

    @Override
    public void onFinish(File file, Progress progress) {
        if (progress.url.equals("http://download.fir.im/v2/app/install/5a982278ca87a84f853b4861?download_token=a3d9627458106b75af4d8b7fd9adb6e7&source=update")) {
            ApkUtils.install(new File(progress.filePath));
        }
        Log.i(TAG, "onFinish: "+progress.filePath);
    }

    @Override
    public void onRemove(Progress progress) {
        Log.i(TAG, "onRemove: "+progress);
    }
}
