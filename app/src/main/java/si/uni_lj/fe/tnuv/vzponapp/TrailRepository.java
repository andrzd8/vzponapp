package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrailRepository {

        private static final String PREFS_NAME = "vzpon_prefs";
        private static final String KEY_TRAILS = "trails_json";

        public static Trail[] trails = new Trail[0];

        public static void load(Context context) {
                try {
                        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        String json = prefs.getString(KEY_TRAILS, "[]");
                        JSONArray array = new JSONArray(json);
                        List<Trail> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);
                                list.add(new Trail(
                                        o.getString("title"),
                                        o.getString("description"),
                                        o.getString("gpxPath"),
                                        o.getString("difficulty"),
                                        (float) o.getDouble("maxEle"),
                                        (float) o.getDouble("minEle"),
                                        (float) o.getDouble("distance"),
                                        o.getBoolean("longRoute")
                                ));
                        }
                        trails = list.toArray(new Trail[0]);
                } catch (Exception e) {
                        trails = new Trail[0];
                }
        }

        public static List<Trail> getTrails(Context context) {
                load(context);
                return new ArrayList<>(java.util.Arrays.asList(trails));
        }

        public static void addTrail(Context context, Trail trail) {
                try {
                        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        String json = prefs.getString(KEY_TRAILS, "[]");
                        JSONArray array = new JSONArray(json);

                        JSONObject o = new JSONObject();
                        o.put("title", trail.title);
                        o.put("description", trail.description);
                        o.put("gpxPath", trail.gpxPath);
                        o.put("difficulty", trail.difficulty);
                        o.put("maxEle", trail.maxEle);
                        o.put("minEle", trail.minEle);
                        o.put("distance", trail.distance);
                        o.put("longRoute", trail.longRoute);
                        array.put(o);

                        prefs.edit().putString(KEY_TRAILS, array.toString()).apply();
                        load(context);
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}