package si.uni_lj.fe.tnuv.vzponapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

public class TrailDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_details);

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

        TextView titleText = findViewById(R.id.titleText);
        TextView distanceText = findViewById(R.id.distanceText);
        TextView weatherText = findViewById(R.id.weatherText);

        Button saveButton = findViewById(R.id.saveButton);

        String trailName = getIntent().getStringExtra("trail_name");
        String trailDistance = getIntent().getStringExtra("trail_distance");
        String trailWeather = getIntent().getStringExtra("trail_weather");

        titleText.setText(trailName);
        distanceText.setText(trailDistance);
        weatherText.setText(trailWeather);

        saveButton.setOnClickListener(v -> {

            SharedPreferences sharedPref =
                    getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);

            String oldSavedTrails =
                    sharedPref.getString("saved_trails", "");

            if (!oldSavedTrails.contains(trailName)) {

                String newSavedTrails;

                if (oldSavedTrails.isEmpty()) {
                    newSavedTrails = trailName;
                } else {
                    newSavedTrails = oldSavedTrails + "; " + trailName;
                }

                sharedPref.edit()
                        .putString("saved_trails", newSavedTrails)
                        .apply();

                Toast.makeText(this, "Tura shranjena!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Ta tura je že shranjena.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}