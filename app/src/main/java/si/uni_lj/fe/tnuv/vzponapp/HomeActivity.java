package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    int currentTrail = 0;

    Trail[] trails = TrailRepository.trails;

    TextView trailTitle;
    TextView trailDescription;
    TextView distanceText;
    TextView weatherText;
    TextView difficultyText;
    TextView imagePlaceholder;
    TextView cardCounter;

    int[] imageColors = {
            0xFFDDEEFF,
            0xFFE5F5D5,
            0xFFFFE0D2,
            0xFFE8DDF5,
            0xFFFFF4D6
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        trailTitle = findViewById(R.id.trailTitle);
        trailDescription = findViewById(R.id.trailDescription);
        distanceText = findViewById(R.id.distanceText);
        weatherText = findViewById(R.id.weatherText);
        difficultyText = findViewById(R.id.difficultyText);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        cardCounter = findViewById(R.id.cardCounter);

        Button previousButton = findViewById(R.id.previousButton);
        Button nextTrailButton = findViewById(R.id.nextTrailButton);

        TextView navMap = findViewById(R.id.navMap);
        TextView navProfile = findViewById(R.id.navProfile);

        showTrail();

        imagePlaceholder.setOnClickListener(v -> openTrailMap());

        previousButton.setOnClickListener(v -> {
            currentTrail--;

            if (currentTrail < 0) {
                currentTrail = trails.length - 1;
            }

            showTrail();
        });

        nextTrailButton.setOnClickListener(v -> {
            currentTrail++;

            if (currentTrail >= trails.length) {
                currentTrail = 0;
            }

            showTrail();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        navMap.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);

            intent.putExtra("trail_index", currentTrail);

            startActivity(intent);
        });
    }

    private void showTrail() {
        Trail trail = trails[currentTrail];

        trailTitle.setText(trail.title);
        trailDescription.setText(trail.description);
        distanceText.setText(trail.distance);
        weatherText.setText(trail.weather);
        difficultyText.setText(trail.difficulty);

        imagePlaceholder.setText(trail.title);

        imagePlaceholder.setBackgroundColor(
                imageColors[currentTrail % imageColors.length]
        );

        cardCounter.setText(
                (currentTrail + 1) + " / " + trails.length
        );
    }

    private void openTrailMap() {
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