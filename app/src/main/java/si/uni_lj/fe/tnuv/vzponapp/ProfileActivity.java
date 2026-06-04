package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    TextView nameText;
    TextView experienceText;
    Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.nameText);
        experienceText = findViewById(R.id.experienceText);
        backButton = findViewById(R.id.backButton);

        SharedPreferences sharedPref =
                getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);

        String ime = sharedPref.getString("name", "Uporabnik");
        String izkusnje = sharedPref.getString("experience", "Ni izbrano");
        String email = sharedPref.getString("email", "");
        boolean googleSignedIn =
                sharedPref.getBoolean("google_signed_in", false);

        if (googleSignedIn && !email.isEmpty()) {
            nameText.setText(ime + "\n" + email);
        } else {
            nameText.setText(ime);
        }

        experienceText.setText("Stopnja: " + izkusnje);

        backButton.setOnClickListener(v -> finish());
    }
}