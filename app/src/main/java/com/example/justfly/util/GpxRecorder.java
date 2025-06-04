package com.example.justfly.util;

import android.content.Context;
import android.location.Location;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GpxRecorder {

    private FileWriter writer;
    private File file;

    public boolean isRecording() {
        return writer != null;
    }

    public void start(Context context) throws IOException {
        if (writer != null) return;
        String fileName = "track_" + System.currentTimeMillis() + ".gpx";
        file = new File(context.getExternalFilesDir(null), fileName);
        writer = new FileWriter(file);
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<gpx version=\"1.1\" creator=\"JustFly\">\n");
        writer.write("<trk><name>Recorded track</name><trkseg>\n");
    }

    public void record(Location location) throws IOException {
        if (writer == null) return;
        String time = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(location.getTime()));
        String line = String.format(Locale.US,
                "<trkpt lat=\"%f\" lon=\"%f\"><ele>%f</ele><time>%s</time></trkpt>\n",
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                time);
        writer.write(line);
    }

    public File stop() throws IOException {
        if (writer == null) return null;
        writer.write("</trkseg></trk></gpx>\n");
        writer.flush();
        writer.close();
        File result = file;
        writer = null;
        file = null;
        return result;
    }
}
