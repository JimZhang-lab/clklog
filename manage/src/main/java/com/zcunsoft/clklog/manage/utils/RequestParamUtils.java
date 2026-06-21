package com.zcunsoft.clklog.manage.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class RequestParamUtils {

    private RequestParamUtils() {
    }

    public static String getString(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.length() == 0 ? null : text;
    }

    public static int getInt(Map<String, Object> params, String key, int defaultValue) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(Map<String, Object> params, String key, boolean defaultValue) {
        Object value = params == null ? null : params.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public static List<String> getStringList(Map<String, Object> params, String key) {
        Object value = params == null ? null : params.get(key);
        List<String> result = new ArrayList<>();
        if (value instanceof Iterable) {
            for (Object item : (Iterable<?>) value) {
                if (item != null && String.valueOf(item).trim().length() > 0) {
                    result.add(String.valueOf(item).trim());
                }
            }
        } else if (value != null && String.valueOf(value).trim().length() > 0) {
            result.add(String.valueOf(value).trim());
        }
        return result;
    }
}
