package si.uni_lj.fe.tnuv.vzponapp;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    int currentTrail = 0;

    String[] titles = {
            "Voje - Uskovnica",
            "Velika Planina",
            "Slap Savica"
    };

    String[] descriptions = {
            "Zelo sončno, rahlo vetrovno, idealno za hojo v hribe.",
            "Lepa panoramska tura za rekreativce.",
            "Krajša in lažja pot za začetnike."
    };

    String[] distances = {
            "2.3 KM",
            "8.5 KM",
            "4.1 KM"
    };

    String[] weather = {
            "varno",
            "previdno",
            "varno"
    };

    String[] difficulty = {
            "težko",
            "srednje",
            "lahko"
    };

    int[] imageColors = {
            0xFFDDEEFF,
            0xFFE5F5D5,
            0xFFFFE0D2
    };

    TextView trailTitle;
    TextView trailDescription;
    TextView distanceText;
    TextView weatherText;
    TextView difficultyText;
    TextView imagePlaceholder;
    TextView cardCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        trailTitle =
                findViewById(R.id.trailTitle);

        trailDescription =
                findViewById(R.id.trailDescription);

        distanceText =
                findViewById(R.id.distanceText);

        weatherText =
                findViewById(R.id.weatherText);

        difficultyText =
                findViewById(R.id.difficultyText);

        imagePlaceholder =
                findViewById(R.id.imagePlaceholder);

        cardCounter =
                findViewById(R.id.cardCounter);

        Button previousButton =
                findViewById(R.id.previousButton);

        Button nextTrailButton =
                findViewById(R.id.nextTrailButton);

        Button profileButton =
                findViewById(R.id.profileButton);

        showTrail();

        imagePlaceholder.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            TrailDetailsActivity.class
                    );

            intent.putExtra(
                    "trail_name",
                    titles[currentTrail]
            );

            intent.putExtra(
                    "trail_distance",
                    distances[currentTrail]
            );

            intent.putExtra(
                    "trail_weather",
                    weather[currentTrail]
            );

            startActivity(intent);

        });

        previousButton.setOnClickListener(v -> {

            currentTrail--;

            if (currentTrail < 0) {
                currentTrail = titles.length - 1;
            }

            showTrail();
        });

        nextTrailButton.setOnClickListener(v -> {

            currentTrail++;

            if (currentTrail >= titles.length) {
                currentTrail = 0;
            }

            showTrail();
        });

        profileButton.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            HomeActivity.this,
                            ProfileActivity.class
                    );

            startActivity(intent);

        });

    }

    private void showTrail() {

        trailTitle.setText(
                titles[currentTrail]
        );

        trailDescription.setText(
                descriptions[currentTrail]
        );

        distanceText.setText(
                distances[currentTrail]
        );

        weatherText.setText(
                weather[currentTrail]
        );

        difficultyText.setText(
                difficulty[currentTrail]
        );

        imagePlaceholder.setText(
                titles[currentTrail]
        );

        imagePlaceholder.setBackgroundColor(
                imageColors[currentTrail]
        );

        cardCounter.setText(
                (currentTrail + 1)
                        + " / "
                        + titles.length
        );
    }
}