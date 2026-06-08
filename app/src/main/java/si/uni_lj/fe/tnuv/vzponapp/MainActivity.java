package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences prefs =
                    getSharedPreferences("vzpon_prefs", MODE_PRIVATE);

            boolean onboardingDone = prefs.getBoolean("onboarding_done", false);

            if (onboardingDone) {

                Intent intent =
                        new Intent(this, HomeActivity.class);

                startActivity(intent);

            } else {

                Intent intent =
                        new Intent(this, OnboardingActivity.class);

                startActivity(intent);
            }

            finish();

        }, 2000);
    }
}