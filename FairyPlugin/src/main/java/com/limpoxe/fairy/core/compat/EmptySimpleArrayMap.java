package com.limpoxe.fairy.core.compat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;

public class EmptySimpleArrayMap<K, V> extends SimpleArrayMap<K, V> {
    @Override
    public V put(K key, V value) {
        //put null
        return super.put(key, null);
    }

    @Nullable
    @Override
    public V putIfAbsent(K key, V value) {
        //put null
        return super.putIfAbsent(key, null);
    }

    @Override
    public void putAll(@NonNull SimpleArrayMap<? extends K, ? extends V> array) {
        //do nothing
    }
}
