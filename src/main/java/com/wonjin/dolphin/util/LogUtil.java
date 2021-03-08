package com.wonjin.dolphin.util;

import org.apache.http.Header;

import java.util.Map;

public class LogUtil {
    public static String mapToLogString(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return "";
        }

        String logText = "";
        for (String key : map.keySet()) {
            logText += key + " : " + map.get(key) + ", ";
        }

        return logText.substring(0, logText.length() - 2);
    }

    public static String headerToLogString(Header[] headers) {
        if (headers == null || headers.length == 0) {
            return "";
        }

        String logText = "";
        for (Header header : headers) {
            logText += header.getName() + " : " + header.getValue() + ", ";
        }

        return logText.substring(0, logText.length() - 2);
    }
}
