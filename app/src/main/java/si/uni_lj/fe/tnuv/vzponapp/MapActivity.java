package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    private static final String ARSO_RADAR_URL =
            "https://meteo.arso.gov.si/uploads/probase/www/observ/radar/si0-rm-anim.gif";

    private static final double RADAR_NORTH = 47.386;
    private static final double RADAR_SOUTH = 44.688;
    private static final double RADAR_WEST  = 12.102;
    private static final double RADAR_EAST  = 17.413;

    private static final int CROP_TOP    = 55;
    private static final int CROP_BOTTOM = 15;
    private static final int CROP_LEFT   = 5;
    private static final int CROP_RIGHT  = 5;

    // Minimalna saturacija da piksel velja kot "barvno" (0–1)
    private static final float MIN_SATURATION = 0.25f;

    private MapView mapView;
    private ArsoRadarOverlay radarOverlay;
    private TextView radarLoadingText;

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
        mapView.getController().setZoom(8.0);
        mapView.getController().setCenter(new GeoPoint(46.1512, 14.9955));

        radarLoadingText = findViewById(R.id.radarLoadingText);

        radarOverlay = new ArsoRadarOverlay();
        mapView.getOverlays().add(0, radarOverlay);

        fetchArsoRadar();

        int trailIndex = getIntent().getIntExtra("trail_index", 0);
        Trail selectedTrail = TrailRepository.trails[trailIndex];
        drawGpxTrack(selectedTrail.gpxPath);

        findViewById(R.id.navHome).setOnClickListener(v -> {
            NavigationHelper.openHome(this);
            finish();
        });

        findViewById(R.id.navMap).setOnClickListener(v -> {
            // Že smo na zemljevidu
        });

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            NavigationHelper.openProfile(this);
            finish();
        });
    }

    private void fetchArsoRadar() {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(ARSO_RADAR_URL).openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                InputStream is = conn.getInputStream();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[4096];
                int len;
                while ((len = is.read(chunk)) != -1) buffer.write(chunk, 0, len);
                is.close();
                conn.disconnect();

                byte[] bytes = buffer.toByteArray();
                Movie movie = Movie.decodeByteArray(bytes, 0, bytes.length);
                if (movie == null || movie.width() == 0) return;

                List<Bitmap> frames = extractFrames(movie);

                runOnUiThread(() -> {
                    radarOverlay.setFrames(frames, movie.duration(), System.currentTimeMillis());
                    mapView.invalidate();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private List<Bitmap> extractFrames(Movie movie) {
        List<Bitmap> frames = new ArrayList<>();
        int duration = movie.duration();
        int frameCount = 18; // ARSO animacija ~18 frame-ov (90 min / 5 min)

        Paint p = new Paint();
        for (int i = 0; i < frameCount; i++) {
            movie.setTime((int)((long) duration * i / frameCount));

            Bitmap raw = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(raw);
            movie.draw(c, 0, 0, p);

            frames.add(makeColorsOnly(raw));
            raw.recycle();
        }
        return frames;
    }

    // Ohrani samo barvne piksle, ostalo → transparent
    private Bitmap makeColorsOnly(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        float[] hsv = new float[3];
        for (int i = 0; i < pixels.length; i++) {
            Color.colorToHSV(pixels[i], hsv);
            if (hsv[1] < MIN_SATURATION) {
                pixels[i] = Color.TRANSPARENT;
            }
        }

        result.setPixels(pixels, 0, w, 0, 0, w, h);
        return result;
    }

    private class ArsoRadarOverlay extends Overlay {

        private List<Bitmap> frames;
        private int totalDuration;
        private long startTime = 0;
        private long fetchTime = 0;

        private final Paint imagePaint = new Paint();
        { imagePaint.setAlpha(230); }
        private final Paint legendPaint = new Paint();
        private final Paint textPaint = new Paint();
        private final Paint bgPaint = new Paint();

        ArsoRadarOverlay() {
            textPaint.setColor(Color.WHITE);
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            bgPaint.setColor(Color.argb(160, 0, 0, 0));
        }

        void setFrames(List<Bitmap> f, int duration, long ft) {
            if (radarLoadingText != null) radarLoadingText.setVisibility(android.view.View.GONE);
            frames = f;
            totalDuration = duration;
            fetchTime = ft;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow || frames == null || frames.isEmpty()) return;

            Projection proj = mapView.getProjection();
            Point nw = proj.toPixels(new GeoPoint(RADAR_NORTH, RADAR_WEST), null);
            Point se = proj.toPixels(new GeoPoint(RADAR_SOUTH, RADAR_EAST), null);

            int frameW = frames.get(0).getWidth();
            int frameH = frames.get(0).getHeight();
            int cropW = frameW - CROP_LEFT - CROP_RIGHT;
            int cropH = frameH - CROP_TOP - CROP_BOTTOM;

            float scaleX = (float)(se.x - nw.x) / cropW;
            float scaleY = (float)(se.y - nw.y) / cropH;

            int idx = 0;
            if (totalDuration > 0) {
                long elapsed = (System.currentTimeMillis() - startTime) % totalDuration;
                idx = (int)(elapsed * frames.size() / totalDuration);
                idx = Math.max(0, Math.min(idx, frames.size() - 1));
            }

            canvas.save();
            canvas.clipRect(nw.x, nw.y, se.x, se.y);
            canvas.translate(nw.x - CROP_LEFT * scaleX, nw.y - CROP_TOP * scaleY);
            canvas.scale(scaleX, scaleY);
            canvas.drawBitmap(frames.get(idx), 0, 0, imagePaint);
            canvas.restore();

            drawLegend(canvas, mapView, idx);

            mapView.postInvalidateDelayed(150);
        }

        private void drawLegend(Canvas canvas, MapView mapView, int idx) {
            int[] colors  = { 0xFF9999FF, 0xFF00AAFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF8800, 0xFFFF0000 };
            String[] labels = { ".5", "1", "2", "5", "15", "50+" };

            int swatchW = 36;
            int swatchH = 16;
            int gap = 2;
            int startX = 20;
            int startY = mapView.getHeight() - 50;

            // čas tega frame-a: najnovejši frame = fetchTime, starejši grejo 5 min nazaj
            long frameTime = fetchTime - (long)(frames.size() - 1 - idx) * 5 * 60 * 1000;
            java.util.Date date = new java.util.Date(frameTime);
            String timeLabel = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date);

            canvas.drawRoundRect(
                    new RectF(startX - 6, startY - 40, startX + (swatchW + gap) * colors.length + 6, startY + swatchH + 18),
                    6, 6, bgPaint
            );

            textPaint.setTextSize(18f);
            canvas.drawText("Radar " + timeLabel, startX, startY - 22, textPaint);

            textPaint.setTextSize(16f);
            canvas.drawText("mm/h", startX, startY - 6, textPaint);

            for (int i = 0; i < colors.length; i++) {
                int x = startX + i * (swatchW + gap);
                legendPaint.setColor(colors[i]);
                canvas.drawRect(x, startY, x + swatchW, startY + swatchH, legendPaint);
                textPaint.setTextSize(14f);
                canvas.drawText(labels[i], x + 4, startY + swatchH + 14, textPaint);
            }
        }
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

    private void drawGpxTrack(String gpxFile) {
        ArrayList<GeoPoint> points = new ArrayList<>();
        try {
            InputStream inputStream;
            if (gpxFile.startsWith("/")) {
                inputStream = new FileInputStream(gpxFile);
            } else {
                int resId = getResources().getIdentifier(gpxFile, "raw", getPackageName());
                inputStream = getResources().openRawResource(resId);
            }

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, null);

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.getName().equals("trkpt")) {
                    double lat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                    double lon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                    points.add(new GeoPoint(lat, lon));
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