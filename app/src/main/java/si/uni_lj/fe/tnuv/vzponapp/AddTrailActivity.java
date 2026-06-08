package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AddTrailActivity extends AppCompatActivity {

    EditText nameInput;
    EditText descriptionInput;
    Button backButton;
    Button pickGpxButton;
    Button saveButton;
    TextView gpxStatusText;
    TextView statsPreview;

    Uri selectedGpxUri = null;

    float parsedMaxEle = 0;
    float parsedMinEle = Float.MAX_VALUE;
    float parsedDistance = 0;
    float parsedElevationGain = 0;
    String parsedDifficulty = "lahko";

    ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trail);

        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        backButton = findViewById(R.id.backButton);
        pickGpxButton = findViewById(R.id.pickGpxButton);
        saveButton = findViewById(R.id.saveButton);
        gpxStatusText = findViewById(R.id.gpxStatusText);
        statsPreview = findViewById(R.id.statsPreview);

        backButton.setOnClickListener(v -> finish());

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedGpxUri = result.getData().getData();
                        parseAndPreview(selectedGpxUri);
                    }
                }
        );

        pickGpxButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            filePickerLauncher.launch(intent);
        });

        saveButton.setOnClickListener(v -> saveTrail());
    }

    private void parseAndPreview(Uri uri) {
        parsedMaxEle = 0;
        parsedMinEle = Float.MAX_VALUE;
        parsedDistance = 0;
        parsedElevationGain = 0;

        try {
            InputStream is = getContentResolver().openInputStream(uri);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, null);

            List<double[]> points = new ArrayList<>();
            List<Float> elevations = new ArrayList<>();
            boolean inEle = false;

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("trkpt")) {
                        double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                        double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                        points.add(new double[]{lat, lon});
                        inEle = false;
                    } else if (parser.getName().equals("ele")) {
                        inEle = true;
                    }

                } else if (eventType == XmlPullParser.TEXT && inEle) {
                    float ele = Float.parseFloat(parser.getText().trim());
                    elevations.add(ele);
                    if (ele > parsedMaxEle) parsedMaxEle = ele;
                    if (ele < parsedMinEle) parsedMinEle = ele;
                    inEle = false;

                } else if (eventType == XmlPullParser.END_TAG) {
                    inEle = false;
                }

                eventType = parser.next();
            }

            if (is != null) is.close();

            for (int i = 1; i < points.size(); i++) {
                parsedDistance += haversine(points.get(i - 1), points.get(i));
            }

            for (int i = 1; i < elevations.size(); i++) {
                float diff = elevations.get(i) - elevations.get(i - 1);
                if (diff > 0) parsedElevationGain += diff;
            }

            if (parsedElevationGain < 300) parsedDifficulty = "lahko";
            else if (parsedElevationGain < 700) parsedDifficulty = "srednje";
            else parsedDifficulty = "težko";

            gpxStatusText.setText(R.string.add_trail_gpx_loaded);

            statsPreview.setText(getString(
                    R.string.add_trail_stats_preview,
                    parsedDistance,
                    parsedMaxEle,
                    parsedElevationGain,
                    parsedDifficulty
            ));

        } catch (Exception e) {
            Toast.makeText(this, R.string.add_trail_error_read_gpx, Toast.LENGTH_SHORT).show();
        }
    }

    private float haversine(double[] p1, double[] p2) {
        double earthRadiusKm = 6371;
        double dLat = Math.toRadians(p2[0] - p1[0]);
        double dLon = Math.toRadians(p2[1] - p1[1]);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(p1[0])) * Math.cos(Math.toRadians(p2[0]))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return (float) (earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

    private void saveTrail() {
        String name = nameInput.getText().toString().trim();
        String desc = descriptionInput.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, R.string.add_trail_error_name_required, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedGpxUri == null) {
            Toast.makeText(this, R.string.add_trail_error_gpx_required, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File gpxDir = new File(getFilesDir(), "gpx");
            if (!gpxDir.exists()) gpxDir.mkdirs();

            String fileName = name.replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + System.currentTimeMillis() + ".gpx";

            File destFile = new File(gpxDir, fileName);

            InputStream in = getContentResolver().openInputStream(selectedGpxUri);
            OutputStream out = new FileOutputStream(destFile);

            byte[] buf = new byte[4096];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            if (in != null) in.close();
            out.close();

            Trail trail = new Trail(
                    name,
                    desc,
                    destFile.getAbsolutePath(),
                    parsedDifficulty,
                    parsedMaxEle,
                    parsedMinEle,
                    parsedDistance,
                    parsedDistance > 15
            );

            TrailRepository.addTrail(this, trail);

            Toast.makeText(this, R.string.add_trail_success, Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Toast.makeText(this, R.string.add_trail_error_save, Toast.LENGTH_SHORT).show();
        }
    }
}