package si.uni_lj.fe.tnuv.vzponapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrailDetailsActivity extends AppCompatActivity {

    double[] coords;
    double maxEle;
    String experience;
    String trailName;
    boolean longRoute;
    String gpxPath;
    private MapView trailMapView;
    private List<Double> elevationData;
    private double eleMin, eleMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );

        setContentView(R.layout.activity_trail_details);

        Button backButton    = findViewById(R.id.backButton);
        TextView titleText   = findViewById(R.id.titleText);
        TextView distanceText= findViewById(R.id.distanceText);
        Button refreshButton = findViewById(R.id.refreshButton);
        Button aiButton      = findViewById(R.id.ullaButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        backButton.setOnClickListener(v -> finish());

        int trailIndex = getIntent().getIntExtra("trail_index", 0);
        Trail trail = TrailRepository.getTrails(this).get(trailIndex);

        if (trail.gpxPath.startsWith("/")) {
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.GONE);
        }

        trailName = trail.title;
        gpxPath   = trail.gpxPath;
        longRoute = trail.longRoute;

        titleText.setText(trailName);
        distanceText.setText(String.format("%.1f km", trail.distance));
        TextView locationText = findViewById(R.id.locationText);
        locationText.setText(trail.description != null ? trail.description : "");

        trailMapView = findViewById(R.id.trailMapView);
        trailMapView.setTileSource(TileSourceFactory.MAPNIK);
        trailMapView.setMultiTouchControls(false);

        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);
        experience = prefs.getString("experience", "hiker");

        GpxService.GpxData gpxData = GpxService.load(this, gpxPath);
        maxEle        = gpxData.maxEle;
        coords        = new double[]{gpxData.lat, gpxData.lon};
        elevationData = gpxData.elevations;
        eleMin        = gpxData.minEle;
        eleMax        = gpxData.maxEle;

        loadWeather(false);
        drawGpxTrack(gpxPath);

        ElevationView elevView = new ElevationView(this);
        elevView.setBackgroundColor(0xFF112840);
        LinearLayout elevContainer = findViewById(R.id.elevationContainer);
        LinearLayout.LayoutParams elevParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 340);
        elevParams.topMargin = 24;
        elevContainer.addView(elevView, elevParams);

        refreshButton.setOnClickListener(v -> loadWeather(true));

        aiButton.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);
            String safetyLabel = sp.getString("weather_" + trailName, "varno");

            Intent intent = new Intent(this, GeminiActivity.class);
            intent.putExtra("trail_name",   trailName);
            intent.putExtra("lat",          coords[0]);
            intent.putExtra("lon",          coords[1]);
            intent.putExtra("max_ele",      maxEle);
            intent.putExtra("min_ele",      eleMin);
            intent.putExtra("distance",     trail.distance);
            intent.putExtra("experience",   experience);
            intent.putExtra("long_route",   longRoute);
            intent.putExtra("safety_label", safetyLabel);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {

            View dialogView = getLayoutInflater()
                    .inflate(R.layout.dialog_delete_trail, null);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            Button cancelButton = dialogView.findViewById(R.id.cancelDeleteButton);
            Button confirmButton = dialogView.findViewById(R.id.confirmDeleteButton);

            cancelButton.setOnClickListener(x -> dialog.dismiss());

            confirmButton.setOnClickListener(x -> {

                TrailRepository.deleteTrail(this, trail);

                Toast.makeText(this,
                        R.string.delete_trail_success,
                        Toast.LENGTH_SHORT).show();

                dialog.dismiss();
                finish();
            });

            dialog.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (trailMapView != null) trailMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (trailMapView != null) trailMapView.onPause();
    }

    private void drawGpxTrack(String path) {
        GpxService.GpxData data = GpxService.load(this, path);
        if (data.points.isEmpty()) return;

        Polyline line = new Polyline();
        line.setPoints(data.points);
        line.setColor(Color.rgb(255, 154, 122));
        line.setWidth(8f);
        trailMapView.getOverlays().add(line);

        org.osmdroid.util.BoundingBox bb =
                org.osmdroid.util.BoundingBox.fromGeoPoints(data.points);
        trailMapView.post(() -> {
            trailMapView.zoomToBoundingBox(bb, false, 60);
            trailMapView.invalidate();
        });
    }

    private void loadWeather(boolean forceRefresh) {
        WeatherService.fetchWeather(this, coords[0], coords[1], forceRefresh,
                new WeatherService.WeatherCallback() {
                    @Override
                    public void onSuccess(String description, double tempCelsius,
                                          List<WeatherService.DailyForecast> forecast, long cachedAt) {
                        LinearLayout container = findViewById(R.id.forecastDailyContainer);
                        populateDailyForecast(container, forecast, experience, maxEle);

                        if (!forecast.isEmpty()) {
                            WeatherService.DailyForecast danes = forecast.get(0);

                            TextView wText = findViewById(R.id.weatherText);
                            String safetyLabel = WeatherService.getSafetyLabel(
                                    experience, maxEle, danes, longRoute);

                            getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("weather_" + trailName, safetyLabel)
                                    .apply();

                            switch (safetyLabel) {
                                case "varno":
                                    wText.setText("Varno");
                                    wText.setTextColor(0xFF81C784);
                                    break;
                                case "previdno":
                                    wText.setText("Previdno");
                                    wText.setTextColor(0xFFFFD580);
                                    break;
                                default:
                                    wText.setText("Nevarno");
                                    wText.setTextColor(0xFFE57373);
                                    break;
                            }
                        }

                        TextView updatedText = findViewById(R.id.weatherUpdatedText);
                        updatedText.setText("Osveženo: " + new SimpleDateFormat("HH:mm",
                                Locale.getDefault()).format(new Date(cachedAt)));
                    }

                    @Override
                    public void onError(String errorMessage) {}
                });
    }

    private void populateDailyForecast(LinearLayout container,
                                       List<WeatherService.DailyForecast> days,
                                       String experience, double maxEle) {
        container.removeAllViews();

        for (int i = 0; i < days.size(); i++) {
            final WeatherService.DailyForecast d = days.get(i);

            int color;
            if (!d.hours.isEmpty()) {
                color = 0xFF81C784;
                for (WeatherService.HourForecast h : d.hours) {
                    int hColor = WeatherService.getWeatherColor(
                            experience, maxEle, h.pop, h.windSpeed, h.weatherId);
                    if (hColor == 0xFFE57373) { color = hColor; break; }
                    if (hColor == 0xFFFFD580) { color = hColor; }
                }
            } else {
                color = WeatherService.getWeatherColor(
                        experience, maxEle, d.pop, d.windSpeed, d.weatherId);
            }

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);
            col.setPadding(28, 16, 28, 16);
            col.setBackgroundColor(color & 0x40FFFFFF | 0x40000000);

            String dayName = (i == 0) ? "Danes\n" :
                    new SimpleDateFormat("EEE\ndd.MM", Locale.forLanguageTag("sl"))
                            .format(new Date(d.dt * 1000L));

            TextView dayView = new TextView(this);
            dayView.setText(dayName);
            dayView.setTextColor(0xFFFFFFFF);
            dayView.setTextSize(12f);
            dayView.setGravity(Gravity.CENTER);

            View colorBar = new View(this);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 6);
            barParams.topMargin = 6;
            barParams.bottomMargin = 6;
            colorBar.setLayoutParams(barParams);
            colorBar.setBackgroundColor(color);

            TextView emojiView = new TextView(this);
            emojiView.setText(weatherIdToEmoji(d.weatherId));
            emojiView.setTextSize(28f);
            emojiView.setPadding(0, 4, 0, 4);
            emojiView.setGravity(Gravity.CENTER);

            TextView tempView = new TextView(this);
            tempView.setText((int) d.tempMax + "° / " + (int) d.tempMin + "°");
            tempView.setTextColor(0xFFFFFFFF);
            tempView.setTextSize(13f);
            tempView.setTypeface(null, Typeface.BOLD);
            tempView.setGravity(Gravity.CENTER);

            col.addView(dayView);
            col.addView(colorBar);
            col.addView(emojiView);
            col.addView(tempView);
            col.setOnClickListener(v -> showHourlyPopup(d, experience, maxEle));
            container.addView(col);

            if (i < days.size() - 1) {
                View sep = new View(this);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                        1, LinearLayout.LayoutParams.MATCH_PARENT);
                sep.setLayoutParams(p);
                sep.setBackgroundColor(0x40FFFFFF);
                container.addView(sep);
            }
        }
    }

    private void showHourlyPopup(WeatherService.DailyForecast day,
                                 String experience, double maxEle) {
        String dayName = new SimpleDateFormat("EEEE, dd. MM.", Locale.forLanguageTag("sl"))
                .format(new Date(day.dt * 1000L));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setBackgroundColor(0xFF0E2233);

        TextView title = new TextView(this);
        title.setText(dayName);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(18f);
        title.setTypeface(null, Typeface.BOLD);
        title.setPadding(0, 0, 0, 24);
        layout.addView(title);

        if (day.hours.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Ni urnih podatkov");
            empty.setTextColor(0xFFB8C5D1);
            layout.addView(empty);
        } else {
            for (WeatherService.HourForecast h : day.hours) {
                String hour = new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(h.dt * 1000L));

                int hColor = WeatherService.getWeatherColor(
                        experience, maxEle, h.pop, h.windSpeed, h.weatherId);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(0, 10, 0, 10);

                View dot = new View(this);
                LinearLayout.LayoutParams dotP = new LinearLayout.LayoutParams(16, 16);
                dotP.setMarginEnd(16);
                dot.setLayoutParams(dotP);
                dot.setBackgroundColor(hColor);

                TextView hourText = new TextView(this);
                hourText.setText(hour);
                hourText.setTextColor(0xFFB8C5D1);
                hourText.setTextSize(14f);
                hourText.setWidth(120);

                TextView emojiText = new TextView(this);
                emojiText.setText(weatherIdToEmoji(h.weatherId));
                emojiText.setTextSize(20f);
                emojiText.setWidth(80);

                TextView tempText = new TextView(this);
                tempText.setText((int) h.temp + "°");
                tempText.setTextColor(0xFFFFFFFF);
                tempText.setTextSize(14f);
                tempText.setTypeface(null, Typeface.BOLD);
                tempText.setWidth(80);

                TextView windText = new TextView(this);
                windText.setText((int)(h.windSpeed * 3.6) + " km/h");
                windText.setTextColor(0xFFD7DEE5);
                windText.setTextSize(13f);

                row.addView(dot);
                row.addView(hourText);
                row.addView(emojiText);
                row.addView(tempText);
                row.addView(windText);
                layout.addView(row);

                View sep = new View(this);
                sep.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1));
                sep.setBackgroundColor(0x20FFFFFF);
                layout.addView(sep);
            }
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);

        new AlertDialog.Builder(this)
                .setView(scrollView)
                .setPositiveButton("Zapri", null)
                .show();
    }

    private String weatherIdToEmoji(int id) {
        if (id == 800)               return "☀️";
        if (id == 801)               return "🌤️";
        if (id >= 802 && id <= 804)  return "☁️";
        if (id >= 700 && id < 800)   return "🌫️";
        if (id >= 600 && id < 700)   return "❄️";
        if (id >= 500 && id < 600)   return "🌦️";
        if (id >= 300 && id < 500)   return "🌧️";
        if (id >= 200 && id < 300)   return "⛈️";
        return "🌡️";
    }

    private class ElevationView extends View {
        public ElevationView(Context context) { super(context); }

        @Override
        protected void onDraw(Canvas canvas) {
            if (elevationData == null || elevationData.isEmpty()) return;
            int w = getWidth(); int h = getHeight();
            float padLeft = 60f, padBottom = 30f, padTop = 20f;
            float graphW = w - padLeft, graphH = h - padBottom - padTop;
            double eleRange = eleMax - eleMin; if (eleRange == 0) eleRange = 1;

            Paint gp = new Paint();
            gp.setColor(0x20FFFFFF);
            Paint tp = new Paint(Paint.ANTI_ALIAS_FLAG);
            tp.setColor(0xFFB8C5D1); tp.setTextSize(28f);

            for (int i = 0; i <= 3; i++) {
                float y = padTop + graphH - graphH * i / 3f;
                canvas.drawLine(padLeft, y, w, y, gp);
                canvas.drawText((int)(eleMin + eleRange * i / 3) + "m", 0, y + 10, tp);
            }

            Path fp = new Path(), lp = new Path();
            fp.moveTo(padLeft, padTop + graphH);
            for (int i = 0; i < elevationData.size(); i++) {
                float x = padLeft + graphW * i / (elevationData.size() - 1);
                float y = padTop + graphH - (float)((elevationData.get(i) - eleMin) / eleRange * graphH);
                if (i == 0) { lp.moveTo(x, y); fp.lineTo(x, y); }
                else { lp.lineTo(x, y); fp.lineTo(x, y); }
            }
            fp.lineTo(w, padTop + graphH); fp.close();

            Paint fillP = new Paint(Paint.ANTI_ALIAS_FLAG);
            fillP.setColor(0x336BB5FF); fillP.setStyle(Paint.Style.FILL);
            canvas.drawPath(fp, fillP);

            Paint lineP = new Paint(Paint.ANTI_ALIAS_FLAG);
            lineP.setColor(0xFF6BB5FF); lineP.setStrokeWidth(3f);
            lineP.setStyle(Paint.Style.STROKE);
            canvas.drawPath(lp, lineP);
        }
    }
}