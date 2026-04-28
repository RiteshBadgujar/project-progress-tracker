package com.minigroup.projectprogresstracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText email, password, classCode;
    Spinner roleSpinner;
    LinearLayout classCodeLayout;
    Button loginBtn;
    TextView forgotPassword, createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- SESSION CHECK START ---
        // Check if a user is already logged in before loading the layout
        User activeUser = SessionManager.getUser(this);
        if (activeUser != null) {
            navigateToDashboard(activeUser);
            return; // Stop execution of onCreate for this activity
        }
        // --- SESSION CHECK END ---

        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        classCode = findViewById(R.id.classCode);
        roleSpinner = findViewById(R.id.roleSpinner);
        classCodeLayout = findViewById(R.id.classCodeLayout);
        loginBtn = findViewById(R.id.loginBtn);
        forgotPassword = findViewById(R.id.forgotPassword);
        createAccount = findViewById(R.id.createAccount);

        String[] roles = {"Admin", "Teacher", "Student"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );
        roleSpinner.setAdapter(adapter);

        // Handle Deep Link for users who aren't logged in yet
        handleDeepLink();

        // Show / hide class code
        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = parent.getItemAtPosition(position).toString();

                if ("Teacher".equals(role) || "Student".equals(role)) {
                    classCodeLayout.setVisibility(View.VISIBLE);
                } else {
                    classCodeLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Go to register
        createAccount.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );

        // LOGIN BUTTON
        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim().toLowerCase();
            String userPassword = password.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            String code = classCode.getText().toString().trim().toUpperCase();

            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Enter Email & Password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                Toast.makeText(this, "Invalid Email Format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (("Teacher".equals(role) || "Student".equals(role)) && code.isEmpty()) {
                Toast.makeText(this, "Enter Class Code", Toast.LENGTH_SHORT).show();
                return;
            }

            User validatedUser = UserStorage.validateUser(this, userEmail, userPassword, role);

            if (validatedUser == null) {
                forgotPassword.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Teacher".equals(role) || "Student".equals(role)) {
                if (!ClassStorage.isValidCode(this, code)) {
                    Toast.makeText(this, "Invalid Class Code", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (validatedUser.getClassCode() == null ||
                        !validatedUser.getClassCode().equalsIgnoreCase(code)) {
                    Toast.makeText(this, "You are not assigned to this class", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Save the session so the app remembers the user
            SessionManager.saveUser(this, validatedUser);
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            navigateToDashboard(validatedUser);
        });

        // Forgot password
        forgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    /**
     * Helper method to handle redirection logic based on user role
     */
    private void navigateToDashboard(User user) {
        Intent intent;
        String role = user.getRole();

        if ("Admin".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else if ("Teacher".equalsIgnoreCase(role)) {
            intent = new Intent(this, TeacherDashboardActivity.class);
        } else {
            intent = new Intent(this, StudentDashboardActivity.class);
        }

        startActivity(intent);
        finish(); // Remove LoginActivity from backstack
    }

    // DEEP LINK HANDLER
    private void handleDeepLink() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            String code = data.getQueryParameter("code");
            if (code != null) {
                classCode.setText(code.toUpperCase());
                roleSpinner.setSelection(2); // Student auto-select
                Toast.makeText(this, "Class Code Auto Filled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}