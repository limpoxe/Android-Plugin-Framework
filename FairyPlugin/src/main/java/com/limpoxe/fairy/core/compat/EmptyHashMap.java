package com.limpoxe.fairy.core.compat;

import java.util.HashMap;

public class EmptyHashMap<K, V> extends HashMap<K, V> {

    @Override
    public V put(K key, V value) {
        //不缓存
        return super.put(key, null);
    }

}
