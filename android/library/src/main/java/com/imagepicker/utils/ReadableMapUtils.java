package com.imagepicker.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by rusfearuth on 22.02.17.
 */

public class ReadableMapUtils {
    public static
    @NonNull
    boolean hasAndNotEmpty(@NonNull Class clazz,
                           @NonNull final JSONObject target,
                           @NonNull final String key) {
        if (!target.containsKey(key)) {
            return false;
        }

        if (target.get(key) == null) {
            return false;
        }

        if (String.class.equals(clazz)) {
            final String value = target.getString(key);
            return !TextUtils.isEmpty(value);
        }

        return true;
    }


    public static
    @NonNull
    boolean hasAndNotNullReadableMap(@NonNull final JSONObject target,
                                     @NonNull final String key) {
        return hasAndNotEmpty(JSONObject.class, target, key);
    }


    public static
    @NonNull
    boolean hasAndNotEmptyString(@NonNull final JSONObject target,
                                 @NonNull final String key) {
        return hasAndNotEmpty(String.class, target, key);
    }
}
