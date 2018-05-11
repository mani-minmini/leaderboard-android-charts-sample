package com.minmini.leaderboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.minmini.leaderboard.app.MyApplication;
import com.minmini.leaderboard.model.Leaderboard;
import com.minmini.leaderboard.util.LeaderboardUtil;
import com.minmini.leaderboard.util.LogMessage;
import com.minmini.leaderboard.util.MultiValueMap;
import com.minmini.leaderboard.util.MyMarkerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static com.minmini.leaderboard.util.LeaderboardUtil.LEADERBOARD_URL;

public class BarChartActivity extends Activity implements OnChartValueSelectedListener, LogMessage {

    private BarChart mChart;
    private ArrayList<String> players;
    private MultiValueMap<String, Leaderboard> rawData;
    private RelativeLayout relativeLayout;
    private ScrollView table_layout_bar_chart;
    private LinearLayout table_layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);
        table_layouts = findViewById(R.id.table_layouts);
        table_layouts.setVisibility(View.INVISIBLE);
        relativeLayout = findViewById(R.id.relativeLayout);
        table_layout_bar_chart = findViewById(R.id.table_layout_bar_chart);

        Button update_chart = findViewById(R.id.update_chart);
        Button show_pie_chart = findViewById(R.id.show_pie_chart);

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

//        mChart
        mChart.setOnChartValueSelectedListener(this);

        IMarker marker = new MyMarkerView(LeaderboardUtil.getContext(), R.layout.tool_tip);

        mChart.setMarker(marker);

        dataPrepare();
        update_chart.setOnClickListener(view -> dataPrepare());
        show_pie_chart.setOnClickListener(view -> {
            startActivity(LeaderboardUtil.getIntentPeiChart());
            finish();
        });
    }

    private void dataPrepare() {
        JsonArrayRequest request = new JsonArrayRequest(LEADERBOARD_URL,
                response -> {
                    if (response == null) {
                        Toast.makeText(LeaderboardUtil.getContext(), "Couldn't fetch the menu! Pleas try again.", Toast.LENGTH_LONG).show();
                    } else {
                        setData(response);
                    }
                }, error -> {
//            Toast.makeText(LeaderboardUtil.getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            String jsonData = LeaderboardUtil.AssetJSONFile("data.json", LeaderboardUtil.getContext());
            try {
                setData(new JSONArray(jsonData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showToast(Object o) {
        Toast.makeText(LeaderboardUtil.getContext(), String.valueOf(o), Toast.LENGTH_LONG).show();
    }

    private void showSnackBar(Object o) {
        Snackbar.make(relativeLayout, String.valueOf(o), Snackbar.LENGTH_LONG).show();
    }

    private void setData(JSONArray response) {
        Legend legend = mChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(9f);
        legend.setTextSize(11f);
        legend.setXEntrySpace(4f);
        legend.setYEntrySpace(5f);
        legend.setEnabled(true);

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
        BarChartActivity.MyComparator comp = new BarChartActivity.MyComparator(board);
        Map<String, Float> newMap = new TreeMap<String, Float>(comp);
        newMap.putAll(board);
        showLog(newMap);
        count = 0;
        for (Map.Entry<String, Float> entry : newMap.entrySet()) {
            String key = entry.getKey();
            Float value = entry.getValue();
            if (count < 5) {
                entries.add(new BarEntry(count, value, getResources().getDrawable(R.drawable.ic_launcher_background)));
                players.add(key);
            }
            count++;
        }
        BarDataSet dataSet = new BarDataSet(entries, null);

        dataSet.setDrawIcons(false);

        dataSet.setIconsOffset(new MPPointF(0, 0));

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setHighLightAlpha(0);

        dataSet.setDrawIcons(false);
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);
        mChart.highlightValues(null);

        mChart.invalidate();
        mChart.setVisibility(View.VISIBLE);

        mChart.getAxisLeft().setAxisMaximum(100);
        mChart.getAxisLeft().setAxisMinimum(0);
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
                date = dateFormat.parse(vals.getActivity_date());
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

                textView = new TextView(this);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundColor(Color.WHITE);
                textView.setText(desiredDateFormat.format(date));
                textView.setLayoutParams(new TableRow.LayoutParams(3));
                textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                tableRow.addView(textView);

                tableLayout.addView(tableRow);
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
        Log.i("BarChart", "nothing selected");
    }

    private class MyComparator implements Comparator {

        Map<String, Float> map;

        MyComparator(Map<String, Float> map) {
            this.map = map;
        }

        public int compare(Object o1, Object o2) {
            return (map.get(o2)).compareTo(map.get(o1));
        }
    }

    @Override
    public void onBackPressed() {
    }
}