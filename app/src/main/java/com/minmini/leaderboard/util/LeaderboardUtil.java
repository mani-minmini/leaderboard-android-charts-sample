package com.minmini.leaderboard.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

public class LeaderboardUtil {
    public static String AssetJSONFile(String filename, Context context) {
        AssetManager manager = context.getAssets();
        InputStream file = null;
        try {
            file = manager.open(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] formArray = new byte[0];
        try {
            if (file != null) {
                formArray = new byte[file.available()];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (file != null) {
                file.read(formArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (file != null) {
                file.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(formArray);
    }
}
