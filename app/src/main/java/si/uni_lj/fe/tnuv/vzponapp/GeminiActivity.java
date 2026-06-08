package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GeminiActivity extends AppCompatActivity {

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite:generateContent?key=" + API_KEY;

    private LinearLayout equipmentContainer;
    private LinearLayout chatContainer;
    private ScrollView   mainScrollView;
    private EditText     chatInput;
    private ProgressBar  loadingBar;
    private RequestQueue queue;
    private SharedPreferences prefs;

    private final List<JSONObject> history = new ArrayList<>();
    private String trailContext;
    private String cacheKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini);

        queue = Volley.newRequestQueue(this);
        prefs = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);

        // Intent extras
        String  trailName  = getIntent().getStringExtra("trail_name");
        double  lat        = getIntent().getDoubleExtra("lat", 46.0);
        double  lon        = getIntent().getDoubleExtra("lon", 14.5);
        double  maxEle     = getIntent().getDoubleExtra("max_ele", 0);
        double  minEle     = getIntent().getDoubleExtra("min_ele", 0);
        float   distance   = getIntent().getFloatExtra("distance", 0);
        String  experience = getIntent().getStringExtra("experience");
        boolean longRoute  = getIntent().getBooleanExtra("long_route", false);
        String  safety     = getIntent().getStringExtra("safety_label");

        // Cache ključ: trail + vreme + datum → svež odgovor vsak dan ali ob spremembi vremena
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        cacheKey = "gemini_equip_" + trailName + "_" + safety + "_" + today;

        String expLabel   = "mountaineer".equals(experience) ? "izkušen gornik" :
                "alpinist".equals(experience)    ? "alpinist" : "začetnik";
        String safetyDesc = "varno".equals(safety)    ? "varno, dobri pogoji" :
                "previdno".equals(safety) ? "previdnost - možne padavine/veter" :
                        "nevarno - slabe razmere";

        trailContext =
                "Si pomočnik za planinarjenje v Sloveniji. " +
                        "Pot: " + trailName + ". " +
                        "Koordinate: " + String.format("%.4f", lat) + ", " + String.format("%.4f", lon) + ". " +
                        "Višina: " + (int)minEle + "m – " + (int)maxEle + "m. " +
                        "Razdalja: " + String.format("%.1f", distance) + " km. " +
                        "Izkušnje pohodnika: " + expLabel + ". " +
                        (longRoute ? "Dolga in zahtevna tura. " : "") +
                        "Datum in ura klica: " + new SimpleDateFormat("dd. MM. yyyy HH:mm", Locale.getDefault()).format(new Date()) + ". " +
                        "Vremenska ocena za danes: " + safetyDesc + ". " +
                        "Vedno odgovarjaj v slovenščini, kratko in jasno.";

        // Bind views
        Button backButton = findViewById(R.id.geminiBackButton);
        equipmentContainer = findViewById(R.id.equipmentContainer);
        chatContainer      = findViewById(R.id.chatContainer);
        mainScrollView     = findViewById(R.id.mainScrollView);
        chatInput          = findViewById(R.id.chatInput);
        chatInput.setHintTextColor(0xFF4A5568);
        loadingBar         = findViewById(R.id.geminiLoading);
        Button sendButton  = findViewById(R.id.sendButton);

        backButton.setOnClickListener(v -> finish());
        sendButton.setOnClickListener(v -> {
            String msg = chatInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                chatInput.setText("");
                sendChatMessage(msg);
            }
        });

        new Handler().postDelayed(this::fetchEquipmentList, 300);
    }

    // ── Equipment list z cachom ───────────────────────────────────────────────

    private void fetchEquipmentList() {
        String cached = prefs.getString(cacheKey, null);
        if (cached != null) {
            showCacheIndicator();
            parseAndShowList(cached);
            addToHistory("user", buildEquipmentPrompt());
            addToHistory("model", cached);
            return;
        }

        loadingBar.setVisibility(View.VISIBLE);
        callGemini(buildEquipmentPrompt(), true, text -> runOnUiThread(() -> {
            loadingBar.setVisibility(View.GONE);
            if (text.startsWith("Napaka")) {
                addEquipmentItem("⚠️ " + text);
                return;
            }
            prefs.edit().putString(cacheKey, text).apply();
            parseAndShowList(text);
        }));
    }

    private String buildEquipmentPrompt() {
        return trailContext + "\n\n" +
                "Navedi seznam priporočene opreme za to turo glede na pogoje. " +
                "Odgovori SAMO s seznamom, vsak predmet v novi vrstici, začni z '- '. " +
                "Največ 10 predmetov. Brez uvoda, brez pojasnil.";
    }

    private void parseAndShowList(String text) {
        for (String line : text.split("\n")) {
            String item = line.trim().replaceFirst("^[-•*\\d.]+\\s*", "").trim();
            if (!item.isEmpty()) addEquipmentItem(item);
        }
    }

    private void showCacheIndicator() {
        TextView cached = new TextView(this);
        cached.setText("✓ Iz predpomnilnika (danes)");
        cached.setTextColor(0xFF6BB5FF);
        cached.setTextSize(11f);
        cached.setPadding(4, 0, 0, 8);
        equipmentContainer.addView(cached);
    }

    private void setFormattedText(TextView tv, String text) {
        String html = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>")
                .replaceAll("\\*(.+?)\\*", "<i>$1</i>")
                .replaceAll("(?m)^[-•] (.+)$", "• $1")
                .replace("\n", "<br/>");
        tv.setText(android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_COMPACT));
    }

    private void addEquipmentItem(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(4, 14, 4, 14);

        CheckBox cb = new CheckBox(this);
        cb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF6BB5FF));

        TextView tv = new TextView(this);
        setFormattedText(tv, text);  // FIX: pravilen klic namesto deklaracije
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(15f);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        p.setMarginStart(10);
        tv.setLayoutParams(p);

        cb.setOnCheckedChangeListener((btn, checked) -> {
            tv.setTextColor(checked ? 0xFF4A5568 : 0xFFFFFFFF);
            tv.setPaintFlags(checked
                    ? tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
                    : tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        });

        row.addView(cb);
        row.addView(tv);
        equipmentContainer.addView(row);

        View sep = new View(this);
        sep.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1));
        sep.setBackgroundColor(0x15FFFFFF);
        equipmentContainer.addView(sep);
    }

    // ── Chat ──────────────────────────────────────────────────────────────────

    private void sendChatMessage(String msg) {
        addBubble(msg, true);
        addToHistory("user", msg);
        TextView typing = addBubble("...", false);

        callGemini(null, false, response -> runOnUiThread(() -> {
            chatContainer.removeView((View) typing.getParent());
            addBubble(response, false);
            addToHistory("model", response);
        }));
    }

    private TextView addBubble(String text, boolean isUser) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setGravity(isUser ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        wp.topMargin = 10;
        wrapper.setLayoutParams(wp);

        TextView tv = new TextView(this);
        setFormattedText(tv, text);
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(14f);
        tv.setPadding(24, 14, 24, 14);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadii(isUser
                ? new float[]{20,20,20,20,4,4,20,20}
                : new float[]{20,20,20,20,20,20,4,4});
        bg.setColor(isUser ? 0xFFFF9A7A : 0xFF112840);
        tv.setBackground(bg);

        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        tp.setMargins(isUser ? 100 : 0, 0, isUser ? 0 : 100, 0);
        tv.setLayoutParams(tp);

        wrapper.addView(tv);
        chatContainer.addView(wrapper);
        mainScrollView.post(() -> mainScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        return tv;
    }

    // ── Gemini API ────────────────────────────────────────────────────────────

    interface GeminiCallback { void onResponse(String text); }

    private void addToHistory(String role, String text) {
        try {
            JSONObject turn = new JSONObject();
            turn.put("role", role);
            turn.put("parts", new JSONArray().put(new JSONObject().put("text", text)));
            history.add(turn);
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private void callGemini(String singlePrompt, boolean firstCall, GeminiCallback cb) {
        try {
            JSONArray contents = new JSONArray();

            if (firstCall) {
                JSONObject turn = new JSONObject();
                turn.put("role", "user");
                turn.put("parts", new JSONArray().put(new JSONObject().put("text", singlePrompt)));
                contents.put(turn);
                history.add(turn);
            } else {
                for (JSONObject msg : history) contents.put(msg);
            }

            JSONObject body = new JSONObject().put("contents", contents);

            JsonObjectRequest req = new JsonObjectRequest(
                    com.android.volley.Request.Method.POST, API_URL, body,
                    response -> {
                        try {
                            String text = response
                                    .getJSONArray("candidates")
                                    .getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .getString("text");
                            cb.onResponse(text.trim());
                        } catch (JSONException e) {
                            cb.onResponse("Napaka pri branju odgovora.");
                        }
                    },
                    error -> {
                        String errMsg = "Napaka pri povezavi";
                        if (error.networkResponse != null)
                            errMsg += " (Status: " + error.networkResponse.statusCode + ")";
                        cb.onResponse(errMsg);
                    }
            );

            req.setRetryPolicy(new DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            queue.add(req);

        } catch (JSONException e) { e.printStackTrace(); }
    }
}