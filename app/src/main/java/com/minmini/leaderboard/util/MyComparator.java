package com.minmini.leaderboard.util;

import java.util.Comparator;
import java.util.Map;

public class MyComparator implements Comparator {

    private Map<String, Float> map;

    public MyComparator(Map<String, Float> map) {
        this.map = map;
    }

    public int compare(Object o1, Object o2) {
        return (map.get(o2)).compareTo(map.get(o1));
    }
}
