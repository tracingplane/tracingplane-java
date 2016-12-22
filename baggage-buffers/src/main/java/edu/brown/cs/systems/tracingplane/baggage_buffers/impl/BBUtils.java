package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;

public class BBUtils {
    
    public static String toString(Set<?> set) {
        return set == null ? "{ }" : "{ " + StringUtils.join(set, ", ") + " }";
    }
    
    public static <V> String toString(Map<?, V> map, Function<V, String> valueToStringFunction) {
        if (map == null) {
            return "{ }";
        }
        List<String> strings = new ArrayList<>();
        for (Object key : map.keySet()) {
            strings.add(String.valueOf(key) + " = " + map.get(key));
        }
        return formatChild(StringUtils.join(strings, "\n"));
    }
    
    public static String toString(Bag bag) {
        return bag == null ? "{ }" : formatChild(indent(bag.toString()));
    }
    
    public static String formatChild(String s) {
        return StringUtils.join(Lists.newArrayList("{", indent(s), "}"), "\n");
    }
    
    public static String indent(String s) {
        return s.replaceAll("(?m)^", "  ");
    }

}
