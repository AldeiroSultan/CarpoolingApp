package com.carpoolingapp.activities;
// File: CarpoolingApp/app/src/main/java/com/carpooling/app/activities/RegisterActivity.java
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.carpoolingapp.R;
import com.carpoolingapp.models.User;
import com.carpoolingapp.utils.FirebaseHelper;
import com.carpoolingapp.utils.SharedPrefsHelper;
import com.carpoolingapp.utils.ValidationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout, emailInputLayout, phoneInputLayout, passwordInputLayout;
    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private MaterialButton registerButton;
    private View loginText;

    private FirebaseHelper firebaseHelper;
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initFirebase();
        setupListeners();
    }

    private void initViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
    }

    private void initFirebase() {
        firebaseHelper = FirebaseHelper.getInstance();
        prefsHelper = new SharedPrefsHelper(this);
    }

    private void setupListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });

        loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performRegister() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate inputs
        String nameError = ValidationHelper.getNameError(name);
        String emailError = ValidationHelper.getEmailError(email);
        String passwordError = ValidationHelper.getPasswordError(password);

        if (nameError != null) {
            nameInputLayout.setError(nameError);
            return;
        }
        nameInputLayout.setError(null);

        if (emailError != null) {
            emailInputLayout.setError(emailError);
            return;
        }
        emailInputLayout.setError(null);

        if (passwordError != null) {
            passwordInputLayout.setError(passwordError);
            return;
        }
        passwordInputLayout.setError(null);

        // Show loading
        registerButton.setEnabled(false);
        registerButton.setText("Creating account...");

        // Firebase authentication
        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseHelper.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                // Create user in database
                                User user = new User(firebaseUser.getUid(), name, email, phone);
                                firebaseHelper.getUserRef(firebaseUser.getUid()).setValue(user)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                // Save user data locally
                                                prefsHelper.saveUserData(
                                                        firebaseUser.getUid(),
                                                        name,
                                                        email
                                                );

                                                // Navigate to Home
                                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                registerButton.setEnabled(true);
                                                registerButton.setText(R.string.register);
                                                Toast.makeText(RegisterActivity.this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });
                        }
                    } else {
                        registerButton.setEnabled(true);
                        registerButton.setText(R.string.register);
                        Toast.makeText(RegisterActivity.this,
                                task.getException() != null ?
                                        task.getException().getMessage() :
                                        getString(R.string.error_register_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}