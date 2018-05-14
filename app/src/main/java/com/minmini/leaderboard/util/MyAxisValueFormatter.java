package com.minmini.leaderboard.util;


import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;

public class MyAxisValueFormatter implements IAxisValueFormatter, LogMessage {
    private DecimalFormat mFormat;

    public MyAxisValueFormatter() {
        mFormat = new DecimalFormat("0.0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        if (value == 0.0) {
            return 0 + " %";
        } else {
            return mFormat.format(value) + " %";
        }
    }
}
