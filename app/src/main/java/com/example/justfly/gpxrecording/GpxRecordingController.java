package com.example.justfly.gpxrecording;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.ImageButton;

public class GpxRecordingController {

    private GpxRecordingService gpxRecordingService;
    private boolean recordingActive;
    private final ImageButton recordButton;

    private final ServiceConnection gpxServiceConnection = createGpxRecordingServiceConnection();

    public GpxRecordingController(ImageButton recordButton) {
        this.recordButton = recordButton;
    }

    public void bindService(Context context, Intent intent) {
        context.bindService(intent, gpxServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Context context) {
        if (recordingActive) {
            context.unbindService(gpxServiceConnection);
            recordingActive = false;
            gpxRecordingService = null;
        }
    }

    public void addToggleRecordingFunctionality(Context context, Intent intent) {
        if (recordButton != null) {
            recordButton.setOnClickListener(v -> toggleRecording(context, intent));
        }
    }

    private ServiceConnection createGpxRecordingServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                GpxRecordingService.LocalBinder binder = (GpxRecordingService.LocalBinder) service;
                gpxRecordingService = binder.getService();
                recordingActive = gpxRecordingService.isRecording();
                toggleButtonColor(recordingActive);
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                recordingActive = false;
                gpxRecordingService = null;
            }
        };
    }

    private void toggleButtonColor(boolean buttonActive) {
        if (recordButton != null) {
            recordButton.setColorFilter(buttonActive ? android.graphics.Color.RED : android.graphics.Color.GRAY);
        }
    }

    private void toggleRecording(Context context, Intent intent) {
        if (!recordingActive) {
            context.startForegroundService(intent);
            bindService(context, intent);
            toggleButtonColor(true);//make sure to call this because binding service and starting are async operations
            recordingActive = true;
        } else {
            context.stopService(intent);
            unbindService(context);
            recordingActive = false;
            gpxRecordingService = null;
            toggleButtonColor(false);
        }
    }

}
