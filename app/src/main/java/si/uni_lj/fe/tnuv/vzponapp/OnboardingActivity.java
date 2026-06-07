package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    private int step = 1;
    private String userName = "";
    private String experience = "";

    private TextView titleText, subtitleText, dotsText;
    private EditText nameInput;
    private LinearLayout experienceContainer;
    private RadioGroup radioGroup;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        nameInput = findViewById(R.id.nameInput);
        experienceContainer = findViewById(R.id.experienceGroup);
        radioGroup = findViewById(R.id.radioGroup);
        nextButton = findViewById(R.id.nextButton);
        dotsText = findViewById(R.id.dotsText);

        nextButton.setOnClickListener(v -> handleNext());
    }

    private void handleNext() {
        if (step == 1) {
            userName = nameInput.getText().toString().trim();

            if (userName.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_enter_name_short), Toast.LENGTH_SHORT).show();
                return;
            }

            goToExperienceStep();
            return;
        }

        int checkedId = radioGroup.getCheckedRadioButtonId();

        if (checkedId == -1) {
            Toast.makeText(this, getString(R.string.error_choose_experience), Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkedId == R.id.expHiker) {
            experience = "hiker";
        } else if (checkedId == R.id.expMountaineer) {
            experience = "mountaineer";
        } else {
            experience = "alpinist";
        }

        saveUserAndOpenHome();
    }

    private void goToExperienceStep() {
        step = 2;

        titleText.setText(R.string.onboarding_ready_title);
        subtitleText.setText(R.string.onboarding_ready_subtitle);

        nameInput.setVisibility(View.GONE);
        experienceContainer.setVisibility(View.VISIBLE);

        nextButton.setText(R.string.onboarding_start);
        dotsText.setText(R.string.onboarding_dots_2);
    }

    private void saveUserAndOpenHome() {
        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", MODE_PRIVATE);

        prefs.edit()
                .putBoolean("onboarding_done", true)
                .putString("name", userName)
                .putString("experience", experience)
                .apply();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}