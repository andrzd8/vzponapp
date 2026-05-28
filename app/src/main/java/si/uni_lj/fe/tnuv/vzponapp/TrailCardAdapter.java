package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

public class TrailCardAdapter extends RecyclerView.Adapter<TrailCardAdapter.TrailViewHolder> {

    public interface OnTrailClickListener {
        void onTrailClick(int position);
    }

    private final Context context;
    private Trail[] trails;
    private WeatherService.DailyForecast todayForecast;
    private final String experience;
    private final OnTrailClickListener listener;

    private static final int COLOR_DANGER_BG   = 0x26FF4444;
    private static final int COLOR_DANGER_TEXT  = 0xFFFF6B6B;
    private static final int COLOR_WARN_BG      = 0x26FF8C00;
    private static final int COLOR_WARN_TEXT    = 0xFFFFAA33;
    private static final int COLOR_EASY_BG      = 0x2644BB77;
    private static final int COLOR_EASY_TEXT    = 0xFF55CC88;
    private static final int COLOR_NEUTRAL_BG   = 0xFF0D1F2D;
    private static final int COLOR_NEUTRAL_TEXT = 0xFF8FA8BD;

    public TrailCardAdapter(Context context, Trail[] trails,
                            WeatherService.DailyForecast todayForecast,
                            String experience,
                            OnTrailClickListener listener) {
        this.context = context;
        this.trails = trails;
        this.todayForecast = todayForecast;
        this.experience = experience;
        this.listener = listener;
    }

    public void updateData(Trail[] newTrails, WeatherService.DailyForecast forecast) {
        this.trails = newTrails;
        this.todayForecast = forecast;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_trail_card, parent, false);
        return new TrailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailViewHolder holder, int position) {
        Trail trail = trails[position];

        // Naloži maxEle iz GPX cache (brezplačno po prvem klicu)
        if (trail.maxEle == 0 && trail.gpxFile != 0) {
            GpxService.GpxData gpxData = GpxService.load(context, trail.gpxFile);
            trail.maxEle = gpxData.maxEle;
        }

        holder.title.setText(trail.title);
        holder.description.setText(trail.description);
        holder.distance.setText(trail.distance);

        applyDifficultyBadge(holder, trail.difficulty);

        if (todayForecast != null) {
            String safety = WeatherService.getSafetyLabel(
                    experience, trail.maxEle, todayForecast, trail.longRoute);
            applyWeatherBadge(holder, safety);
        } else {
            holder.weather.setText("...");
            holder.weatherIcon.setText("— ");
            holder.weatherBadge.setBackgroundColor(COLOR_NEUTRAL_BG);
            holder.weather.setTextColor(COLOR_NEUTRAL_TEXT);
            holder.weatherIcon.setTextColor(COLOR_NEUTRAL_TEXT);
        }

        holder.mapView.setTileSource(TileSourceFactory.MAPNIK);
        holder.mapView.setMultiTouchControls(false);
        holder.mapView.getOverlays().clear();

        GpxService.GpxData data = GpxService.load(context, trail.gpxFile);
        if (!data.points.isEmpty()) {
            Polyline line = new Polyline();
            line.setPoints(data.points);
            line.setColor(Color.rgb(255, 154, 122));
            line.setWidth(8f);
            holder.mapView.getOverlays().add(line);

            BoundingBox bbox = BoundingBox.fromGeoPoints(data.points);
            holder.mapView.post(() -> {
                holder.mapView.zoomToBoundingBox(bbox, false, 40);
                holder.mapView.invalidate();
            });
        }

        holder.clickOverlay.setOnClickListener(v -> listener.onTrailClick(position));
        holder.title.setOnClickListener(v -> listener.onTrailClick(position));
    }

    private void applyDifficultyBadge(TrailViewHolder h, String difficulty) {
        if (difficulty == null) difficulty = "";
        h.difficulty.setText(difficulty);
        switch (difficulty.toLowerCase()) {
            case "nevarno":
            case "težko":
                h.difficultyBadge.setBackgroundColor(COLOR_DANGER_BG);
                h.difficultyIcon.setText("⚠ ");
                h.difficultyIcon.setTextColor(COLOR_DANGER_TEXT);
                h.difficulty.setTextColor(COLOR_DANGER_TEXT);
                break;
            case "srednje":
                h.difficultyBadge.setBackgroundColor(COLOR_WARN_BG);
                h.difficultyIcon.setText("~ ");
                h.difficultyIcon.setTextColor(COLOR_WARN_TEXT);
                h.difficulty.setTextColor(COLOR_WARN_TEXT);
                break;
            case "lahko":
            default:
                h.difficultyBadge.setBackgroundColor(COLOR_EASY_BG);
                h.difficultyIcon.setText("✓ ");
                h.difficultyIcon.setTextColor(COLOR_EASY_TEXT);
                h.difficulty.setTextColor(COLOR_EASY_TEXT);
                break;
        }
    }

    private void applyWeatherBadge(TrailViewHolder h, String safety) {
        h.weather.setText(safety);
        switch (safety.toLowerCase()) {
            case "nevarno":
                h.weatherBadge.setBackgroundColor(COLOR_DANGER_BG);
                h.weatherIcon.setText("⚠ ");
                h.weatherIcon.setTextColor(COLOR_DANGER_TEXT);
                h.weather.setTextColor(COLOR_DANGER_TEXT);
                break;
            case "previdno":
            case "tvegano":
            case "srednje":
                h.weatherBadge.setBackgroundColor(COLOR_WARN_BG);
                h.weatherIcon.setText("~ ");
                h.weatherIcon.setTextColor(COLOR_WARN_TEXT);
                h.weather.setTextColor(COLOR_WARN_TEXT);
                break;
            case "varno":
                h.weatherBadge.setBackgroundColor(COLOR_EASY_BG);
                h.weatherIcon.setText("✓ ");
                h.weatherIcon.setTextColor(COLOR_EASY_TEXT);
                h.weather.setTextColor(COLOR_EASY_TEXT);
                break;
            default:
                h.weatherBadge.setBackgroundColor(COLOR_NEUTRAL_BG);
                h.weatherIcon.setText("— ");
                h.weatherIcon.setTextColor(COLOR_NEUTRAL_TEXT);
                h.weather.setTextColor(COLOR_NEUTRAL_TEXT);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return trails != null ? trails.length : 0;
    }

    static class TrailViewHolder extends RecyclerView.ViewHolder {
        MapView mapView;
        View clickOverlay;
        TextView title, description, distance;
        TextView weather, weatherIcon, difficulty, difficultyIcon;
        LinearLayout weatherBadge, difficultyBadge;

        TrailViewHolder(@NonNull View itemView) {
            super(itemView);
            mapView = itemView.findViewById(R.id.cardMapView);
            clickOverlay = itemView.findViewById(R.id.cardClickOverlay);
            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
            distance = itemView.findViewById(R.id.cardDistance);
            weather = itemView.findViewById(R.id.cardWeather);
            weatherIcon = itemView.findViewById(R.id.cardWeatherIcon);
            difficulty = itemView.findViewById(R.id.cardDifficulty);
            difficultyIcon = itemView.findViewById(R.id.cardDifficultyIcon);
            weatherBadge = itemView.findViewById(R.id.cardWeatherBadge);
            difficultyBadge = itemView.findViewById(R.id.cardDifficultyBadge);
        }
    }
}