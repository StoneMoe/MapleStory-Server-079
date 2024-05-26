package utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtil
{
    public static <T> List<T> copyFirst(final List<T> list, final int count) {
        final List<T> ret = new ArrayList<T>(Math.min(list.size(), count));
        int i = 0;
        for (final T elem : list) {
            ret.add(elem);
            if (i++ > count) {
                break;
            }
        }
        return ret;
    }
}
