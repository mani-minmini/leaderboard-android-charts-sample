package com.minmini.leaderboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonArrayRequest;
import com.github.mikephil.charting.charts.BarChart;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class BarChartActivity extends Activity implements OnChartValueSelectedListener, LogMessage {

    private BarChart mChart;
    private ListView player_details;
    private ArrayList<BarEntry> entries;
    private MultiValueMap<String, Leaderboard> rawData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        Button update_chart = findViewById(R.id.update_chart);
        Button show_pie_chart = findViewById(R.id.show_pie_chart);

        player_details = findViewById(R.id.player_details);
        player_details.setDivider(null);
        player_details.setDividerHeight(0);

        mChart = findViewById(R.id.chart2);
        mChart.getDescription().setEnabled(false);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setHighlightPerTapEnabled(true);

        mChart.setOnChartValueSelectedListener(this);
        dataPrepare();
        update_chart.setOnClickListener(view -> dataPrepare());
        show_pie_chart.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MainActivity.class)));
    }

    private void dataPrepare() {
        mChart.invalidate();
        Legend l = mChart.getLegend();
        l.setEnabled(false);
        JsonArrayRequest request = new JsonArrayRequest("http://192.168.1.38:3000/api/v1/leaderboards",
                response -> {
                    if (response == null) {
                        Toast.makeText(getApplicationContext(), "Couldn't fetch the menu! Pleas try again.", Toast.LENGTH_LONG).show();
                    } else {
                        setData(response);
                    }
                }, error -> {
            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            String jsonData = LeaderboardUtil.AssetJSONFile("data.json", getApplicationContext());
            try {
                setData(new JSONArray(jsonData));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showData(ArrayList<String> data) {
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, data);
        player_details.setAdapter(adapter);
        player_details.setVisibility(View.VISIBLE);
    }

    private void showToast(Object o) {
        Toast.makeText(getApplicationContext(), String.valueOf(o), Toast.LENGTH_LONG).show();
    }

    private void setData(JSONArray response) {
        int count;
        entries = new ArrayList<>();
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
            }
            count++;
        }
        BarDataSet dataSet = new BarDataSet(entries, "Data");

        dataSet.setDrawIcons(false);

        dataSet.setIconsOffset(new MPPointF(0, 0));

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);


        BarData data = new BarData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
        mChart.setVisibility(View.VISIBLE);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        Log.i("VAL SELECTED",
                "Value: " + e.getY() + ", index: " + h.getX()
                        + ", DataSet index: " + h.getDataSetIndex());
//        BarEntry barEntry = entries.get((int) Float.parseFloat(String.valueOf(h.getX())));
//        ArrayList<String> stringArrayList  = new ArrayList<>();
//        for (Leaderboard vals: rawData.getValues(barEntry.getY())){
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
//            SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
//            Date date;
//            try {
//                date = dateFormat.parse(vals.getActivity_date());
//                String s = "Name: " + vals.getPlayer_name() + ", Course: " + vals.getCourse() + ", Score: " + vals.getScore() + ", Activity Date: " + desiredDateFormat.format(date);
//                stringArrayList.add(s);
//            } catch (ParseException e1) {
//                e1.printStackTrace();
//            }
//        }
//        showData(stringArrayList);
    }

    @Override
    public void onNothingSelected() {
        Log.i("BarChart", "nothing selected");
    }

    class MyComparator implements Comparator {

        Map<String, Float> map;

        MyComparator(Map<String, Float> map) {
            this.map = map;
        }

        public int compare(Object o1, Object o2) {
            return (map.get(o2)).compareTo(map.get(o1));
        }
    }
}