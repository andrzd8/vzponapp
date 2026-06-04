package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.osmdroid.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    Trail[] allTrails = TrailRepository.trails;
    Trail[] displayedTrails = allTrails;

    boolean filterActive = false;
    WeatherService.DailyForecast todayForecast = null;
    String experience = "hiker";

    RecyclerView trailRecyclerView;
    TrailCardAdapter adapter;
    Button filterButton;

    // Nav views
    LinearLayout navHome, navMap, navProfile;
    TextView navHomeIcon, navMapIcon, navProfileIcon;
    View navHomeDot, navMapDot, navProfileDot;

    static final double LAT = 46.05;
    static final double LON = 14.51;
    static final double DEFAULT_ELEVATION = 500;

    private static final int COLOR_ACTIVE = 0xFFFF6B35;
    private static final int COLOR_INACTIVE = 0x66FFFFFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_home);

        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", MODE_PRIVATE);
        experience = prefs.getString("experience", "hiker");

        trailRecyclerView = findViewById(R.id.trailRecyclerView);
        filterButton = findViewById(R.id.filterButton);

        navHome = findViewById(R.id.navHome);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        navHomeIcon = findViewById(R.id.navHomeIcon);
        navMapIcon = findViewById(R.id.navMapIcon);
        navProfileIcon = findViewById(R.id.navProfileIcon);
        navHomeDot = findViewById(R.id.navHomeDot);
        navMapDot = findViewById(R.id.navMapDot);
        navProfileDot = findViewById(R.id.navProfileDot);

        // Home je vedno aktiven na tem screenu
        setActiveNav(0);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false);
        trailRecyclerView.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(trailRecyclerView);

        adapter = new TrailCardAdapter(this, displayedTrails, null, experience,
                position -> openTrailDetails(position));
        trailRecyclerView.setAdapter(adapter);

        navMap.setOnClickListener(v -> {
            setActiveNav(1);
            int pos = getCurrentPosition();
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            intent.putExtra("trail_index", findRealIndex(displayedTrails[pos]));
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            setActiveNav(2);
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        filterButton.setOnClickListener(v -> {
            filterActive = !filterActive;
            applyFilter();
            filterButton.setText(filterActive ? "🟢 Samo varne poti" : "Priporočeno za danes");
        });

        WeatherService.fetchWeather(this, LAT, LON, false, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(String description, double tempCelsius,
                                  List<WeatherService.DailyForecast> forecast, long cachedAt) {
                if (!forecast.isEmpty()) {
                    todayForecast = forecast.get(0);
                    adapter.updateData(displayedTrails, todayForecast);
                }
            }
            @Override
            public void onError(String errorMessage) { }
        });
    }

    private void setActiveNav(int index) {
        // Reset vseh
        navHomeIcon.setAlpha(0.4f);
        navMapIcon.setAlpha(0.4f);
        navProfileIcon.setAlpha(0.4f);
        navHomeDot.setBackgroundColor(Color.TRANSPARENT);
        navMapDot.setBackgroundColor(Color.TRANSPARENT);
        navProfileDot.setBackgroundColor(Color.TRANSPARENT);

        // Aktiviraj izbranega
        switch (index) {
            case 0:
                navHomeIcon.setAlpha(1f);
                navHomeDot.setBackgroundColor(COLOR_ACTIVE);
                break;
            case 1:
                navMapIcon.setAlpha(1f);
                navMapDot.setBackgroundColor(COLOR_ACTIVE);
                break;
            case 2:
                navProfileIcon.setAlpha(1f);
                navProfileDot.setBackgroundColor(COLOR_ACTIVE);
                break;
        }
    }

    private void applyFilter() {
        if (filterActive && todayForecast != null) {
            List<Trail> safe = new ArrayList<>();
            for (Trail t : allTrails) {
                String safety = WeatherService.getSafetyLabel(
                        experience, DEFAULT_ELEVATION, todayForecast, t.longRoute);
                if (!"nevarno".equals(safety)) safe.add(t);
            }
            displayedTrails = safe.toArray(new Trail[0]);
        } else {
            displayedTrails = allTrails;
        }
        adapter.updateData(displayedTrails, todayForecast);
        trailRecyclerView.scrollToPosition(0);
    }

    private void openTrailDetails(int adapterPosition) {
        Trail trail = displayedTrails[adapterPosition];
        String safety = todayForecast != null
                ? WeatherService.getSafetyLabel(experience, DEFAULT_ELEVATION,
                todayForecast, trail.longRoute)
                : "-";

        Intent intent = new Intent(HomeActivity.this, TrailDetailsActivity.class);
        intent.putExtra("trail_name", trail.title);
        intent.putExtra("trail_distance", trail.distance);
        intent.putExtra("trail_difficulty", trail.difficulty);
        intent.putExtra("trail_gpx_file", trail.gpxFile);
        intent.putExtra("trail_long_route", trail.longRoute);
        intent.putExtra("trail_weather", safety);
        intent.putExtra("trail_index", findRealIndex(trail));
        startActivity(intent);
    }

    private int getCurrentPosition() {
        LinearLayoutManager lm = (LinearLayoutManager) trailRecyclerView.getLayoutManager();
        if (lm == null) return 0;
        int pos = lm.findFirstVisibleItemPosition();
        return Math.max(0, Math.min(pos, displayedTrails.length - 1));
    }

    private int findRealIndex(Trail target) {
        for (int i = 0; i < allTrails.length; i++) {
            if (allTrails[i] == target) return i;
        }
        return 0;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs =
                getSharedPreferences("vzpon_prefs", MODE_PRIVATE);

        experience = prefs.getString("experience", "hiker");

        setActiveNav(0);

        applyFilter();
    }
}