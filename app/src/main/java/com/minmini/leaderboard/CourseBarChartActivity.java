package com.minmini.leaderboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.minmini.leaderboard.model.Leaderboard;
import com.minmini.leaderboard.util.LeaderboardUtil;
import com.minmini.leaderboard.util.LogMessage;
import com.minmini.leaderboard.util.MultiValueMap;
import com.minmini.leaderboard.util.MyAxisValueFormatter;
import com.minmini.leaderboard.util.MyComparator;
import com.minmini.leaderboard.util.MyMarkerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class CourseBarChartActivity extends Activity implements OnChartValueSelectedListener, LogMessage {


    private BarChart mChart;
    private ArrayList<String> players;
    private MultiValueMap<String, Leaderboard> rawData;
    private ScrollView table_layout_bar_chart;
    private LinearLayout table_layouts;
    private DecimalFormat mFormat;

    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_bar_chart);

        String player_name = Objects.requireNonNull(getIntent().getExtras()).getString("player_name");
        response = Objects.requireNonNull(getIntent().getExtras()).getString("response");

        table_layouts = findViewById(R.id.table_layouts);
        table_layouts.setVisibility(View.INVISIBLE);
        table_layout_bar_chart = findViewById(R.id.table_layout_bar_chart);
        mFormat = new DecimalFormat("0.0");


        mChart = findViewById(R.id.chart2);
        mChart.getDescription().setEnabled(false);
        mChart.setFitBars(true);
        mChart.setDoubleTapToZoomEnabled(false);
        mChart.setHighlightFullBarEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);
        mChart.setHighlightPerTapEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(false);
        mChart.setLogEnabled(false);
        mChart.setDrawBorders(false);

        mChart.setOnChartValueSelectedListener(this);
        mChart.setMarker(new MyMarkerView(LeaderboardUtil.getContext(), R.layout.tool_tip));

        try {
            setData(new JSONArray(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void setData(JSONArray response) {

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        int count;
        ArrayList<BarEntry> entries = new ArrayList<>();
        players = new ArrayList<>();
        rawData = new MultiValueMap<>();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject jsonObject = response.getJSONObject(i);
                rawData.putValue(jsonObject.getString("player_name"), new Leaderboard(jsonObject.getString("player_name"), jsonObject.getString("course"), jsonObject.getString("score"), jsonObject.getString("activity_date")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Float> board = new HashMap<>();
        for (Map.Entry<String, Set<Leaderboard>> entry : rawData.getAllValues()) {
            float score = 0;
            for (Leaderboard leaderboard : entry.getValue()) {
                score += Float.parseFloat(leaderboard.getScore());
            }
            board.put(entry.getKey(), score / entry.getValue().size());
        }
        MyComparator comp = new MyComparator(board);
        Map<String, Float> newMap = new TreeMap<String, Float>(comp);
        newMap.putAll(board);
        showLog(newMap);
        count = 0;
        for (Map.Entry<String, Float> entry : newMap.entrySet()) {
            String key = entry.getKey();
            Float value = entry.getValue();
            if (count < 5) {
                entries.add(new BarEntry(count, Float.parseFloat(mFormat.format((double) value)), getResources().getDrawable(R.drawable.ic_launcher_background)));
                players.add(key);
            }
            count++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Leaderboard By Course per Player");
        dataSet.setDrawIcons(false);

        dataSet.setIconsOffset(new MPPointF(0, 0));

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        dataSet.setColors(colors);

//        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        dataSet.setHighLightAlpha(0);

        dataSet.setDrawIcons(false);
        BarData data = new BarData(dataSet);
        dataSet.setDrawIcons(false);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);
        mChart.setData(data);
        mChart.highlightValues(null);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setEnabled(false);

        IAxisValueFormatter custom = new MyAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setLabelCount(5, true);
        leftAxis.setValueFormatter(custom);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setEnabled(false);

        mChart.setVisibility(View.VISIBLE);

        for (IDataSet<BarEntry> set : mChart.getData().getDataSets()) {
            set.setDrawValues(false);
        }

        mChart.invalidate();
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
        table_layout_bar_chart.removeAllViewsInLayout();
        TableLayout tableLayout = new TableLayout(this);
        for (Leaderboard vals : rawData.getValues(players.get((int) h.getX()))) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            Date date;
            try {
                TableRow tableRow = new TableRow(this);
                tableRow.setGravity(Gravity.CENTER);
                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                TextView textView = new TextView(this);
                textView.setBackgroundColor(Color.WHITE);
                textView.setText(vals.getPlayer_name());
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(new TableRow.LayoutParams(0));
                textView.setLayoutParams(new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);

                textView = new TextView(this);
                textView.setText(vals.getCourse());
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.WHITE);
                textView.setLayoutParams(new TableRow.LayoutParams(1));
                textView.setLayoutParams(new TableRow.LayoutParams(100, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);

                textView = new TextView(this);
                textView.setText(vals.getScore());
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.WHITE);
                textView.setLayoutParams(new TableRow.LayoutParams(2));
                textView.setLayoutParams(new TableRow.LayoutParams(200, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);

                date = dateFormat.parse(vals.getActivity_date());
                textView = new TextView(this);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.WHITE);
                textView.setText(desiredDateFormat.format(date));
                textView.setLayoutParams(new TableRow.LayoutParams(3));
                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);

                tableLayout.addView(tableRow);
                tableLayout.setTag(vals.getPlayer_name());
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        table_layout_bar_chart.addView(tableLayout);
        table_layout_bar_chart.setVisibility(View.VISIBLE);
        table_layouts.setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected() {

    }
}
