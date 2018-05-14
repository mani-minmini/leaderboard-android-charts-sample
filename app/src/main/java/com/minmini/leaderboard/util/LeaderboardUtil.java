package com.minmini.leaderboard.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

import com.minmini.leaderboard.BarChartActivity;
import com.minmini.leaderboard.MainActivity;

import java.io.IOException;
import java.io.InputStream;

public class LeaderboardUtil implements LogMessage {

    public static final String LEADERBOARD_URL = "http://192.168.1.38:3000/api/v1/leaderboards";

    private static Intent intentPieChart;

    private static Intent intentBarChart;

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getContext() {
        return LeaderboardUtil.context;
    }

    public static void setContext(Context context) {
        LeaderboardUtil.context = context;
    }

    public static Intent getIntentPeiChart() {
        if (intentPieChart == null) {
            intentPieChart = new Intent(LeaderboardUtil.context, MainActivity.class);
            System.out.println("My Log: " + "Pie Chart Instance Created");
        }
        return intentPieChart;
    }

    public static Intent getIntentBarChart() {
        if (intentBarChart == null) {
            intentBarChart = new Intent(LeaderboardUtil.context, BarChartActivity.class);
            System.out.println("My Log: " + "Bar Chart Instance Created");
        }
        return intentBarChart;
    }

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
