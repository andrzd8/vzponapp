package si.uni_lj.fe.tnuv.vzponapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileImage;
    EditText nameInput;
    TextView emailText;
    Spinner experienceSpinner;
    Button backButton, saveButton, logoutButton, changeImageButton;

    SharedPreferences sharedPref;

    String[] experienceLabels = {
            "Pohodnik",
            "Gornik",
            "Alpinist"
    };

    String[] experienceValues = {
            "hiker",
            "mountaineer",
            "alpinist"
    };

    ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        emailText = findViewById(R.id.emailText);
        experienceSpinner = findViewById(R.id.experienceSpinner);
        backButton = findViewById(R.id.backButton);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        changeImageButton = findViewById(R.id.changeImageButton);

        sharedPref = getSharedPreferences("vzpon_prefs", Context.MODE_PRIVATE);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                experienceLabels
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        experienceSpinner.setAdapter(adapter);

        setupImagePicker();
        loadProfileData();

        backButton.setOnClickListener(v -> finish());

        changeImageButton.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> saveProfileData());

        logoutButton.setOnClickListener(v -> logout());
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();

                        if (imageUri != null) {
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        imageUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }

                            sharedPref.edit()
                                    .putString("profile_image_uri", imageUri.toString())
                                    .apply();

                            profileImage.setImageURI(imageUri);
                        }
                    } else {
                        Toast.makeText(this, "Izbira slike preklicana", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        imagePickerLauncher.launch(intent);
    }

    private void loadProfileData() {
        String name = sharedPref.getString("name", "Uporabnik");
        String email = sharedPref.getString("email", "");
        String experience = sharedPref.getString("experience", "hiker");
        String imageUriString = sharedPref.getString("profile_image_uri", "");

        nameInput.setText(name);

        if (!email.isEmpty()) {
            emailText.setText(email);
        } else {
            emailText.setText("Prijavljena brez Google računa");
        }

        int selectedIndex = 0;
        for (int i = 0; i < experienceValues.length; i++) {
            if (experienceValues[i].equals(experience)) {
                selectedIndex = i;
                break;
            }
        }
        experienceSpinner.setSelection(selectedIndex);

        if (!imageUriString.isEmpty()) {
            profileImage.setImageURI(Uri.parse(imageUriString));
        }
    }

    private void saveProfileData() {
        String newName = nameInput.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Ime ne sme biti prazno", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = experienceSpinner.getSelectedItemPosition();
        String newExperience = experienceValues[selectedPosition];

        sharedPref.edit()
                .putString("name", newName)
                .putString("experience", newExperience)
                .apply();

        Toast.makeText(this, "Profil shranjen", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sharedPref.edit()
                .clear()
                .apply();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}