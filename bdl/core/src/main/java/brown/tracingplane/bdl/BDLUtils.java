package brown.tracingplane.bdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.StringUtils;

public class BDLUtils {
    
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
    
    /**
     * Equality comparison, using the provided default value if a or b are null
     * @param a an object instance, possibly null
     * @param b an object instance, possibly null
     * @param defaultValue the default value to use for a or b if they happen to be null
     * @return true if <code>a</code> and <code>b</code> are equal
     */
    public static <T> boolean equals(T a, T b, T defaultValue) {
        if (a == null && b == null) {
            return true;
        } else if (a == null) {
            return b.equals(defaultValue);
        } else if (b == null) {
            return a.equals(defaultValue);
        } else {
            return a.equals(b);
        }
    }
    
    // This is a hack to add 'compaction' joins without deploying them to all the interfaces.
    // At this point in time it's still a question of whether compact joins are useful
    public static final ThreadLocal<Boolean> is_compaction = new ThreadLocal<Boolean>() {
        @Override public Boolean initialValue() {
            return false;
        }
    };

}
