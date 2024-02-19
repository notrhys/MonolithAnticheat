package me.rhys.anticheat.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class EvictingMap<K, V> extends LinkedHashMap<K, V> {
    private final int size;

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() >= this.size;
    }

    public EvictingMap(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }
}
