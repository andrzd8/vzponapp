package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    int step = 1;

    String userName = "";
    String experience = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        TextView titleText = findViewById(R.id.titleText);
        TextView subtitleText = findViewById(R.id.subtitleText);

        EditText nameInput = findViewById(R.id.nameInput);

        LinearLayout experienceContainer = findViewById(R.id.experienceGroup);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);

        ProgressBar weatherProgress =
                findViewById(R.id.weatherProgress);

        Button nextButton =
                findViewById(R.id.nextButton);

        TextView dotsText =
                findViewById(R.id.dotsText);

        nextButton.setOnClickListener(v -> {

            if (step == 1) {

                userName =
                        nameInput.getText().toString();

                if (userName.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Najprej vnesi ime",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                step = 2;

                titleText.setText("");
                subtitleText.setText("Izberi stopnjo izkušenj?");

                nameInput.setVisibility(View.GONE);

                experienceContainer.setVisibility(View.VISIBLE);
                int checkedId = radioGroup.getCheckedRadioButtonId();

                dotsText.setText("○  ●  ○");

            } else if (step == 2) {

                int checkedId = radioGroup.getCheckedRadioButtonId();

                if (checkedId == -1) {

                    Toast.makeText(
                            this,
                            "Izberi stopnjo izkušenj",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                if (checkedId == R.id.expHiker)            experience = "hiker";
                else if (checkedId == R.id.expMountaineer) experience = "mountaineer";
                else                                       experience = "alpinist";

                step = 3;

                titleText.setText("Vreme za vikend");

                subtitleText.setText(
                        "Nalagam napoved..."
                );

                experienceContainer.setVisibility(View.GONE);
                weatherProgress.setVisibility(View.VISIBLE);

                nextButton.setText("Začni");

                dotsText.setText("○  ○  ●");

            } else {

                SharedPreferences prefs =
                        getSharedPreferences(
                                "vzpon_prefs",
                                MODE_PRIVATE
                        );

                prefs.edit()
                        .putBoolean("onboarding_done", false)
                        .putString("name", userName)
                        .putString("experience", experience)
                        .apply();

                Intent intent =
                        new Intent(
                                this,
                                HomeActivity.class
                        );

                startActivity(intent);

                finish();
            }
        });
    }
}