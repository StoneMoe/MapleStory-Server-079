package api;

import java.util.HashMap;
import java.util.Map;

public class Tools {
    /**
     * 将查询字符串转换为键值对映射
     * @param query 查询字符串
     * @return 映射表
     */
    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
        }
        return result;
    }
}
