package com.example.injexpro;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.*;
import java.util.ArrayList;

public class TrendsFragment extends Fragment {

    private static final String TAG = "TrendsFragment";

    // Declare LineChart objects for each plot
    private LineChart trendChart, productionChart, powerChart;
    private LineChart temperatureZonesChart;
    private LineChart heatExchangeChart;
    private LineChart clampingChart;

    // Reference to the Firebase Realtime Database
    private DatabaseReference databaseRef;

    // Progress Bar
    private ProgressBar progressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trends, container, false);

        // Initialize charts (existing and additional)
        trendChart = view.findViewById(R.id.trendChart);
        productionChart = view.findViewById(R.id.productionChart);
        powerChart = view.findViewById(R.id.powerChart);
        temperatureZonesChart = view.findViewById(R.id.temperatureZonesChart);
        heatExchangeChart = view.findViewById(R.id.heatExchangeChart);
        clampingChart = view.findViewById(R.id.clampingChart);


        // Progress Bar
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Firebase reference
        databaseRef = FirebaseDatabase.getInstance().getReference("sensor_data");

        // Setup each chart with appropriate parameters
        setupChart(trendChart, "Ambient Temperature", 20, 40);
        setupChart(productionChart, "Production Rate", 0, 300);
        setupChart(powerChart, "Power Consumption", 0, 5000);
        setupChart(temperatureZonesChart, "Temperature Zones", 100, 150);
        setupChart(heatExchangeChart, "Mold Temperature Difference", -10, 10);
        setupChart(clampingChart, "Clamping Position", 0, 100);

        // Fetch data for each plot
        fetchTemperatureData();
        fetchProductionData();
        fetchPowerData();
        fetchTemperatureZonesData();
        fetchHeatExchangeData();
        fetchClampingData();

        return view;
    }

    // General method to set up a chart
    private void setupChart(LineChart chart, String description, float yMin, float yMax) {
        chart.getDescription().setText(description);
        chart.getDescription().setTextSize(12f);
        chart.setBackgroundColor(android.graphics.Color.argb(150, 255, 255, 255)); // Slightly transparent white
        chart.setDrawGridBackground(true);
        chart.setGridBackgroundColor(android.graphics.Color.argb(180, 255, 255, 255));
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(yMin);
        leftAxis.setAxisMaximum(yMax);
        leftAxis.setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.setDrawBorders(true);
    }

    // Fetch Ambient Temperature Data
    private void fetchTemperatureData() {
        progressBar.setVisibility(View.VISIBLE); // Show loading
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double temperature = snapshot.child("ambient_temperature").getValue(Double.class);
                    if (temperature != null) {
                        entries.add(new Entry(index++, temperature.floatValue()));
                    }
                }
                updateChart(trendChart, entries, "Ambient Temperature");
                progressBar.setVisibility(View.GONE); // Hide loading after data is loaded
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE); // Hide loading even if there's an error
            }
        });
    }

    // Fetch Production Data
    private void fetchProductionData() {
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long uptime = snapshot.child("production_data/uptime").getValue(Long.class);
                    Long count = snapshot.child("production_data/production_count").getValue(Long.class);
                    if (uptime != null && count != null) {
                        entries.add(new Entry(uptime.floatValue(), count.floatValue()));
                    }
                }
                updateChart(productionChart, entries, "Production Rate");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Fetch Power Consumption Data
    private void fetchPowerData() {
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long uptime = snapshot.child("production_data/uptime").getValue(Long.class);
                    Double power = snapshot.child("power_monitoring/overall_power_consumption").getValue(Double.class);
                    if (uptime != null && power != null) {
                        entries.add(new Entry(uptime.floatValue(), power.floatValue()));
                    }
                }
                updateChart(powerChart, entries, "Power Consumption");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // fetchTemperatureZonesData
    private void fetchTemperatureZonesData() {
        // Fetch data from Firebase and update the chart
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double zone1 = snapshot.child("temperature_zones/zone_1").getValue(Double.class);
                    Double zone2 = snapshot.child("temperature_zones/zone_2").getValue(Double.class);
                    Double zone3 = snapshot.child("temperature_zones/zone_3").getValue(Double.class);
                    Double zone4 = snapshot.child("temperature_zones/zone_4").getValue(Double.class);
                    if (zone1 != null && zone2 != null && zone3 != null && zone4 != null) {
                        entries.add(new Entry(index++, (float) (zone1 + zone2 + zone3 + zone4) / 4));
                    }
                }
                updateChart(temperatureZonesChart, entries, "Temperature Zones");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // fetchHeatExchangeData
    private void fetchHeatExchangeData() {
        // Fetch data from Firebase and update the chart
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double temperature1 = snapshot.child("machine_heat_exchange/outlet_temperature").getValue(Double.class);
                    Double temperature2 = snapshot.child("machine_heat_exchange/inlet_temperature").getValue(Double.class);
                    if (temperature1 != null && temperature2 != null) {
                        entries.add(new Entry(index++, (float) (temperature1 - temperature2)));
                    }
                }
                updateChart(heatExchangeChart, entries, "Mold Temperature Difference");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error (optional)
            }
        });
    }

    // fetchClampingData
    private void fetchClampingData() {
        // Fetch data from Firebase and update the chart
        databaseRef.orderByChild("timestamp").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long uptime = snapshot.child("production_data/uptime").getValue(Long.class);
                    Double clamping = snapshot.child("position_sensors/clamping_device_position").getValue(Double.class);
                    if (uptime != null && clamping != null) {
                        entries.add(new Entry(uptime.floatValue(), clamping.floatValue()));
                    }
                }
                updateChart(clampingChart, entries, "Clamping Position");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    // Helper method to update any chart with data entries
    private void updateChart(LineChart chart, ArrayList<Entry> entries, String label) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawCircles(false); // Optional: Hide data point markers
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate(); // Refresh the chart
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove all event listeners to prevent memory leaks
    }
}