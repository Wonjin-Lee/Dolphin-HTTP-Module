package com.wonjin.dolphin.util;

import java.util.Iterator;
import java.util.Map;

public class StringUtil {
    public static String mapToHttpString(Map<String, String> map) {
        if (map == null || map.size() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        Iterator<String> iterator = map.keySet().iterator();

        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key);

            result.append(key + "=" + value + "&");
        }

        result.delete(result.length() - 1, result.length());

        return result.toString();
    }
}
