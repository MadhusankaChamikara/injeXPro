package com.example.injexpro;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.injexpro.databinding.ActivityDashboardBinding;
import com.google.android.material.snackbar.Snackbar;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    private ActivityDashboardBinding binding;

    // Notification service
    private NotificationService notificationService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.d(TAG, "Initializing DashboardActivity");

            initializeBinding();
            initializeNotificationService();
            setupInitialFragment();
            setupBottomNavigation();

            Log.d(TAG, "DashboardActivity initialization complete");
        } catch (Exception e) {
            Log.e(TAG, "Critical error in onCreate: ", e);
            handleFatalError(e);
        }
    }

    private void initializeBinding() {
        try {
            binding = ActivityDashboardBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            Log.d(TAG, "View binding initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing view binding: ", e);
            throw e; // Rethrow as this is critical
        }
    }

    private void initializeNotificationService() {
        try {
            notificationService = new NotificationService(this);
            Log.d(TAG, "NotificationService initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing NotificationService: ", e);
            showError("Failed to initialize notifications. Some features may be limited.");
        }
    }

    private void setupInitialFragment() {
        try {
            replaceFragment(new HomeFragment());
            binding.bottomNavigationView.setBackground(null);
            Log.d(TAG, "Initial fragment (Home) set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up initial fragment: ", e);
            showError("Failed to load home screen.");
        }
    }

    private void setupBottomNavigation() {
        try {
            binding.bottomNavigationView.setOnItemSelectedListener(item -> {
                Log.d(TAG, "Bottom navigation item selected: " + item.getItemId());
                return handleNavigationItemSelected(item.getItemId());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation: ", e);
            showError("Navigation setup failed.");
        }
    }

    private boolean handleNavigationItemSelected(int itemId) {
        try {
            if (itemId == R.id.home) {
                return loadFragment(new HomeFragment(), "HomeFragment");
            } else if (itemId == R.id.production) {
                return loadFragment(new ProductionFragment(), "ProductionFragment");
            } else if (itemId == R.id.trends) {
                return loadFragment(new TrendsFragment(), "TrendsFragment");
            } else if (itemId == R.id.notification) {
                return loadFragment(new NotificationFragment(), "NotificationFragment");
            } else if (itemId == R.id.settings) {
                return loadFragment(new SettingsFragment(), "SettingsFragment");
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error handling navigation selection: ", e);
            showError("Failed to navigate to selected screen.");
            return false;
        }
    }

    private boolean loadFragment(Fragment fragment, String fragmentName) {
        try {
            Log.d(TAG, "Loading fragment: " + fragmentName);
            replaceFragment(fragment);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error loading fragment " + fragmentName + ": ", e);
            showError("Failed to load " + fragmentName);
            return false;
        }
    }

    private void replaceFragment(Fragment fragment) {
        try {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.commit();
            Log.d(TAG, "Fragment replaced successfully: " + fragment.getClass().getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Error replacing fragment: ", e);
            throw e; // Rethrow as this is a critical operation
        }
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    private void handleFatalError(Exception e) {
        Log.e(TAG, "Fatal error occurred", e);
        showError("A critical error occurred. Please restart the app.");
        finish();
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            Log.d(TAG, "DashboardActivity destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onDestroy: ", e);
        }
    }
}