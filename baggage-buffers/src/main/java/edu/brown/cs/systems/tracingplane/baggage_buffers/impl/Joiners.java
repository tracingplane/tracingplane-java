package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.util.Map;
import java.util.Set;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Joiner;

/**
 * Joiners for built-in types used by compiled baggage buffers classes
 */
public class Joiners {

    private Joiners() {}
    
    public static Joiner<Boolean> or() {
        return new Joiner<Boolean>() {
            public Boolean join(Boolean a, Boolean b) {
                if (a == null) {
                    return b;
                } else if (b == null) {
                    return a;
                } else {
                    return a || b;
                }
            }
        };
    }

    public static <T> Joiner<T> first() {
        return new Joiner<T>() {
            public T join(T a, T b) {
                return a == null ? b : a;
            }
        };
    }

    public static <V> Joiner<Set<V>> setUnion() {
        return new Joiner<Set<V>>() {
            public Set<V> join(Set<V> firstSet, Set<V> secondSet) {
                if (firstSet == null) {
                    return secondSet;
                } else if (secondSet == null) {
                    return firstSet;
                } else {
                    firstSet.addAll(secondSet);
                    return firstSet;
                }
            }
        };
    }

    public static <K, V> Joiner<Map<K, V>> mapMerge(Joiner<V> valueJoiner) {
        return new Joiner<Map<K, V>>() {
            public Map<K, V> join(Map<K, V> firstMap, Map<K, V> secondMap) {
                if (firstMap == null) {
                    return secondMap;
                } else if (secondMap == null) {
                    return firstMap;
                } else {
                    for (K key : secondMap.keySet()) {
                        if (firstMap.containsKey(key)) {
                            firstMap.put(key, valueJoiner.join(firstMap.get(key), secondMap.get(key)));
                        } else {
                            firstMap.put(key, secondMap.get(key));
                        }
                    }
                    return firstMap;
                }
            }
        };
    }

}
