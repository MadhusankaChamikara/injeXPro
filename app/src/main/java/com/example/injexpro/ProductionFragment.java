package com.example.injexpro;

// Import necessary libraries
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.cardview.widget.CardView;
import android.widget.GridLayout;

// Firebase libraries for accessing the Realtime Database
import com.google.firebase.database.*;
// Android libraries for logging and debugging
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// This fragment fetches data from Firebase and displays it on the app's home screen.
public class ProductionFragment extends Fragment {

    // Used for logging debug messages, especially during development and debugging.
    private static final String TAG = "ProductionFragment";

    // Points to the Firebase Realtime Database where sensor data is stored.
    private DatabaseReference databaseRef;

    // A listener that reacts whenever the database data changes.
    private ValueEventListener valueEventListener; // Store reference to listener

    // UI elements
    // GridLayout to display sensor data
//    private GridLayout gridLayout;

    // TextView to display the machine status
    private TextView machineStatusText;
    // TextView to display the last updated timestamp
    private TextView lastUpdatedText;
    // TextView to display ambient temperature
    private TextView ambientTempText;

    // Mold Number Card UI elements
    private TextView moldNumberText;

    // Operation State Card UI elements
    private TextView operationStateText;

    // Machine Uptime Card UI elements
    private TextView machineUptimeText;

    // Production Count Card UI elements
    private TextView productionCountText;

    // Mold Heat Exchange Card UI elements
    private TextView moldHeatExchangeInletText;
    private TextView moldHeatExchangeOutletText;

    // Position Sensors Card UI elements
    private TextView clampingPositionText;
    private TextView ejectorPositionText;


    // Required empty public constructor
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_production, container, false);

        // Get references to the UI elements
