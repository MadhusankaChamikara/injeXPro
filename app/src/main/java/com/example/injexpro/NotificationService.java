package com.example.injexpro;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {
    // Notification Channels
    private static final String CHANNEL_ID = "InjectionMoldingAlerts";
    private static final String CHANNEL_NAME = "Injection Molding Alerts";
    private static final String CHANNEL_DESC = "Alerts for injection molding machine parameters";
    private static final String HIGH_PRIORITY_CHANNEL_ID = "CriticalAlerts";
    private static final String HIGH_PRIORITY_CHANNEL_NAME = "Critical Alerts";

    // Throttling map for notification categories
    private final Map<String, Long> lastNotificationTimes = new ConcurrentHashMap<>();
    private static final Map<String, Long> COOLDOWN_PERIODS = new HashMap<>();

    static {
        // Configure cooldown periods for different notification types
        COOLDOWN_PERIODS.put("temperature", 180000L);    // 3 minutes for temperature alerts
        COOLDOWN_PERIODS.put("position", 120000L);       // 2 minutes for position alerts
        COOLDOWN_PERIODS.put("production", 300000L);     // 5 minutes for production alerts
        COOLDOWN_PERIODS.put("critical", 60000L);        // 1 minute for critical alerts
        COOLDOWN_PERIODS.put("default", 300000L);        // 5 minutes default
    }

    private final Context context;
    private final NotificationManager notificationManager;
    private final DatabaseReference sensorDataRef;
    private final DatabaseReference thresholdsRef;
    private final DatabaseReference notificationsRef;
    private Map<String, Object> currentThresholds;
    private static final String TAG = "NotificationService";
    private boolean isServiceActive = true;
    private final SimpleDateFormat dateFormat;

    public NotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Initialize date formatter
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Colombo"));

        // Create notification channels
        createNotificationChannels();

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        sensorDataRef = database.getReference("sensor_data");
        thresholdsRef = database.getReference("thresholds");
        notificationsRef = database.getReference("notifications");

        // Start monitoring
        setupThresholdListener();
        setupSensorDataListener();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Regular notification channel
            NotificationChannel regularChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            regularChannel.setDescription(CHANNEL_DESC);

            // High priority channel for critical alerts
            NotificationChannel criticalChannel = new NotificationChannel(
                    HIGH_PRIORITY_CHANNEL_ID,
                    HIGH_PRIORITY_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            criticalChannel.setDescription("Critical alerts requiring immediate attention");
            criticalChannel.enableVibration(true);
            criticalChannel.setVibrationPattern(new long[]{0, 500, 200, 500});

            notificationManager.createNotificationChannel(regularChannel);
            notificationManager.createNotificationChannel(criticalChannel);
        }
    }

    private void setupThresholdListener() {
        thresholdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentThresholds = (Map<String, Object>) snapshot.getValue();
                    Log.d(TAG, "Thresholds updated successfully");
                } else {
                    Log.w(TAG, "No thresholds data available");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Failed to load thresholds: " + error.getMessage());
            }
        });
    }

    private void setupSensorDataListener() {
        sensorDataRef.limitToLast(1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!isServiceActive || currentThresholds == null) return;

                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        checkTemperatureZones(dataSnapshot);
                        checkAmbientTemperature(dataSnapshot);
                        checkProductionData(dataSnapshot);
                        checkMachineOilTemperature(dataSnapshot);
                        checkMachineHeatExchange(dataSnapshot);
                        checkMoldTemperature(dataSnapshot);
                        checkPositionSensors(dataSnapshot);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing sensor data", e);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    // Service control methods
    public void stopService() {
        isServiceActive = false;
    }

    public void startService() {
        isServiceActive = true;
    }

    private boolean shouldSendNotification(String category) {
        if (!isServiceActive) return false;

        Long lastTime = lastNotificationTimes.get(category);
        long currentTime = SystemClock.elapsedRealtime();
        long cooldownPeriod = getCooldownPeriod(category);

        if (lastTime == null || (currentTime - lastTime) > cooldownPeriod) {
            lastNotificationTimes.put(category, currentTime);
            return true;
        }
        return false;
    }

    private long getCooldownPeriod(String category) {
        String type = category.contains("temp") ? "temperature" :
                category.contains("position") ? "position" :
                        category.contains("production") ? "production" :
                                category.contains("critical") ? "critical" : "default";
        return COOLDOWN_PERIODS.getOrDefault(type, COOLDOWN_PERIODS.get("default"));
    }

    private void checkTemperatureZones(DataSnapshot snapshot) {
        try {
            DataSnapshot tempZones = snapshot.child("temperature_zones");

            // Check each zone with correct casing
            checkZoneTemperature(tempZones, "zone_1", "tempZone1min", "temp_zone1_min");
            checkZoneTemperature(tempZones, "zone_1", "tempZone1max", "temp_zone1_max");
            checkZoneTemperature(tempZones, "zone_2", "tempZone2min", "temp_zone2_min");
            checkZoneTemperature(tempZones, "zone_2", "tempZone2max", "temp_zone2_max");
            checkZoneTemperature(tempZones, "zone_3", "tempZone3min", "temp_zone3_min");
            checkZoneTemperature(tempZones, "zone_3", "tempZone3max", "temp_zone3_max");
            checkZoneTemperature(tempZones, "zone_4", "tempZone4min", "temp_zone4_min");
            checkZoneTemperature(tempZones, "zone_4", "tempZone4max", "temp_zone4_max");
        } catch (Exception e) {
            Log.e(TAG, "Error checking temperature zones", e);
        }
    }

    private void checkZoneTemperature(DataSnapshot tempZones, String zoneName,
                                      String thresholdKey, String notificationCategory) {
        Double currentTemp = tempZones.child(zoneName).getValue(Double.class);
        Double threshold = getThresholdValue(thresholdKey);

        if (currentTemp != null && threshold != null) {
            boolean isMaxThreshold = thresholdKey.toLowerCase().contains("max");
            boolean thresholdExceeded = isMaxThreshold ?
                    currentTemp > threshold : currentTemp < threshold;

            if (thresholdExceeded && shouldSendNotification(notificationCategory)) {
                String title = "Temperature Alert - " + zoneName.toUpperCase();
                String message = String.format("Temperature in %s is %s threshold: %.1f°C (Threshold: %.1f°C)",
                        zoneName.replace("_", " "),
                        isMaxThreshold ? "above" : "below",
                        currentTemp, threshold);

                createAndSendNotification(title, message, "critical", notificationCategory);
            }
        }
    }

    private void checkAmbientTemperature(DataSnapshot snapshot) {
        try {
            Double ambientTemp = snapshot.child("ambient_temperature").getValue(Double.class);
            Double minTemp = getThresholdValue("envTempMin");
            Double maxTemp = getThresholdValue("envTempMax");

            if (ambientTemp != null && minTemp != null && maxTemp != null) {
                if (ambientTemp < minTemp) {
                    if (shouldSendNotification("ambient_temp_low")) {
                        String title = "Low Ambient Temperature Alert";
                        String message = String.format("Ambient temperature is below minimum: %.1f°C (Min: %.1f°C)",
                                ambientTemp, minTemp);
                        createAndSendNotification(title, message, "critical", "ambient_temp_low");
                    }
                } else if (ambientTemp > maxTemp) {
                    if (shouldSendNotification("ambient_temp_high")) {
                        String title = "High Ambient Temperature Alert";
                        String message = String.format("Ambient temperature is above maximum: %.1f°C (Max: %.1f°C)",
                                ambientTemp, maxTemp);
                        createAndSendNotification(title, message, "critical", "ambient_temp_high");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking ambient temperature", e);
        }
    }

    private void checkMachineOilTemperature(DataSnapshot snapshot) {
        try {
            Double oilTemp = snapshot.child("machine_oil_temperature").getValue(Double.class);
            Double minTemp = getThresholdValue("machineOilTempMin");
            Double maxTemp = getThresholdValue("machineOilTempMax");

            if (oilTemp != null && minTemp != null && maxTemp != null) {
                if (oilTemp < minTemp) {
                    if (shouldSendNotification("oil_temp_low")) {
                        String title = "Low Oil Temperature Alert";
                        String message = String.format("Machine oil temperature is below minimum: %.1f°C (Min: %.1f°C)",
                                oilTemp, minTemp);
                        createAndSendNotification(title, message, "critical", "oil_temp_low");
                    }
                } else if (oilTemp > maxTemp) {
                    if (shouldSendNotification("oil_temp_high")) {
                        String title = "High Oil Temperature Alert";
                        String message = String.format("Machine oil temperature is above maximum: %.1f°C (Max: %.1f°C)",
                                oilTemp, maxTemp);
                        createAndSendNotification(title, message, "critical", "oil_temp_high");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking machine oil temperature", e);
        }
    }

    private void checkProductionData(DataSnapshot snapshot) {
        try {
            Double productionCount = snapshot.child("production_data/production_count").getValue(Double.class);
            Double threshold = getThresholdValue("prodQuantityThreshold");

            if (productionCount != null && threshold != null && productionCount > threshold) {
                if (shouldSendNotification("production_quantity")) {
                    String title = "Production Quantity Alert";
                    String message = String.format("Production count has reached target: %.0f (Target: %.0f)",
                            productionCount, threshold);
                    createAndSendNotification(title, message, "warning", "production_quantity");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking production data", e);
        }
    }

    private void checkMachineHeatExchange(DataSnapshot snapshot) {
        try {
            // Get temperature values
            Double inletTemp = snapshot.child("machine_heat_exchange/inlet_temperature").getValue(Double.class);
            Double outletTemp = snapshot.child("machine_heat_exchange/outlet_temperature").getValue(Double.class);

            // Get thresholds
            Double inletMinTemp = getThresholdValue("machineInletTempMin");
            Double inletMaxTemp = getThresholdValue("machineInletTempMax");
            Double outletMinTemp = getThresholdValue("machineOutletTempMin");
            Double outletMaxTemp = getThresholdValue("machineOutletTempMax");

            // Check inlet temperature
            checkHeatExchangeTemperature("Machine Inlet", inletTemp, inletMinTemp, inletMaxTemp,
                    "machine_inlet_temp");

            // Check outlet temperature
            checkHeatExchangeTemperature("Machine Outlet", outletTemp, outletMinTemp, outletMaxTemp,
                    "machine_outlet_temp");

            // Check temperature delta
            checkTemperatureDelta("Machine", inletTemp, outletTemp, "machine_heat_exchange_delta");

        } catch (Exception e) {
            Log.e(TAG, "Error checking machine heat exchange", e);
        }
    }

    private void checkMoldTemperature(DataSnapshot snapshot) {
        try {
            // Get temperature values
            Double inletTemp = snapshot.child("mold_temperature/inlet_temperature").getValue(Double.class);
            Double outletTemp = snapshot.child("mold_temperature/outlet_temperature").getValue(Double.class);

            // Get thresholds
            Double inletMinTemp = getThresholdValue("moldInletTempMin");
            Double inletMaxTemp = getThresholdValue("moldInletTempMax");
            Double outletMinTemp = getThresholdValue("moldOutletTempMin");
            Double outletMaxTemp = getThresholdValue("moldOutletTempMax");

            // Check inlet temperature
            checkHeatExchangeTemperature("Mold Inlet", inletTemp, inletMinTemp, inletMaxTemp,
                    "mold_inlet_temp");

            // Check outlet temperature
            checkHeatExchangeTemperature("Mold Outlet", outletTemp, outletMinTemp, outletMaxTemp,
                    "mold_outlet_temp");

            // Check temperature delta
            checkTemperatureDelta("Mold", inletTemp, outletTemp, "mold_heat_exchange_delta");

        } catch (Exception e) {
            Log.e(TAG, "Error checking mold temperature", e);
        }
    }

    private void checkHeatExchangeTemperature(String location, Double temp, Double minTemp,
                                              Double maxTemp, String category) {
        if (temp != null && minTemp != null && maxTemp != null) {
            if (temp < minTemp) {
                if (shouldSendNotification(category + "_low")) {
                    String title = location + " Temperature Low Alert";
                    String message = String.format("%s temperature is below minimum: %.1f°C (Min: %.1f°C)",
                            location, temp, minTemp);
                    createAndSendNotification(title, message, "critical", category + "_low");
                }
            } else if (temp > maxTemp) {
                if (shouldSendNotification(category + "_high")) {
                    String title = location + " Temperature High Alert";
                    String message = String.format("%s temperature is above maximum: %.1f°C (Max: %.1f°C)",
                            location, temp, maxTemp);
                    createAndSendNotification(title, message, "critical", category + "_high");
                }
            }
        }
    }

    private void checkTemperatureDelta(String location, Double inletTemp, Double outletTemp,
                                       String category) {
        if (inletTemp != null && outletTemp != null) {
            Double tempDelta = Math.abs(outletTemp - inletTemp);
            if ((tempDelta < 5 || tempDelta > 15) && shouldSendNotification(category)) {
                String title = location + " Temperature Delta Alert";
                String message = String.format("%s temperature delta is out of range: %.1f°C " +
                        "(Inlet: %.1f°C, Outlet: %.1f°C)", location, tempDelta, inletTemp, outletTemp);
                createAndSendNotification(title, message, "critical", category);
            }
        }
    }

    private void checkPositionSensors(DataSnapshot snapshot) {
        try {
            checkPositionSensor(snapshot, "ejector_position", "ejectorPosition", "Ejector");
            checkPositionSensor(snapshot, "injection_position", "injectionPosition", "Injection");
            checkPositionSensor(snapshot, "clamping_device_position", "clampingDevicePosition",
                    "Clamping Device");
        } catch (Exception e) {
            Log.e(TAG, "Error checking position sensors", e);
        }
    }

    private void checkPositionSensor(DataSnapshot snapshot, String sensorPath, String thresholdPrefix,
                                     String sensorName) {
        Double position = snapshot.child("position_sensors/" + sensorPath).getValue(Double.class);
        Double minPos = getThresholdValue(thresholdPrefix + "Min");
        Double maxPos = getThresholdValue(thresholdPrefix + "Max");

        if (position != null && minPos != null && maxPos != null) {
            String category = sensorPath.toLowerCase();
            if (position < minPos) {
                if (shouldSendNotification(category + "_low")) {
                    String title = sensorName + " Position Alert";
                    String message = String.format("%s position is below minimum: %.1f (Min: %.1f)",
                            sensorName, position, minPos);
                    createAndSendNotification(title, message, "critical", category + "_low");
                }
            } else if (position > maxPos) {
                if (shouldSendNotification(category + "_high")) {
                    String title = sensorName + " Position Alert";
                    String message = String.format("%s position is above maximum: %.1f (Max: %.1f)",
                            sensorName, position, maxPos);
                    createAndSendNotification(title, message, "critical", category + "_high");
                }
            }
        }
    }

    private Double getThresholdValue(String key) {
        try {
            if (currentThresholds != null && currentThresholds.containsKey(key)) {
                Object value = currentThresholds.get(key);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting threshold value for key: " + key, e);
        }
        return null;
    }

    private void createAndSendNotification(String title, String message, String type, String category) {
        try {
            String channelId = type.equals("critical") ? HIGH_PRIORITY_CHANNEL_ID : CHANNEL_ID;

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(type.equals("critical") ?
                            NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            // Create intent for notification tap action
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.setContentIntent(pendingIntent);

            // Generate unique notification ID
            int notificationId = (category + System.currentTimeMillis()).hashCode();

            // Show notification
            notificationManager.notify(notificationId, builder.build());

            // Save to Firebase
            saveNotificationToFirebase(title, message, type);

        } catch (Exception e) {
            Log.e(TAG, "Error creating notification", e);
        }
    }

    private void saveNotificationToFirebase(String title, String message, String type) {
        try {
            NotificationFragment.NotificationItem notificationItem = new NotificationFragment.NotificationItem(
                    title,
                    message,
                    dateFormat.format(new Date()),
                    type
            );

            notificationsRef.push().setValue(notificationItem)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved to Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification to Firebase", e));

        } catch (Exception e) {
            Log.e(TAG, "Error saving notification", e);
        }
    }
}