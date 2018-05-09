package com.minmini.leaderboard.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MultiValueMap<K,V>
{
    private final Map<K,Set<V>> mappings = new HashMap<>();

    public Set<V> getValues(K key)
    {
        return mappings.get(key);
    }

    public Set<Map.Entry<K, Set<V>>> getAllValues()
    {
        return mappings.entrySet();
    }

    public void putValue(K key, V value)
    {
        Set<V> target = mappings.get(key);

        if(target == null)
        {
            target = new HashSet<V>();
            mappings.put(key,target);
        }

        target.add(value);
    }

}