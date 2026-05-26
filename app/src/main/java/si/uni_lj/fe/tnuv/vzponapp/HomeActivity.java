package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

public class HomeActivity extends AppCompatActivity {

    int currentTrail = 0;
    Trail[] trails = TrailRepository.trails;

    TextView trailTitle;
    TextView trailDescription;
    TextView distanceText;
    TextView weatherText;
    TextView difficultyText;
    TextView cardCounter;
    MapView homeMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_home);

        trailTitle = findViewById(R.id.trailTitle);
        trailDescription = findViewById(R.id.trailDescription);
        distanceText = findViewById(R.id.distanceText);
        weatherText = findViewById(R.id.weatherText);
        difficultyText = findViewById(R.id.difficultyText);
        cardCounter = findViewById(R.id.cardCounter);
        homeMapView = findViewById(R.id.homeMapView);

        homeMapView.setTileSource(TileSourceFactory.MAPNIK);
        homeMapView.setMultiTouchControls(false);

        Button previousButton = findViewById(R.id.previousButton);
        Button nextTrailButton = findViewById(R.id.nextTrailButton);
        TextView navMap = findViewById(R.id.navMap);
        TextView navProfile = findViewById(R.id.navProfile);

        showTrail();

        findViewById(R.id.mapClickOverlay).setOnClickListener(v -> openTrailDetails());
        trailTitle.setOnClickListener(v -> openTrailDetails());

        previousButton.setOnClickListener(v -> {
            currentTrail--;
            if (currentTrail < 0) currentTrail = trails.length - 1;
            showTrail();
        });

        nextTrailButton.setOnClickListener(v -> {
            currentTrail++;
            if (currentTrail >= trails.length) currentTrail = 0;
            showTrail();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        navMap.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            intent.putExtra("trail_index", currentTrail);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        homeMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        homeMapView.onPause();
    }

    private void showTrail() {
        Trail trail = trails[currentTrail];

        trailTitle.setText(trail.title);
        trailDescription.setText(trail.description);
        distanceText.setText(trail.distance);
        weatherText.setText(trail.weather);
        difficultyText.setText(trail.difficulty);
        cardCounter.setText((currentTrail + 1) + " / " + trails.length);

        GpxService.GpxData data = GpxService.load(this, trail.gpxFile);

        homeMapView.getOverlays().clear();

        if (!data.points.isEmpty()) {
            Polyline line = new Polyline();
            line.setPoints(data.points);
            line.setColor(Color.rgb(255, 154, 122));
            line.setWidth(8f);
            homeMapView.getOverlays().add(line);

            BoundingBox boundingBox = BoundingBox.fromGeoPoints(data.points);
            homeMapView.post(() -> {
                homeMapView.zoomToBoundingBox(boundingBox, false, 60);
                homeMapView.invalidate();
            });
        }
    }

    private void openTrailDetails() {
        Trail trail = trails[currentTrail];
        Intent intent = new Intent(HomeActivity.this, TrailDetailsActivity.class);
        intent.putExtra("trail_name", trail.title);
        intent.putExtra("trail_distance", trail.distance);
        intent.putExtra("trail_difficulty", trail.difficulty);
        intent.putExtra("trail_gpx_file", trail.gpxFile);
        intent.putExtra("trail_weather", trail.weather);
        intent.putExtra("trail_index", currentTrail);
        startActivity(intent);
    }
}