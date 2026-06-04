package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.concurrent.Executors;

public class OnboardingActivity extends AppCompatActivity {

    int step = 1;
    String userName = "";
    String userEmail = "";
    String experience = "";

    TextView titleText, subtitleText, dotsText;
    EditText nameInput;
    LinearLayout experienceContainer;
    RadioGroup radioGroup;
    ProgressBar weatherProgress;
    Button nextButton, googleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        titleText = findViewById(R.id.titleText);
        subtitleText = findViewById(R.id.subtitleText);
        nameInput = findViewById(R.id.nameInput);
        experienceContainer = findViewById(R.id.experienceGroup);
        radioGroup = findViewById(R.id.radioGroup);
        weatherProgress = findViewById(R.id.weatherProgress);
        nextButton = findViewById(R.id.nextButton);
        dotsText = findViewById(R.id.dotsText);
        googleSignInButton = findViewById(R.id.googleSignInButton);

        googleSignInButton.setOnClickListener(v -> signInWithGoogle());

        nextButton.setOnClickListener(v -> handleNext());
    }

    private void signInWithGoogle() {
        CredentialManager credentialManager = CredentialManager.create(this);

        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.google_web_client_id))
                .setAutoSelectEnabled(false)
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        runOnUiThread(() -> handleGoogleResult(result));
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() -> {

                            android.util.Log.e(
                                    "GOOGLE_LOGIN",
                                    "Google prijava napaka",
                                    e
                            );

                            Toast.makeText(
                                    OnboardingActivity.this,
                                    "NAPAKA: "
                                            + e.getClass().getSimpleName()
                                            + "\n"
                                            + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }

    private void handleGoogleResult(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        android.util.Log.d("GOOGLE_LOGIN", "Credential class: " + credential.getClass().getName());
        android.util.Log.d("GOOGLE_LOGIN", "Credential type: " + credential.getType());

        if (credential instanceof CustomCredential) {
            CustomCredential customCredential = (CustomCredential) credential;

            try {
                GoogleIdTokenCredential googleCredential =
                        GoogleIdTokenCredential.createFrom(customCredential.getData());

                userName = googleCredential.getDisplayName();
                userEmail = googleCredential.getId();

                if (userName == null || userName.isEmpty()) {
                    userName = "Uporabnik";
                }

                Toast.makeText(this, "Prijavljena kot " + userName, Toast.LENGTH_SHORT).show();

                goToExperienceStep();

            } catch (Exception e) {
                android.util.Log.e("GOOGLE_LOGIN", "Napaka pri branju Google podatkov", e);
                Toast.makeText(this, "Napaka pri branju Google računa", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Ni Google credential", Toast.LENGTH_LONG).show();
        }
    }

    private void handleNext() {
        if (step == 1) {
            userName = nameInput.getText().toString().trim();

            if (userName.isEmpty()) {
                Toast.makeText(this, "Najprej vnesi ime", Toast.LENGTH_SHORT).show();
                return;
            }

            goToExperienceStep();

        } else if (step == 2) {
            int checkedId = radioGroup.getCheckedRadioButtonId();

            if (checkedId == -1) {
                Toast.makeText(this, "Izberi stopnjo izkušenj", Toast.LENGTH_SHORT).show();
                return;
            }

            if (checkedId == R.id.expHiker) {
                experience = "hiker";
            } else if (checkedId == R.id.expMountaineer) {
                experience = "mountaineer";
            } else {
                experience = "alpinist";
            }

            step = 3;

            titleText.setText("Vreme za vikend");
            subtitleText.setText("Nalagam napoved...");

            experienceContainer.setVisibility(View.GONE);
            weatherProgress.setVisibility(View.VISIBLE);

            nextButton.setText("Začni");
            dotsText.setText("○  ○  ●");

        } else {
            saveUserAndOpenHome();
        }
    }

    private void goToExperienceStep() {
        step = 2;

        titleText.setText("");
        subtitleText.setText("Izberi stopnjo izkušenj?");

        nameInput.setVisibility(View.GONE);
        googleSignInButton.setVisibility(View.GONE);
        experienceContainer.setVisibility(View.VISIBLE);

        dotsText.setText("○  ●  ○");
    }

    private void saveUserAndOpenHome() {
        SharedPreferences prefs = getSharedPreferences("vzpon_prefs", MODE_PRIVATE);

        prefs.edit()
                .putBoolean("onboarding_done", true)
                .putBoolean("google_signed_in", !userEmail.isEmpty())
                .putString("name", userName)
                .putString("email", userEmail)
                .putString("experience", experience)
                .apply();

        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}