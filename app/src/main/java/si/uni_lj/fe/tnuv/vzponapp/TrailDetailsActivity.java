package si.uni_lj.fe.tnuv.vzponapp;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrailDetailsActivity extends AppCompatActivity {

    double[] coords;
    double maxEle;
    String experience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trail_details);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        TextView titleText = findViewById(R.id.titleText);
        TextView distanceText = findViewById(R.id.distanceText);
        Button saveButton = findViewById(R.id.saveButton);
        Button refreshButton = findViewById(R.id.refreshButton);

        String trailName = getIntent().getStringExtra("trail_name");
        String trailDistance = getIntent().getStringExtra("trail_distance");
        int gpxFile = getIntent().getIntExtra("trail_gpx_file", -1);

        titleText.setText(trailName);
        distanceText.setText(trailDistance);

        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);
        experience = prefs.getString("experience", "hiker");

        if (gpxFile != -1) {
            coords = WeatherService.getDestinationFromGpx(this, gpxFile);
            maxEle = coords[2];
            loadWeather(false);
        }

        refreshButton.setOnClickListener(v -> loadWeather(true));

        saveButton.setOnClickListener(v -> {
            SharedPreferences sharedPref = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);
            String oldSavedTrails = sharedPref.getString("saved_trails", "");
            if (!oldSavedTrails.contains(trailName)) {
                String newSavedTrails = oldSavedTrails.isEmpty()
                        ? trailName
                        : oldSavedTrails + "; " + trailName;
                sharedPref.edit().putString("saved_trails", newSavedTrails).apply();
                Toast.makeText(this, "Tura shranjena!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ta tura je že shranjena.", Toast.LENGTH_SHORT).show();
            }
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
                            int todayColor;
                            if (!danes.hours.isEmpty()) {
                                android.util.Log.d("COLOR", "maxEle=" + maxEle + " experience=" + experience);
                                todayColor = 0xFF81C784;
                                for (WeatherService.HourForecast h : danes.hours) {
                                    int hColor = WeatherService.getWeatherColor(
                                            experience, maxEle, h.pop, h.windSpeed, h.weatherId);
                                    if (hColor == 0xFFE57373) { todayColor = hColor; break; }
                                    if (hColor == 0xFFFFD580) { todayColor = hColor; }
                                }
                            } else {
                                todayColor = WeatherService.getWeatherColor(
                                        experience, maxEle, danes.pop, danes.windSpeed, danes.weatherId);
                            }

                            TextView wText = findViewById(R.id.weatherText);
                            if (todayColor == 0xFF81C784) {
                                wText.setText("Varno");
                                wText.setTextColor(0xFF81C784);
                            } else if (todayColor == 0xFFFFD580) {
                                wText.setText("Previdno");
                                wText.setTextColor(0xFFFFD580);
                            } else {
                                wText.setText("Nevarno");
                                wText.setTextColor(0xFFE57373);
                            }
                        }

                        TextView updatedText = findViewById(R.id.weatherUpdatedText);
                        updatedText.setText("Osveženo: " + new SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(new Date(cachedAt)));
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
                    android.util.Log.d("COLOR", new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(h.dt * 1000L))
                            + " pop=" + h.pop + " wind=" + (int)(h.windSpeed * 3.6) + " id=" + h.weatherId + " color=" + (hColor == 0xFF81C784 ? "zelena" : hColor == 0xFFFFD580 ? "rumena" : "rdeča"));
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
                LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(16, 16);
                dotParams.setMarginEnd(16);
                dot.setLayoutParams(dotParams);
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
}