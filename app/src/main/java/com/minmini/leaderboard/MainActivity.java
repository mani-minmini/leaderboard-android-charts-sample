package com.minmini.leaderboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
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

public class MainActivity extends Activity implements OnChartValueSelectedListener, LogMessage {

    private PieChart mChart;
    private ArrayList<PieEntry> entries;
    private MultiValueMap<String, Leaderboard> rawData;

    private ScrollView table_layout_bar_chart;
    private LinearLayout table_layouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        table_layouts = findViewById(R.id.table_layouts);
        table_layouts.setVisibility(View.INVISIBLE);
        table_layout_bar_chart = findViewById(R.id.table_layout_bar_chart);

        mChart = findViewById(R.id.chart1);

        Button update_chart = findViewById(R.id.update_chart);
        Button show_bar_chart = findViewById(R.id.show_bar_chart);

        mChart.getDescription().setEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(false);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);
        mChart.setDescription(null);
        mChart.setHoleRadius(0f);
        mChart.setTransparentCircleRadius(0f);

        mChart.setDrawCenterText(false);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(false);
        mChart.setHighlightPerTapEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);
        dataPrepare();
        update_chart.setOnClickListener(view -> dataPrepare());

        show_bar_chart.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BarChartActivity.class)));

        Legend l = mChart.getLegend();
        l.setEnabled(false);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(18f);
    }

    private void dataPrepare() {
        JsonArrayRequest request = new JsonArrayRequest(LEADERBOARD_URL,
                response -> {
                    if (response == null) {
                        Toast.makeText(getApplicationContext(), "Couldn't fetch the menu! Pleas try again.", Toast.LENGTH_LONG).show();
                    }else{
                        setData(response);
                    }
                }, error -> {
//            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            String jsonData = LeaderboardUtil.AssetJSONFile("data.json", getApplicationContext());
            try {
                setData(new JSONArray(jsonData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showToast(Object o) {
        Toast.makeText(getApplicationContext(), String.valueOf(o), Toast.LENGTH_LONG).show();
    }

    private void setData(JSONArray response) {
        int count;
        entries = new ArrayList<>();
        rawData = new MultiValueMap<>();
        for(int i=0; i < response.length(); i++) {
            try {
                JSONObject jsonObject = response.getJSONObject(i);
                rawData.putValue(jsonObject.getString("player_name"), new Leaderboard(jsonObject.getString("player_name"), jsonObject.getString("course"), jsonObject.getString("score"), jsonObject.getString("activity_date")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Float> board = new HashMap<>();
        for (Map.Entry<String, Set<Leaderboard>> entry: rawData.getAllValues()){
            float score = 0;
            for (Leaderboard leaderboard : entry.getValue()) {
                score += Float.parseFloat(leaderboard.getScore());
            }
            board.put(entry.getKey(), score / entry.getValue().size());
        }
        MyComparator comp=new MyComparator(board);
        Map<String,Float> newMap = new TreeMap<String,Float>(comp);
        newMap.putAll(board);
        showLog(newMap);
        count = 0;
        for (Map.Entry<String, Float> entry : newMap.entrySet()) {
            String key = entry.getKey();
            Float value = entry.getValue();
            if(count < 5){
                entries.add(new PieEntry(value, key, getResources().getDrawable(R.drawable.ic_launcher_background)));
            }
            count++;
        }
        PieDataSet dataSet = new PieDataSet(entries, null);

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

//        ArrayList<Integer> colors = new ArrayList<>();
//
//        for (int c : ColorTemplate.VORDIPLOM_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.JOYFUL_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.COLORFUL_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.LIBERTY_COLORS)
//            colors.add(c);
//
//        for (int c : ColorTemplate.PASTEL_COLORS)
//            colors.add(c);
//
//        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
        mChart.setVisibility(View.VISIBLE);
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
    @SuppressLint("SimpleDateFormat")
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
        PieEntry pieEntry = entries.get((int) Float.parseFloat(String.valueOf(h.getX())));
        ArrayList<String> stringArrayList  = new ArrayList<>();

        table_layout_bar_chart.removeAllViewsInLayout();
        TableLayout tableLayout = new TableLayout(this);

        for (Leaderboard vals: rawData.getValues(pieEntry.getLabel())){
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
        Log.i("PieChart", "nothing selected");
    }

}