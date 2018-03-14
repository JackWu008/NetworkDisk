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
import com.lzy.okserver.upload.UploadListener;


public class LogUploadListener<T> extends UploadListener<T> {
    String TAG = "nhnhnhnhgrtgefa";

    public LogUploadListener() {
        super("LogUploadListener");
    }

    @Override
    public void onStart(Progress progress) {
        Log.i(TAG, "onStart: ");

    }

    @Override
    public void onProgress(Progress progress) {
        Log.i(TAG, "onProgress: " + progress);

    }

    @Override
    public void onError(Progress progress) {
        Log.i(TAG, "onError: ");
        Log.i(TAG, "onError: " + progress.fileName);
        Log.i(TAG, "onError: " + progress.url);
        Log.i(TAG, "onError: dfd" + progress.exception);

        progress.exception.printStackTrace();
    }

    @Override
    public void onFinish(T t, Progress progress) {
        Log.i(TAG, "onFinish: ");

    }

    @Override
    public void onRemove(Progress progress) {
        Log.i(TAG, "onRemove: ");

    }
}
