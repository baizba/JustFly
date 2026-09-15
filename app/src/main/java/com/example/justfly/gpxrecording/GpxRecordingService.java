package com.example.justfly.gpxrecording;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.justfly.JustFlyApp;
import com.example.justfly.R;
import com.example.justfly.gps.LocationRepository;

import java.io.File;
import java.io.IOException;

public class GpxRecordingService extends Service {
    private final String TAG = "GpxRecordingService";

    private final GpxRecorder gpxRecorder = new GpxRecorder();
    private final IBinder binder = new LocalBinder();
    private final LocationRepository.Listener recordingListener = this::recordLocation;

    private LocationRepository locationRepository;
    private boolean subscribedToLocation;

    // --- Notification Fields (Required for Foreground Service) ---
    private static final String CHANNEL_ID = "GpxRecordingChannel";
    private static final int NOTIFICATION_ID = 1; // Unique ID for the notification


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        locationRepository = JustFlyApp.getLocationRepository(this);
        createNotificationChannel(); // Create notification channel for foreground service
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        // --- Start as a Foreground Service ---
        // This is crucial for background location access from Android 8+
        // and highly recommended for any long-running background task.
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GPX Recording Active")
                .setContentText("Your track is being recorded.")
                .setSmallIcon(R.mipmap.ic_launcher) // REPLACE with your actual notification icon
                .setOngoing(true) // Makes the notification non-dismissable by swipe
                .build();
        startForeground(NOTIFICATION_ID, notification);
        // --- End Foreground Service Setup ---

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (externalFilesDir != null && !externalFilesDir.exists()) {
            if (!externalFilesDir.mkdirs()) {
                Log.e(TAG, "Failed to create recording directory: " + externalFilesDir.getAbsolutePath());
                stopSelf(); // Stop the service if directory creation fails
                return START_NOT_STICKY;
            }
        }

        try {
            if (!gpxRecorder.isRecording()) {
                Log.i(TAG, "Starting GPX Recording to " + externalFilesDir.getAbsolutePath());
                gpxRecorder.start(externalFilesDir); // Pass the directory to your recorder
                startLocationUpdates();
                Log.i(TAG, "GPX Recording started and listening for location updates.");
            } else {
                Log.i(TAG, "GPX Recording already in progress.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while starting GpxRecorder", e);
            stopSelf(); // Stop the service if starting the recorder fails
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        stopLocationUpdates();

        try {
            File file = gpxRecorder.stop();
            Log.i(TAG, "Stopping GPX Recording to " + file.getAbsolutePath());
            SharedPreferences prefs = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE);
            prefs.edit().putString("last_gpx_file_path", file.getAbsolutePath()).apply();
        } catch (IOException e) {
            Log.e(TAG, "IOException while stopping GpxRecorder", e);
        }

        // If you started as a foreground service, ensure it's stopped.
        stopForeground(true); // True to remove the notification
    }

    public boolean isRecording() {
        return gpxRecorder.isRecording();
    }

    private void startLocationUpdates() {
        // Permission check is vital. The service CANNOT request permissions.
        // The calling component (Fragment/Activity) MUST ensure permissions are granted
        // BEFORE starting this service for recording purposes.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permission not granted to service. Cannot start location updates.");
            // Consider stopping the service or at least not attempting to record.
            stopSelf(); // Stop as we can't perform the primary function
            return;
        }

        if (!subscribedToLocation) {
            locationRepository.addListener(recordingListener);
            subscribedToLocation = true;
        }
    }

    private void stopLocationUpdates() {
        if (subscribedToLocation) {
            Log.i(TAG, "Stopping location updates.");
            locationRepository.removeListener(recordingListener);
            subscribedToLocation = false;
        }
    }

    private void recordLocation(@NonNull Location location) {
        try {
            gpxRecorder.record(location);
        } catch (IOException e) {
            Log.e(TAG, "Error recording location point in service", e);
        }
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "GPX Recording Service",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GpxRecordingService getService() {
            return GpxRecordingService.this;
        }
    }


}
