package com.minmini.leaderboard;

import android.app.Activity;
import android.os.Bundle;

import java.util.Objects;

public class CourseBarChartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_bar_chart);

        String player_name = Objects.requireNonNull(getIntent().getExtras()).getString("player_name");
    }
}
