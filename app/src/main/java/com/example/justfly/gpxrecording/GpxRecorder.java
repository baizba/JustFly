package com.example.justfly.gpxrecording;

import android.content.Context;
import android.location.Location;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class GpxRecorder {

    private BufferedWriter writer;
    private File file;

    public boolean isRecording() {
        return writer != null;
    }

    /**
     * Starts a new GPX recording session. The file is created in the app's
     * external files directory under {@link android.os.Environment#DIRECTORY_DOCUMENTS}.
     */
    public void start(Context context) throws IOException {
        if (writer != null)
            return;

        String fileName = "track_" + System.currentTimeMillis() + ".gpx";
        File dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS);
        if (dir != null && !dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        file = new File(dir, fileName);
        writer = new BufferedWriter(new FileWriter(file));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.newLine();
        writer.write("<gpx version=\"1.1\" creator=\"JustFly\">");
        writer.newLine();
        writer.write("<trk><name>Recorded track</name><trkseg>");
        writer.newLine();
        writer.flush();
    }

    public void record(Location location) throws IOException {
        if (writer == null)
            return;

        String time = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(location.getTime()));
        String line = String.format(Locale.US,
                "<trkpt lat=\"%f\" lon=\"%f\"><ele>%f</ele><time>%s</time></trkpt>",
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                time);
        writer.write(line);
        writer.newLine();
        writer.flush();
    }

    public File stop() throws IOException {
        if (writer == null)
            return null;

        writer.write("</trkseg></trk></gpx>\n");
        writer.flush();
        writer.close();
        File result = file;
        writer = null;
        file = null;
        return result;
    }
}
