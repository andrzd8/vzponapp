package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs =
                getSharedPreferences("vzpon_prefs", MODE_PRIVATE);

        boolean onboardingDone = false;
                //prefs.getBoolean("onboarding_done", false);

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
    }
}