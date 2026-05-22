package com.minigroup.projectprogresstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Use Material Components for better control
    private TextInputEditText emailInput, codeInput, newPasswordInput;
    private TextInputLayout layoutCode, layoutNewPassword;
    private MaterialButton sendCodeBtn, verifyBtn, resetBtn;

    private String generatedCode = null;
    private String verifiedEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Force Light Mode to match your UI design
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Inputs
        emailInput = findViewById(R.id.fpEmail);
        codeInput = findViewById(R.id.fpCode);
        newPasswordInput = findViewById(R.id.fpNewPassword);

        // Initialize Layout Containers (The boxes)
        layoutCode = findViewById(R.id.layoutFpCode);
        layoutNewPassword = findViewById(R.id.layoutFpNewPassword);

        // Initialize Buttons
        sendCodeBtn = findViewById(R.id.fpSendCodeBtn);
        verifyBtn = findViewById(R.id.fpVerifyBtn);
        resetBtn = findViewById(R.id.fpResetBtn);

        // Initial Visibility State (Hide step 2 and 3)
        toggleStep2(false);
        toggleStep3(false);

        // 🔹 STEP 1: Send Code
        sendCodeBtn.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                return;
            }

            if (!emailExists(email)) {
                Toast.makeText(this, "This email is not registered", Toast.LENGTH_SHORT).show();
                return;
            }

            generatedCode = generateCode();
            verifiedEmail = email;

            // 🔥 Simulation: In a real app, this would be an API call to send an email
            Toast.makeText(this, "DEBUG ONLY: Code is " + generatedCode, Toast.LENGTH_LONG).show();

            toggleStep2(true);
            sendCodeBtn.setText("Resend Code"); // Give feedback to user
        });

        // 🔹 STEP 2: Verify Code
        verifyBtn.setOnClickListener(v -> {
            String enteredCode = codeInput.getText().toString().trim();

            if (generatedCode != null && generatedCode.equals(enteredCode)) {
                toggleStep3(true);
                Toast.makeText(this, "Code Verified", Toast.LENGTH_SHORT).show();
            } else {
                codeInput.setError("Invalid verification code");
            }
        });

        // 🔹 STEP 3: Reset Password
        resetBtn.setOnClickListener(v -> {
            String newPass = newPasswordInput.getText().toString().trim();

            if (newPass.length() < 6) {
                newPasswordInput.setError("Password must be at least 6 characters");
                return;
            }

            ArrayList<User> users = UserStorage.getUsers(this);
            boolean updated = false;

            for (User u : users) {
                if (u.getEmail().equalsIgnoreCase(verifiedEmail)) {
                    u.setPassword(newPass);
                    updated = true;
                    break;
                }
            }

            if (updated) {
                UserStorage.saveUsers(this, users);
                Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_LONG).show();
                finish(); // Go back to login
            } else {
                Toast.makeText(this, "Error: User not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper methods to keep onCreate clean
    private void toggleStep2(boolean show) {
        layoutCode.setVisibility(show ? View.VISIBLE : View.GONE);
        verifyBtn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void toggleStep3(boolean show) {
        layoutNewPassword.setVisibility(show ? View.VISIBLE : View.GONE);
        resetBtn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private boolean emailExists(String email) {
        for (User u : UserStorage.getUsers(this)) {
            if (u.getEmail().equalsIgnoreCase(email)) {
                return true;
            }
        }
        return false;
    }

    private String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}