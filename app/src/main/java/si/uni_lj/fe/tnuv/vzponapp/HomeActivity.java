package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TrailCardAdapter adapter;
    View emptyState;
    WeatherService.DailyForecast currentForecast = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.trailRecyclerView);
        emptyState = findViewById(R.id.emptyState);

        LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(lm);
        new LinearSnapHelper().attachToRecyclerView(recyclerView);
        recyclerView.setClipToPadding(false);

        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navMap = findViewById(R.id.navMap);
        LinearLayout navProfile = findViewById(R.id.navProfile);
        Button addTrailButton = findViewById(R.id.addTrailButton);

        navHome.setOnClickListener(v -> recyclerView.smoothScrollToPosition(0));

        navMap.setOnClickListener(v -> {
            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
            int pos = llm != null ? llm.findFirstCompletelyVisibleItemPosition() : 0;
            if (pos < 0) pos = 0;
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("trail_index", pos);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        addTrailButton.setOnClickListener(v ->
                startActivity(new Intent(this, AddTrailActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTrails();
    }

    private void refreshTrails() {
        List<Trail> trails = TrailRepository.getTrails(this);

        if (trails.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", MODE_PRIVATE);
        String experience = prefs.getString("experience", "Pohodnik");

        if (adapter == null) {
            adapter = new TrailCardAdapter(this, trails, currentForecast, experience, pos -> {
                Intent intent = new Intent(this, TrailDetailsActivity.class);
                intent.putExtra("trail_index", pos);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateData(trails, currentForecast);
        }

        loadWeather(trails);
    }

    private void loadWeather(List<Trail> trails) {
        double lat = 46.0569;
        double lon = 14.5058;

        if (!trails.isEmpty()) {
            GpxService.GpxData data = GpxService.load(this, trails.get(0).gpxPath);
            if (data.center != null && data.center.getLatitude() != 0) {
                lat = data.center.getLatitude();
                lon = data.center.getLongitude();
            }
        }

        WeatherService.fetchWeather(this, lat, lon, false, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(String description, double tempCelsius,
                                  List<WeatherService.DailyForecast> forecast, long cachedAt) {
                if (forecast.isEmpty()) return;
                currentForecast = forecast.get(0);
                if (adapter != null) {
                    adapter.updateData(TrailRepository.getTrails(HomeActivity.this), currentForecast);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // adapter ostane z "..." za vreme
            }
        });
    }
}