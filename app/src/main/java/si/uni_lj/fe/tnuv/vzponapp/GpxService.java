package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import org.osmdroid.util.GeoPoint;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GpxService {

    public static class GpxData {
        public List<GeoPoint> points;
        public List<Double> elevations;
        public GeoPoint center;
        public double maxEle;
        public double minEle;
        public double lat;
        public double lon;

        public GpxData(List<GeoPoint> points, List<Double> elevations, GeoPoint center,
                       double maxEle, double minEle, double lat, double lon) {
            this.points = points;
            this.elevations = elevations;
            this.center = center;
            this.maxEle = maxEle;
            this.minEle = minEle;
            this.lat = lat;
            this.lon = lon;
        }
    }

    private static final Map<Integer, GpxData> cache = new HashMap<>();
    private static final Map<String, GpxData> pathCache = new HashMap<>();

    public static GpxData load(Context context, int gpxResourceId) {
        if (cache.containsKey(gpxResourceId)) return cache.get(gpxResourceId);
        try {
            GpxData data = parse(context.getResources().openRawResource(gpxResourceId));
            cache.put(gpxResourceId, data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return empty();
        }
    }

    public static GpxData load(Context context, String gpxPath) {
        if (pathCache.containsKey(gpxPath)) return pathCache.get(gpxPath);
        try {
            GpxData data = parse(new FileInputStream(gpxPath));
            pathCache.put(gpxPath, data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return empty();
        }
    }

    private static GpxData parse(InputStream inputStream) throws Exception {
        List<GeoPoint> points = new ArrayList<>();
        List<Double> elevations = new ArrayList<>();
        double maxEle = -Double.MAX_VALUE;
        double minEle = Double.MAX_VALUE;
        double lastLat = 0.0, lastLon = 0.0;
        double pendingEle = 0.0;
        boolean inEle = false;

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(inputStream, null);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("trkpt")) {
                    lastLat = Double.parseDouble(parser.getAttributeValue(null, "lat"));
                    lastLon = Double.parseDouble(parser.getAttributeValue(null, "lon"));
                    pendingEle = 0.0;
                } else if (parser.getName().equals("ele")) {
                    inEle = true;
                }
            } else if (eventType == XmlPullParser.TEXT && inEle) {
                pendingEle = Double.parseDouble(parser.getText().trim());
                inEle = false;
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.getName().equals("trkpt")) {
                    points.add(new GeoPoint(lastLat, lastLon));
                    elevations.add(pendingEle);
                    if (pendingEle > maxEle) maxEle = pendingEle;
                    if (pendingEle < minEle) minEle = pendingEle;
                }
                inEle = false;
            }
            eventType = parser.next();
        }
        inputStream.close();

        if (maxEle == -Double.MAX_VALUE) maxEle = 0;
        if (minEle == Double.MAX_VALUE) minEle = 0;

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;
        for (GeoPoint p : points) {
            if (p.getLatitude() < minLat) minLat = p.getLatitude();
            if (p.getLatitude() > maxLat) maxLat = p.getLatitude();
            if (p.getLongitude() < minLon) minLon = p.getLongitude();
            if (p.getLongitude() > maxLon) maxLon = p.getLongitude();
        }
        GeoPoint center = points.isEmpty() ? new GeoPoint(0.0, 0.0)
                : new GeoPoint((minLat + maxLat) / 2, (minLon + maxLon) / 2);

        return new GpxData(points, elevations, center, maxEle, minEle, lastLat, lastLon);
    }

    private static GpxData empty() {
        return new GpxData(new ArrayList<>(), new ArrayList<>(),
                new GeoPoint(0.0, 0.0), 0, 0, 0, 0);
    }
}