package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.graphics.Color;

import org.osmdroid.views.overlay.Polyline;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        GeoPoint sloveniaCenter = new GeoPoint(46.1512, 14.9955);

        mapView.getController().setZoom(8.0);
        mapView.getController().setCenter(sloveniaCenter);

        int trailIndex = getIntent().getIntExtra("trail_index", 0);

        Trail selectedTrail = TrailRepository.trails[trailIndex];

        drawGpxTrack(selectedTrail.gpxFile);

        TextView navHome = findViewById(R.id.navHome);
        TextView navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void drawGpxTrack(int gpxResourceId) {

        ArrayList<GeoPoint> points = new ArrayList<>();

        try {
            InputStream inputStream =
                    getResources().openRawResource(gpxResourceId);

            XmlPullParserFactory factory =
                    XmlPullParserFactory.newInstance();

            XmlPullParser parser =
                    factory.newPullParser();

            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {

                    if (parser.getName().equals("trkpt")) {

                        double lat =
                                Double.parseDouble(
                                        parser.getAttributeValue(null, "lat")
                                );

                        double lon =
                                Double.parseDouble(
                                        parser.getAttributeValue(null, "lon")
                                );

                        points.add(
                                new GeoPoint(lat, lon)
                        );
                    }
                }

                eventType = parser.next();
            }

            inputStream.close();

            if (!points.isEmpty()) {

                Polyline line = new Polyline();

                line.setPoints(points);
                line.setColor(Color.rgb(255, 154, 122));
                line.setWidth(8f);

                mapView.getOverlays().add(line);

                mapView.getController().setZoom(14.0);
                mapView.getController().setCenter(points.get(0));

                mapView.invalidate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
