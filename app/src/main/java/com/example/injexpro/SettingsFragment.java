package com.example.injexpro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {
    // Firebase reference
    private DatabaseReference thresholdsRef;
    private ValueEventListener valueEventListener;
    private FirebaseAuth mAuth;

    // Temperature Zones UI elements
    private EditText tempZone1min, tempZone2min, tempZone3min, tempZone4min;
    private EditText tempZone1max, tempZone2max, tempZone3max, tempZone4max;

    // Environment Temperature UI elements
    private EditText envTempMin, envTempMax;

    // Production Quantity UI elements
    private EditText prodQuantityThreshold;

    // Machine Oil Temperature UI elements
    private EditText machineOilTempMin, machineOilTempMax;

    // Clamping Device Position UI elements
    private EditText clampingDevicePositionMin, clampingDevicePositionMax;

    // Injection Position UI elements
    private EditText injectionPositionMin, injectionPositionMax;

    // Machine Heat Exchange UI elements
    private EditText machineInletTempMin, machineInletTempMax;
    private EditText machineOutletTempMin, machineOutletTempMax;

    // Mold Heat Exchange UI elements
    private EditText moldInletTempMin, moldInletTempMax;
    private EditText moldOutletTempMin, moldOutletTempMax;

    // Save Button UI element
    private Button saveButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        thresholdsRef = FirebaseDatabase.getInstance().getReference("thresholds");

        // Initialize UI elements
        initializeViews(view);

        // Load existing thresholds
        loadThresholds();

        // Set up save button
        saveButton.setOnClickListener(v -> saveThresholds());

        // Set up logout button
        Button logoutButton = view.findViewById(R.id.logout);
        setupLogoutButton(logoutButton);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null && thresholdsRef != null) {
            thresholdsRef.removeEventListener(valueEventListener);
        }
    }

    private void setupLogoutButton(Button logoutButton) {
        logoutButton.setOnClickListener(v -> {
            Context context = getContext();
            if (context == null) return;

            // Remove the database listener
            if (valueEventListener != null) {
                thresholdsRef.removeEventListener(valueEventListener);
            }

            // Log out the user
            mAuth.signOut();
            showToast("Logged out successfully");

            // Redirect to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            Activity activity = getActivity();
            if (activity != null) {
                activity.finish();
            }
        });
    }

    private void showToast(String message) {
        Context context = getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    // Initialize UI elements
    private void initializeViews(View view) {
        // Initialize UI elements for temperature zones
        tempZone1min = view.findViewById(R.id.tempZone1Threshold);
        tempZone1max = view.findViewById(R.id.tempZone1ThresholdMax);
        tempZone2min = view.findViewById(R.id.tempZone2Threshold);
        tempZone2max = view.findViewById(R.id.tempZone2ThresholdMax);
        tempZone3min = view.findViewById(R.id.tempZone3Threshold);
        tempZone3max = view.findViewById(R.id.tempZone3ThresholdMax);
        tempZone4min = view.findViewById(R.id.tempZone4Threshold);
        tempZone4max = view.findViewById(R.id.tempZone4ThresholdMax);

        // Initialize UI elements for environment temperature
        envTempMin = view.findViewById(R.id.envTempThresholdMin);
        envTempMax = view.findViewById(R.id.envTempThresholdMax);

        // Initialize UI elements for production quantity
        prodQuantityThreshold = view.findViewById(R.id.prodQtyThreshold);

        // Initialize UI elements for machine oil temperature
        machineOilTempMin = view.findViewById(R.id.machinOilTempThresholdMin);
        machineOilTempMax = view.findViewById(R.id.machinOilTempThresholdMax);

        // Initialize UI elements for clamping device position
        clampingDevicePositionMin = view.findViewById(R.id.clampDevicePositionThresholdMin);
        clampingDevicePositionMax = view.findViewById(R.id.clampDevicePositionThresholdMax);

        // Initialize UI elements for injection position
        injectionPositionMin = view.findViewById(R.id.injectPositionThresholdMin);
        injectionPositionMax = view.findViewById(R.id.injectPositionThresholdMax);

        // Initialize UI elements for machine heat exchange
        machineInletTempMin = view.findViewById(R.id.machineInletTempThresholdMin);
        machineInletTempMax = view.findViewById(R.id.machineInletTempThresholdMax);
        machineOutletTempMin = view.findViewById(R.id.machineOutletTempThresholdMin);
        machineOutletTempMax = view.findViewById(R.id.machineOutletTempThresholdMax);

        // Initialize UI elements for mold heat exchange
        moldInletTempMin = view.findViewById(R.id.moldInletTempThresholdMin);
        moldInletTempMax = view.findViewById(R.id.moldInletTempThresholdMax);
        moldOutletTempMin = view.findViewById(R.id.moldOutletTempThresholdMin);
        moldOutletTempMax = view.findViewById(R.id.moldOutletTempThresholdMax);

        // Initialize save button
        saveButton = view.findViewById(R.id.saveThresholdsButton);
    }

    // Load the existing thresholds from the database
    private void loadThresholds() {
        valueEventListener = thresholdsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    try {
                        Map<String, Object> thresholds = (Map<String, Object>) snapshot.getValue();
                        if (thresholds == null) return;

                        // Safely set text values with null checks
                        setEditTextValue(tempZone1min, thresholds.get("tempZone1min"));
                        setEditTextValue(tempZone1max, thresholds.get("tempZone1max"));
                        setEditTextValue(tempZone2min, thresholds.get("tempZone2min"));
                        setEditTextValue(tempZone2max, thresholds.get("tempZone2max"));
                        setEditTextValue(tempZone3min, thresholds.get("tempZone3min"));
                        setEditTextValue(tempZone3max, thresholds.get("tempZone3max"));
                        setEditTextValue(tempZone4min, thresholds.get("tempZone4min"));
                        setEditTextValue(tempZone4max, thresholds.get("tempZone4max"));

                        setEditTextValue(envTempMin, thresholds.get("envTempMin"));
                        setEditTextValue(envTempMax, thresholds.get("envTempMax"));

                        setEditTextValue(prodQuantityThreshold, thresholds.get("prodQuantityThreshold"));

                        setEditTextValue(machineOilTempMin, thresholds.get("machineOilTempMin"));
                        setEditTextValue(machineOilTempMax, thresholds.get("machineOilTempMax"));

                        setEditTextValue(clampingDevicePositionMin, thresholds.get("clampingDevicePositionMin"));
                        setEditTextValue(clampingDevicePositionMax, thresholds.get("clampingDevicePositionMax"));

                        setEditTextValue(injectionPositionMin, thresholds.get("injectionPositionMin"));
                        setEditTextValue(injectionPositionMax, thresholds.get("injectionPositionMax"));

                        setEditTextValue(machineInletTempMin, thresholds.get("machineInletTempMin"));
                        setEditTextValue(machineInletTempMax, thresholds.get("machineInletTempMax"));
                        setEditTextValue(machineOutletTempMin, thresholds.get("machineOutletTempMin"));
                        setEditTextValue(machineOutletTempMax, thresholds.get("machineOutletTempMax"));

                        setEditTextValue(moldInletTempMin, thresholds.get("moldInletTempMin"));
                        setEditTextValue(moldInletTempMax, thresholds.get("moldInletTempMax"));
                        setEditTextValue(moldOutletTempMin, thresholds.get("moldOutletTempMin"));
                        setEditTextValue(moldOutletTempMax, thresholds.get("moldOutletTempMax"));

                    } catch (Exception e) {
                        showToast("Error loading thresholds: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                if (isAdded()) {
                    showToast("Failed to load thresholds: " + error.getMessage());
                }
            }
        });
    }

    private void setEditTextValue(EditText editText, Object value) {
        if (editText != null && value != null) {
            editText.setText(String.valueOf(value));
        }
    }

    // Save the thresholds to the database
    private void saveThresholds() {
        Context context = getContext();
        if (context == null) return;

        Map<String, Object> thresholds = new HashMap<>();

        try {
            // Populate the thresholds map with safe parsing
            thresholds.put("tempZone1min", parseDouble(tempZone1min));
            thresholds.put("tempZone1max", parseDouble(tempZone1max));
            thresholds.put("tempZone2min", parseDouble(tempZone2min));
            thresholds.put("tempZone2max", parseDouble(tempZone2max));
            thresholds.put("tempZone3min", parseDouble(tempZone3min));
            thresholds.put("tempZone3max", parseDouble(tempZone3max));
            thresholds.put("tempZone4min", parseDouble(tempZone4min));
            thresholds.put("tempZone4max", parseDouble(tempZone4max));

            thresholds.put("envTempMin", parseDouble(envTempMin));
            thresholds.put("envTempMax", parseDouble(envTempMax));

            thresholds.put("prodQuantityThreshold", parseDouble(prodQuantityThreshold));

            thresholds.put("machineOilTempMin", parseDouble(machineOilTempMin));
            thresholds.put("machineOilTempMax", parseDouble(machineOilTempMax));

            thresholds.put("clampingDevicePositionMin", parseDouble(clampingDevicePositionMin));
            thresholds.put("clampingDevicePositionMax", parseDouble(clampingDevicePositionMax));

            thresholds.put("injectionPositionMin", parseDouble(injectionPositionMin));
            thresholds.put("injectionPositionMax", parseDouble(injectionPositionMax));

            thresholds.put("machineInletTempMin", parseDouble(machineInletTempMin));
            thresholds.put("machineInletTempMax", parseDouble(machineInletTempMax));
            thresholds.put("machineOutletTempMin", parseDouble(machineOutletTempMin));
            thresholds.put("machineOutletTempMax", parseDouble(machineOutletTempMax));

            thresholds.put("moldInletTempMin", parseDouble(moldInletTempMin));
            thresholds.put("moldInletTempMax", parseDouble(moldInletTempMax));
            thresholds.put("moldOutletTempMin", parseDouble(moldOutletTempMin));
            thresholds.put("moldOutletTempMax", parseDouble(moldOutletTempMax));

            // Save thresholds to the database
            thresholdsRef.setValue(thresholds)
                    .addOnSuccessListener(aVoid -> {
                        if (isAdded()) {
                            showToast("Thresholds saved successfully");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            showToast("Failed to save thresholds: " + e.getMessage());
                        }
                    });

        } catch (NumberFormatException e) {
            showToast("Please enter valid numbers in all fields");
        }
    }

    private double parseDouble(EditText editText) throws NumberFormatException {
        if (editText == null || editText.getText() == null || editText.getText().toString().trim().isEmpty()) {
            throw new NumberFormatException("Empty or invalid input");
        }
        return Double.parseDouble(editText.getText().toString().trim());
    }
}