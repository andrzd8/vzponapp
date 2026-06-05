package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class TrailRepository {

        private static final String TRAILS_FILE = "trails.json";
        private static List<Trail> trails = null;

        public static List<Trail> getTrails(Context context) {
                if (trails == null) {
                        trails = load(context);
                }
                return trails;
        }

        public static void addTrail(Context context, Trail trail) {
                getTrails(context).add(trail);
                save(context);
        }

        public static void removeTrail(Context context, int index) {
                getTrails(context).remove(index);
                save(context);
        }

        private static List<Trail> load(Context context) {
                List<Trail> list = new ArrayList<>();
                File file = new File(context.getFilesDir(), TRAILS_FILE);
                if (!file.exists()) return list;

                try {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) sb.append(line);
                        reader.close();

                        JSONArray arr = new JSONArray(sb.toString());
                        for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);
                                list.add(new Trail(
                                        obj.getString("title"),
                                        obj.getString("description"),
                                        obj.getString("gpxPath"),
                                        obj.getString("difficulty"),
                                        (float) obj.getDouble("maxEle"),
                                        (float) obj.getDouble("minEle"),
                                        (float) obj.getDouble("distance"),
                                        obj.getBoolean("longRoute")
                                ));
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return list;
        }

        private static void save(Context context) {
                try {
                        JSONArray arr = new JSONArray();
                        for (Trail t : trails) {
                                JSONObject obj = new JSONObject();
                                obj.put("title", t.title);
                                obj.put("description", t.description);
                                obj.put("gpxPath", t.gpxPath);
                                obj.put("difficulty", t.difficulty);
                                obj.put("maxEle", t.maxEle);
                                obj.put("minEle", t.minEle);
                                obj.put("distance", t.distance);
                                obj.put("longRoute", t.longRoute);
                                arr.put(obj);
                        }

                        FileWriter writer = new FileWriter(
                                new File(context.getFilesDir(), TRAILS_FILE)
                        );
                        writer.write(arr.toString());
                        writer.close();

                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
}