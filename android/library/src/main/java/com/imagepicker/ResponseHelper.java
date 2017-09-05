package com.imagepicker;

import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;

/**
 * Created by rusfearuth on 24.02.17.
 */

public class ResponseHelper {
    private JSONObject response = new JSONObject();

    public void cleanResponse() {
        response = new JSONObject();
    }

    public
    @NonNull
    JSONObject getResponse() {
        return response;
    }

    public void putString(@NonNull final String key,
                          @NonNull final String value) {
        response.put(key, value);
    }

    public void putInt(@NonNull final String key,
                       final int value) {
        response.put(key, value);
    }

    public void putBoolean(@NonNull final String key,
                           final boolean value) {
        response.put(key, value);
    }

    public void putDouble(@NonNull final String key,
                          final double value) {
        response.put(key, value);
    }

    public void invokeCustomButton(@NonNull final JSCallback callback,
                                   @NonNull final String action) {
        cleanResponse();
        response.put("customButton", action);
        invokeResponse(callback);
    }

    public void invokeCancel(@NonNull final JSCallback callback) {
        cleanResponse();
        response.put("didCancel", true);
        invokeResponse(callback);
    }

    public void invokeError(@NonNull final JSCallback callback,
                            @NonNull final String error) {
        cleanResponse();
        response.put("error", error);
        invokeResponse(callback);
    }

    public void invokeResponse(@NonNull final JSCallback callback) {
        callback.invoke(response);
    }
}
