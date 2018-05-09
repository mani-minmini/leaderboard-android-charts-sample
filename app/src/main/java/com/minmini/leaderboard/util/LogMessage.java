package com.minmini.leaderboard.util;

public interface LogMessage {
    default void showLog(Object o)
    {
        System.out.println("My Log: " + String.valueOf(o));
    }
}