//        gridLayout = view.findViewById(R.id.gridLayout);

        // Machine Status Card UI elements
        machineStatusText = view.findViewById(R.id.machineStatusText);
        lastUpdatedText = view.findViewById(R.id.lastUpdatedText);
        ambientTempText = view.findViewById(R.id.environmentTempText);

        // Mold Number Card UI elements
        moldNumberText = view.findViewById(R.id.moldNumberValue);

        // Operation State Card UI elements
        operationStateText = view.findViewById(R.id.operationStateValue);

        // Machine Uptime Card UI elements
        machineUptimeText = view.findViewById(R.id.machineUptimeValue);

        // Production Count Card UI elements
        productionCountText = view.findViewById(R.id.productCountValue);

        // Mold Heat Exchange Card UI elements
        moldHeatExchangeInletText = view.findViewById(R.id.inletMoldTempValue);
        moldHeatExchangeOutletText = view.findViewById(R.id.outletMoldTempValue);

        // Position Sensors Card UI elements
        clampingPositionText = view.findViewById(R.id.clampPositionValue);
        ejectorPositionText = view.findViewById(R.id.injectorPositionValue);


        // Get a reference to the Firebase Realtime Database
        databaseRef = FirebaseDatabase.getInstance().getReference("sensor_data");
        setupFirebaseDataListener();

        // Inflate the layout for this fragment
        return view;
    }

    // Initializes the Firebase listener to retrieve and process the latest sensor data.
    private void setupFirebaseDataListener() {

        // Listen for new sensor data and update the UI accordingly
        valueEventListener = databaseRef.limitToLast(1).addValueEventListener(new ValueEventListener() {

            @Override
            // Triggered when the database detects new or updated sensor data.
            // This method processes the data and updates the UI accordingly.
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Log the raw data snapshot
                try {
                    // Get the last data snapshot
                    DataSnapshot lastDataSnapshot = dataSnapshot.getChildren().iterator().next();
                    Log.d(TAG, "Raw data: " + lastDataSnapshot.getValue());

                    // Clear the grid layout before adding new sensor data
//                    gridLayout.removeAllViews();

                    // Handle all sensor data
                    handleBasicTemperatures(lastDataSnapshot);
                    handleFlowPressureData(lastDataSnapshot);
                    handleHeatExchangeData(lastDataSnapshot);
                    handleMoldTemperatureData(lastDataSnapshot);
                    handlePositionSensorsData(lastDataSnapshot);
                    handlePowerMonitoringData(lastDataSnapshot);
                    handleProductionData(lastDataSnapshot);
                    handleMachineStatus(lastDataSnapshot);
                    handleRpmSensorsData(lastDataSnapshot);
                    handleTemperatureZonesData(lastDataSnapshot);
                    handleVibrationSensorData(lastDataSnapshot);
                    handleTimestamp(lastDataSnapshot);

                    // Update the machine status icon
                } catch (Exception e) {

                    // Log the error and update the UI
                    Log.e(TAG, "Error updating UI", e);

                    // Update the machine status text and color
                    machineStatusText.setText("Data Error");
                    machineStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }

            @Override
            // Called if the database listener fails, usually due to network or permission issues.
            public void onCancelled(DatabaseError error) {

                // Log the error and update the UI
                Log.e(TAG, "Database error: ", error.toException());

                // Update the machine status text and color
                machineStatusText.setText("Connection Error");
                machineStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        });
    }

    // Reads and displays basic temperatures like ambient and oil temperatures.
    private void handleBasicTemperatures(DataSnapshot snapshot) {

        // Get the basic temperature data
        Object ambientTempObj = snapshot.child("ambient_temperature").getValue();
        Double ambientTemp = (ambientTempObj instanceof Number) ? ((Number) ambientTempObj).doubleValue() : null;

        // Get the machine oil temperature data
        Object machineOilTempObj = snapshot.child("machine_oil_temperature").getValue();
        Double machineOilTemp = (machineOilTempObj instanceof Number) ? ((Number) machineOilTempObj).doubleValue() : null;

        // Add the basic temperature data to the grid layout
        addSensorCard("Ambient Temperature", (ambientTemp != null) ? ambientTemp + "°C" : "Data Not Available");
        // Add the machine oil temperature data to the grid layout
        addSensorCard("Machine Oil Temperature", (machineOilTemp != null) ? machineOilTemp + "°C" : "Data Not Available");

        // Update the ambient temperature text
        ambientTempText.setText((ambientTemp != null) ? ambientTemp + "°C" : "Data Not Available");
    }

    // Extracts hydraulic flow and pressure readings and updates the UI.
    private void handleFlowPressureData(DataSnapshot snapshot) {

        // Get the flow and pressure data
        DataSnapshot flowPressureSnapshot = snapshot.child("flow_pressure_sensors");
        Double hydraulicFlow = flowPressureSnapshot.child("hydraulic_flow").getValue(Double.class);
        Double hydraulicPressure = flowPressureSnapshot.child("hydraulic_pressure").getValue(Double.class);

        // Add the flow and pressure data to the grid layout
        addSensorCard("Hydraulic Flow", (hydraulicFlow != null) ? hydraulicFlow + " L/min" : "Data Not Available");
        // Add the hydraulic pressure data to the grid layout
        addSensorCard("Hydraulic Pressure", (hydraulicPressure != null) ? hydraulicPressure + " bar" : "Data Not Available");

    }

    // Reads data from the heat exchange system (inlet and outlet temperatures).
    private void handleHeatExchangeData(DataSnapshot snapshot) {

        // Get the heat exchange data
        DataSnapshot heatExchangeSnapshot = snapshot.child("machine_heat_exchange");
        Double inletTemp = heatExchangeSnapshot.child("inlet_temperature").getValue(Double.class);
        Double outletTemp = heatExchangeSnapshot.child("outlet_temperature").getValue(Double.class);

        // Add the heat exchange data to the grid layout
        addSensorCard("Heat Exchange Inlet", (inletTemp != null) ? inletTemp + "°C" : "Data Not Available");
        addSensorCard("Heat Exchange Outlet", (outletTemp != null) ? outletTemp + "°C" : "Data Not Available");

    }

    // Displays the inlet and outlet temperatures of the mold.
    private void handleMoldTemperatureData(DataSnapshot snapshot) {

        // Get the mold temperature data
        DataSnapshot moldTempSnapshot = snapshot.child("mold_temperature");
        Double inletTemp = moldTempSnapshot.child("inlet_temperature").getValue(Double.class);
        Double outletTemp = moldTempSnapshot.child("outlet_temperature").getValue(Double.class);

        // Add the mold temperature data to the grid layout
        addSensorCard("Mold Inlet Temperature", (inletTemp != null) ? inletTemp + "°C" : "Data Not Available");
        addSensorCard("Mold Outlet Temperature", (outletTemp != null) ? outletTemp + "°C" : "Data Not Available");

        // Update the mold heat exchange text
        moldHeatExchangeInletText.setText((inletTemp != null) ? inletTemp + "°C" : "Data Not Available");
        moldHeatExchangeOutletText.setText((outletTemp != null) ? outletTemp + "°C" : "Data Not Available");
    }

    // Displays the positions of the clamping device, ejector, and injection components.
    private void handlePositionSensorsData(DataSnapshot snapshot) {

        // Get the position sensor data
        DataSnapshot positionSensorsSnapshot = snapshot.child("position_sensors");
        Double clampingPosition = positionSensorsSnapshot.child("clamping_device_position").getValue(Double.class);
        Double injectionPosition = positionSensorsSnapshot.child("injection_position").getValue(Double.class);

        // Add the position sensor data to the grid layout
        addSensorCard("Clamping Position", (clampingPosition != null) ? clampingPosition + " mm" : "Data Not Available");
        addSensorCard("Injection Position", (injectionPosition != null) ? injectionPosition + " mm" : "Data Not Available");

        // Update the position sensor text
        clampingPositionText.setText((clampingPosition != null) ? clampingPosition + " mm" : "Data Not Available");
        ejectorPositionText.setText((injectionPosition != null) ? injectionPosition + " mm" : "Data Not Available");
    }

    // Extracts power consumption and phase currents, then updates the UI.
    private void handlePowerMonitoringData(DataSnapshot snapshot) {

        // Get the power monitoring data
        DataSnapshot powerMonitoringSnapshot = snapshot.child("power_monitoring");
        Double powerConsumption = powerMonitoringSnapshot.child("overall_power_consumption").getValue(Double.class);

        // Get the individual phase current data
        DataSnapshot phaseCurrentSnapshot = powerMonitoringSnapshot.child("individual_phase_current");
        Double phase1 = phaseCurrentSnapshot.child("phase_1").getValue(Double.class);
        Double phase2 = phaseCurrentSnapshot.child("phase_2").getValue(Double.class);
        Double phase3 = phaseCurrentSnapshot.child("phase_3").getValue(Double.class);

        // Add the power monitoring data to the grid layout
        addSensorCard("Power Consumption", (powerConsumption != null) ? String.format("%.1f kW", powerConsumption/1000) : "Data Not Available");
        addSensorCard("Phase 1 Current", (phase1 != null) ? phase1 + " A" : "Data Not Available");
        addSensorCard("Phase 2 Current", (phase2 != null) ? phase2 + " A" : "Data Not Available");
        addSensorCard("Phase 3 Current", (phase3 != null) ? phase3 + " A" : "Data Not Available");

    }

    // Displays production metrics such as mold number, state, and uptime.
    private void handleProductionData(DataSnapshot snapshot) {

        // Get the production data
        DataSnapshot productionDataSnapshot = snapshot.child("production_data");

        // Get the mold number, operation state, production count, and uptime
        Object moldNumberObj = productionDataSnapshot.child("mold_number").getValue();
        Long moldNumber = (moldNumberObj instanceof Number) ? ((Number) moldNumberObj).longValue() : null;
        Object operationStateObj = productionDataSnapshot.child("operation_state_number").getValue();
        Long operationState = (operationStateObj instanceof Number) ? ((Number) operationStateObj).longValue() : null;
        Object productionCountObj = productionDataSnapshot.child("production_count").getValue();
        Long productionCount = (productionCountObj instanceof Number) ? ((Number) productionCountObj).longValue() : null;
        Long uptime = productionDataSnapshot.child("uptime").getValue(Long.class);

        // Add the production data to the grid layout
        addSensorCard("Mold Number", (moldNumber != null) ? "Mold #" + moldNumber : "Data Not Available");
        addSensorCard("Operation State", (operationState != null) ? "State " + operationState : "Data Not Available");
        addSensorCard("Production Count", (productionCount != null) ? productionCount + " parts" : "Data Not Available");
        addSensorCard("Machine Uptime", (uptime != null) ? formatUptime(uptime) : "Data Not Available");

        // Update the mold number text
        moldNumberText.setText((moldNumber != null) ? "Mold #" + moldNumber : "Data Not Available");

        // Update the operation state text
        operationStateText.setText((operationState != null) ? "State " + operationState : "Data Not Available");

        // Update the machine uptime text
        machineUptimeText.setText((uptime != null) ? formatUptime(uptime) : "Data Not Available");

        // Update the production count text
        productionCountText.setText((productionCount != null) ? productionCount + " parts" : "Data Not Available");
    }

    // Updates the UI with the machine's operational status (e.g., online/offline or online-auto).
    private void handleMachineStatus(DataSnapshot snapshot) {

        // Get the machine status data
        DataSnapshot machineStatusSnapshot = snapshot.child("machine_status");

        // Get the machine ID, state, motor state, and last data entry timestamp
        Object machineIdObj = machineStatusSnapshot.child("machine_id").getValue();
        String machineId = (machineIdObj != null) ? String.valueOf(machineIdObj) : "Unknown";
        Boolean machineState = machineStatusSnapshot.child("machine_on_off_state").getValue(Boolean.class);
        Boolean motorState = machineStatusSnapshot.child("motor_on_off_state").getValue(Boolean.class);
        String lastDataEntryString = machineStatusSnapshot.child("last_data_entry").getValue(String.class);

        // Parse the ISO 8601 timestamp
        boolean isOnline = false;
        if (lastDataEntryString != null) {
            try {
                // Parse the timestamp in UTC format
                SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date lastDataEntryDate = iso8601Format.parse(lastDataEntryString);

                if (lastDataEntryDate != null) {
                    // Calculate the time difference
                    long timeDifference = System.currentTimeMillis() - lastDataEntryDate.getTime();

                    // Log for debugging
                    Log.d("TimeCheck", "Time Difference (ms): " + timeDifference);
                    Log.d("TimeCheck", "Current Time (Millis): " + System.currentTimeMillis());
                    Log.d("TimeCheck", "Last Data Entry Time (Millis): " + lastDataEntryDate.getTime());

                    // Check if the machine is online (15-minute threshold)
                    isOnline = timeDifference < 30 * 60 * 1000; // 15 minutes
                }
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("TimeCheck", "Timestamp parsing failed: " + lastDataEntryString);
            }
        }

        // Determine the machine status text
        String statusText;
        if (isOnline) {
            if (machineState != null && machineState) {
                statusText = (motorState != null && motorState)
                        ? "Online - Auto  |  Machine ID: " + machineId
                        : "Online  |  Machine ID: " + machineId;
            } else {
                statusText = "Online  |  Machine ID: " + machineId;
            }
        } else {
            statusText = "Online  |  Machine ID: " + machineId;
        }

        // Add the machine status data to the grid layout
        addSensorCard("Machine ID", "ID: " + machineId);
        addSensorCard("Machine State", (machineState != null) ? (machineState ? "ON" : "OFF") : "Data Not Available");
        addSensorCard("Motor State", (motorState != null) ? (motorState ? "ON" : "OFF") : "Data Not Available");
        addSensorCard("Last Data Entry", (lastDataEntryString != null) ? "Timestamp: " + lastDataEntryString : "Data Not Available");

        // Update the machine status text
        machineStatusText.setText(statusText);
        machineStatusText.setTextColor(getResources().getColor(isOnline ? android.R.color.black : R.color.online_green));

    }

    // Processes RPM data for the motor and injection screw.
    private void handleRpmSensorsData(DataSnapshot snapshot) {

        // Get the RPM sensor data
        DataSnapshot rpmSensorsSnapshot = snapshot.child("rpm_sensors");
        Double injectionScrewRpm = rpmSensorsSnapshot.child("injection_screw_rpm").getValue(Double.class);
        Double mainMotorRpm = rpmSensorsSnapshot.child("main_motor_rpm").getValue(Double.class);

        // Add the RPM data to the grid layout
        addSensorCard("Injection Screw RPM", (injectionScrewRpm != null) ? String.format("%.0f RPM", injectionScrewRpm) : "Data Not Available");
        addSensorCard("Main Motor RPM", (mainMotorRpm != null) ? String.format("%.0f RPM", mainMotorRpm) : "Data Not Available");
    }

    // Displays temperature readings for different zones in the machine.
    private void handleTemperatureZonesData(DataSnapshot snapshot) {

        // Get the temperature zone data
        DataSnapshot tempZonesSnapshot = snapshot.child("temperature_zones");
        Double zone1 = tempZonesSnapshot.child("zone_1").getValue(Double.class);
        Double zone2 = tempZonesSnapshot.child("zone_2").getValue(Double.class);
        Double zone3 = tempZonesSnapshot.child("zone_3").getValue(Double.class);
        Double zone4 = tempZonesSnapshot.child("zone_4").getValue(Double.class);

        // Add the temperature zone data to the grid layout
        addSensorCard("Temperature Zone 1", (zone1 != null) ? zone1 + "°C" : "Data Not Available");
        addSensorCard("Temperature Zone 2", (zone2 != null) ? zone2 + "°C" : "Data Not Available");
        addSensorCard("Temperature Zone 3", (zone3 != null) ? zone3 + "°C" : "Data Not Available");
        addSensorCard("Temperature Zone 4", (zone4 != null) ? zone4 + "°C" : "Data Not Available");
    }

    // Displays motor vibration readings in the UI.
    private void handleVibrationSensorData(DataSnapshot snapshot) {

        // Get the vibration sensor data
        DataSnapshot vibrationSnapshot = snapshot.child("vibration_sensor");
        Double motorVibration = vibrationSnapshot.child("motor_vibration").getValue(Double.class);

        // Add the motor vibration data to the grid layout
        addSensorCard("Motor Vibration", (motorVibration != null) ? String.format("%.3f g", motorVibration) : "Data Not Available");
    }

    // Displays the timestamp of the last sensor data update.
    private void handleTimestamp(DataSnapshot snapshot) {

        // Get the timestamp of the last sensor update
        String timestamp = snapshot.child("timestamp").getValue(String.class);
        if (timestamp != null) {

            // Add the timestamp to the grid layout
            addSensorCard("Last Updated", formatTimestamp(timestamp));
        }

        // Update the last updated text
        lastUpdatedText.setText("@ " + formatTimestamp(timestamp));
    }

    // Format the timestamp for better readability
    private String formatTimestamp(String timestamp) {
        try {
            // Remove the 'Z' and milliseconds for simpler parsing
            timestamp = timestamp.replace("Z", "").substring(0, 19);
            String[] parts = timestamp.split("T");
            String date = parts[0];
            String time = parts[1];

            // Return the formatted timestamp
            return date + " " + time;

        } catch (Exception e) {
            return timestamp;
        }
    }

    // Format the uptime in hours, minutes, and seconds
    private String formatUptime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        // Return the formatted uptime
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    // Add a new sensor card to the grid layout
    private void addSensorCard(String title, String value) {

        // Create a new card view for the sensor data
//        CardView card = (CardView) (getContext() == null ? null : LayoutInflater.from(getContext())).inflate(R.layout.sensor_card_layout, gridLayout, false);

        // Set the title and value of the sensor card
//        TextView titleText = card.findViewById(R.id.sensorTitle);
//        TextView valueText = card.findViewById(R.id.sensorValue);

        // Update the card's title and value
//        titleText.setText(title);
//        valueText.setText(value);

        // Set layout parameters for the card
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);

        // Add the card to the grid layout
//        gridLayout.addView(card, params);
    }

    // Remove the Firebase listener when the fragment is destroyed to prevent memory leaks.
    @Override
    public void onDestroyView() {

        // Call the parent method to clean up the fragment
        super.onDestroyView();

        // Remove the Firebase listener to prevent updates after the fragment is destroyed
        if (databaseRef != null && valueEventListener != null) {
            databaseRef.removeEventListener(valueEventListener);
        }
    }
}
