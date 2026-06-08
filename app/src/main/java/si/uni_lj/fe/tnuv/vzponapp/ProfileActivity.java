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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "vzpon_prefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_EXPERIENCE = "experience";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";

    private ImageView profileImage;
    private EditText nameInput;
    private Spinner experienceSpinner;
    private Button saveButton, changeImageButton;

    private SharedPreferences sharedPref;

    private final String[] experienceValues = {
            "hiker",
            "mountaineer",
            "alpinist"
    };

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        bindViews();

        sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        setupSpinner();
        setupImagePicker();
        loadProfileData();
        setupClickListeners();
        setupBottomNavigation();
    }

    private void bindViews() {
        profileImage = findViewById(R.id.profileImage);
        nameInput = findViewById(R.id.nameInput);
        experienceSpinner = findViewById(R.id.experienceSpinner);
        saveButton = findViewById(R.id.saveButton);
        changeImageButton = findViewById(R.id.changeImageButton);
    }

    private void setupSpinner() {
        String[] experienceLabels = {
                getString(R.string.experience_hiker_label),
                getString(R.string.experience_mountaineer_label),
                getString(R.string.experience_alpinist_label)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item,
                experienceLabels
        );

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        experienceSpinner.setAdapter(adapter);
    }

    private void setupClickListeners() {
        changeImageButton.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveProfileData());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            NavigationHelper.openHome(this);
            finish();
        });

        findViewById(R.id.navMap).setOnClickListener(v ->
                NavigationHelper.openMap(this, 0));

        findViewById(R.id.navProfile).setOnClickListener(v -> {
            // Trenutno smo že na profilu.
        });
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
                                    .putString(KEY_PROFILE_IMAGE_URI, imageUri.toString())
                                    .apply();

                            profileImage.setImageURI(imageUri);
                        }
                    } else {
                        Toast.makeText(this, R.string.image_selection_cancelled, Toast.LENGTH_SHORT).show();
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
        String name = sharedPref.getString(
                KEY_NAME,
                getString(R.string.profile_default_user)
        );

        String experience = sharedPref.getString(KEY_EXPERIENCE, experienceValues[0]);
        String imageUriString = sharedPref.getString(KEY_PROFILE_IMAGE_URI, "");

        nameInput.setText(name);

        int selectedIndex = getExperienceIndex(experience);
        experienceSpinner.setSelection(selectedIndex);

        if (!imageUriString.isEmpty()) {
            profileImage.setImageURI(Uri.parse(imageUriString));
        }
    }

    private int getExperienceIndex(String experience) {
        for (int i = 0; i < experienceValues.length; i++) {
            if (experienceValues[i].equals(experience)) {
                return i;
            }
        }
        return 0;
    }

    private void saveProfileData() {
        String newName = nameInput.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, R.string.profile_name_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = experienceSpinner.getSelectedItemPosition();
        String newExperience = experienceValues[selectedPosition];

        sharedPref.edit()
                .putString(KEY_NAME, newName)
                .putString(KEY_EXPERIENCE, newExperience)
                .apply();

        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }
}