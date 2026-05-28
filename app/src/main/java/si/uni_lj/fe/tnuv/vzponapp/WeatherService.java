package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import java.util.List;
import java.util.Date;
import android.util.Log;

public class WeatherService {

    private static final String API_KEY = "ae3ad8d545df32cf775025c10a643eb1";
    private static final String BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";
    private static final long CACHE_DURATION_MS = 3 * 60 * 60 * 1000L;
    private static final String PREFS_CACHE = "weather_cache";

    public interface WeatherCallback {
        void onSuccess(String description, double tempCelsius, List<DailyForecast> forecast, long cachedAt);
        void onError(String errorMessage);
    }

    public static class HourForecast {
        public long dt;
        public int weatherId;
        public double temp;
        public double pop;
        public double windSpeed;

        public HourForecast(long dt, int weatherId, double temp, double pop, double windSpeed) {
            this.dt = dt;
            this.weatherId = weatherId;
            this.temp = temp;
            this.pop = pop;
            this.windSpeed = windSpeed;
        }
    }

    public static class DailyForecast {
        public long dt;
        public int weatherId;
        public double tempMax;
        public double tempMin;
        public double pop;
        public double windSpeed;
        public List<HourForecast> hours;

        public DailyForecast(long dt, int weatherId, double tempMax, double tempMin,
                             double pop, double windSpeed) {
            this.dt = dt;
            this.weatherId = weatherId;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.pop = pop;
            this.windSpeed = windSpeed;
            this.hours = new ArrayList<>();
        }
    }

    public static void fetchWeather(Context context, double lat, double lon,
                                    boolean forceRefresh, WeatherCallback callback) {
        SharedPreferences cache = context.getSharedPreferences(PREFS_CACHE, Context.MODE_PRIVATE);
        String cacheKey = "json_" + (int)(lat * 100) + "_" + (int)(lon * 100);
        long cachedAt = cache.getLong("ts_" + cacheKey, 0);
        long now = System.currentTimeMillis();

        if (!forceRefresh && cachedAt > 0 && (now - cachedAt) < CACHE_DURATION_MS) {
            String cachedJson = cache.getString(cacheKey, null);
            if (cachedJson != null) {
                try {
                    parseAndDeliver(new JSONObject(cachedJson), cachedAt, callback);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String url = BASE_URL
                + "?lat=" + lat
                + "&lon=" + lon
                + "&exclude=minutely,alerts"
                + "&units=metric"
                + "&lang=sl"
                + "&appid=" + API_KEY;

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    long ts = System.currentTimeMillis();
                    cache.edit()
                            .putString(cacheKey, response.toString())
                            .putLong("ts_" + cacheKey, ts)
                            .apply();
                    try {
                        parseAndDeliver(response, ts, callback);
                    } catch (Exception e) {
                        callback.onError("Napaka pri branju podatkov");
                    }
                },
                error -> callback.onError("Vreme ni dostopno")
        );

        queue.add(request);
    }

    private static void parseAndDeliver(JSONObject response, long cachedAt,
                                        WeatherCallback callback) throws Exception {
        JSONObject current = response.getJSONObject("current");
        double temp = current.getDouble("temp");
        String description = current.getJSONArray("weather")
                .getJSONObject(0).getString("description");

        JSONArray hourly = response.getJSONArray("hourly");

        List<DailyForecast> forecast = new ArrayList<>();
        JSONArray daily = response.getJSONArray("daily");
        for (int i = 0; i <= 4 && i < daily.length(); i++) {
            JSONObject day = daily.getJSONObject(i);
            long dt = day.getLong("dt");
            int weatherId = day.getJSONArray("weather").getJSONObject(0).getInt("id");
            double max = day.getJSONObject("temp").getDouble("max");
            double min = day.getJSONObject("temp").getDouble("min");
            double pop = day.getDouble("pop");
            double wind = day.getDouble("wind_speed");

            DailyForecast df = new DailyForecast(dt, weatherId, max, min, pop, wind);

            String dayDate = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                    .format(new Date(dt * 1000L));

            for (int h = 0; h < hourly.length(); h++) {
                JSONObject hour = hourly.getJSONObject(h);
                long hDt = hour.getLong("dt");
                String hourDate = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault())
                        .format(new Date(hDt * 1000L));

                if (hourDate.equals(dayDate)) {
                    int hWeatherId = hour.getJSONArray("weather").getJSONObject(0).getInt("id");
                    double hTemp = hour.getDouble("temp");
                    double hPop = hour.getDouble("pop");
                    double hWind = hour.getDouble("wind_speed");
                    df.hours.add(new HourForecast(hDt, hWeatherId, hTemp, hPop, hWind));
                }
            }

            forecast.add(df);
        }

        callback.onSuccess(description, temp, forecast, cachedAt);
    }

    public static int getWeatherColor(String experience, double maxEle,
                                      double pop, double windSpeedMs, int weatherId) {
        double windKmh = windSpeedMs * 3.6;

        double eleOffset;
        if (maxEle > 2000)      eleOffset = 0.30;
        else if (maxEle > 1500) eleOffset = 0.20;
        else if (maxEle > 500)  eleOffset = 0.10;
        else                    eleOffset = 0.0;

        double windOffset;
        if (maxEle > 2000)      windOffset = 15;
        else if (maxEle > 1500) windOffset = 10;
        else if (maxEle > 500)  windOffset = 5;
        else                    windOffset = 0;

        double popRed, popYellow, windRed, windYellow;
        switch (experience) {
            case "alpinist":
                popRed     = 0.60 - eleOffset;
                popYellow  = 0.35 - eleOffset;
                windRed    = 60 - (eleOffset * 100);
                windYellow = 35 - (eleOffset * 100);
                break;
            case "mountaineer":
                popRed     = 0.50 - eleOffset;
                popYellow  = 0.28 - eleOffset;
                windRed    = 50 - (eleOffset * 100);
                windYellow = 28 - (eleOffset * 100);
                break;
            default: // hiker
                popRed     = 0.40 - eleOffset;
                popYellow  = 0.20 - eleOffset;
                windRed    = 40 - windOffset;
                windYellow = 20 - windOffset;
                break;
        }

        if (weatherId >= 200 && weatherId < 300) return 0xFFE57373;
        if (pop >= popRed || windKmh >= windRed)  return 0xFFE57373;
        if (pop >= popYellow || windKmh >= windYellow) return 0xFFFFD580;
        return 0xFF81C784;
    }
    
    public static String getSafetyLabel(String experience, double maxEle,
                                        DailyForecast day, boolean longRoute) {
        int color = getWeatherColor(experience, maxEle, day.pop, day.windSpeed, day.weatherId);

        String safety;
        if (color == 0xFFE57373)      safety = "nevarno";
        else if (color == 0xFFFFD580) safety = "previdno";
        else                          safety = "varno";

        // longRoute zviša tveganje za eno stopnjo
        if (longRoute) {
            if ("varno".equals(safety))    return "previdno";
            if ("previdno".equals(safety)) return "nevarno";
        }

        return safety;
    }
}