package edu.brown.cs.systems.tracingplane.baggage_buffers.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Brancher;

/**
 * Branchers for built-in types used by compiled baggage buffers classes
 */
public class Branchers {

    private Branchers() {}

    /** Copies nothing and reuses the original object */
    public static <T> Brancher<T> noop() {
        return new Brancher<T>() {
            public T branch(T from) {
                return from;
            }
        };
    }
    
    public static <V> Brancher<Set<V>> set() {
        return new Brancher<Set<V>>() {
            public Set<V> branch(Set<V> from) {
                return from == null ? null : new HashSet<V>(from);
            }
        };
    }
    
    public static <K, V> Brancher<Map<K, V>> map(Brancher<V> valueBrancher) {
        return new Brancher<Map<K, V>>() {
            public Map<K, V> branch(Map<K, V> from) {
                if (from == null) {
                    return null;
                }
                Map<K, V> branched = new HashMap<>();
                for (K key : from.keySet()) {
                    branched.put(key, valueBrancher.branch(from.get(key)));
                }
                return branched;
            }
        };
    }

}
