package com.minmini.leaderboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class MainActivity extends Activity implements OnChartValueSelectedListener, LogMessage {

    private PieChart mChart;
    private ListView player_details;
    private ArrayList<PieEntry> entries;
    private MultiValueMap<String, Leaderboard> rawData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChart = findViewById(R.id.chart1);
        player_details = findViewById(R.id.player_details);
        player_details.setDivider(null);
        player_details.setDividerHeight(0);
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

        Legend l = mChart.getLegend();
        l.setEnabled(false);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);
        mChart.setEntryLabelTextSize(18f);
    }

    private void dataPrepare() {
        JsonArrayRequest request = new JsonArrayRequest("http://192.168.1.38:3000/api/v1/leaderboards",
                response -> {
                    if (response == null) {
                        Toast.makeText(getApplicationContext(), "Couldn't fetch the menu! Pleas try again.", Toast.LENGTH_LONG).show();
                    }else{
                        setData(response);
                        mChart.setVisibility(View.VISIBLE);
                    }
                }, error -> Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show());
        MyApplication.getInstance().addToRequestQueue(request);
    }

    private void showData(ArrayList<String> data){
        ArrayAdapter adapter = new ArrayAdapter<>(this,
                R.layout.activity_listview, data);
        player_details.setAdapter(adapter);
        player_details.setVisibility(View.VISIBLE);
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

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
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
        for (Leaderboard vals: rawData.getValues(pieEntry.getLabel())){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            SimpleDateFormat desiredDateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
            Date date;
            try {
                date = dateFormat.parse(vals.getActivity_date());
                String s = "Name: " + vals.getPlayer_name() + ", Course: " + vals.getCourse() + ", Score: " + vals.getScore() + ", Activity Date: " + desiredDateFormat.format(date);
                stringArrayList.add(s);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        showData(stringArrayList);
    }

    @Override
    public void onNothingSelected() {
        Log.i("PieChart", "nothing selected");
    }

}