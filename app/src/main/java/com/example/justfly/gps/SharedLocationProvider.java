package com.example.justfly.gps;

import android.location.Location;

import androidx.annotation.NonNull;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

import java.util.Objects;

/**
 * Forwards {@link LocationRepository} fixes into osmdroid's {@code MyLocationNewOverlay}.
 */
public class SharedLocationProvider implements IMyLocationProvider {

    private final LocationRepository locationRepository;
    private IMyLocationConsumer consumer;

    private final LocationRepository.Listener listener = location -> {
        IMyLocationConsumer currentConsumer = consumer;
        if (currentConsumer != null) {
            currentConsumer.onLocationChanged(location, SharedLocationProvider.this);
        }
    };

    public SharedLocationProvider(@NonNull LocationRepository locationRepository) {
        this.locationRepository = Objects.requireNonNull(locationRepository, "locationRepository");
    }

    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        this.consumer = myLocationConsumer;
        locationRepository.addListener(listener);
        return true;
    }

    @Override
    public void stopLocationProvider() {
        locationRepository.removeListener(listener);
        consumer = null;
    }

    @Override
    public Location getLastKnownLocation() {
        return locationRepository.getLastLocation();
    }

    @Override
    public void destroy() {
        stopLocationProvider();
    }
}
