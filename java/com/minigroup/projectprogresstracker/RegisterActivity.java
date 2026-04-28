package com.minigroup.projectprogresstracker;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private Spinner roleSpinner;
    private TextInputEditText etName, etEmail, etPassword, etTeacherId, etRollNo, etClassCode;
    private TextInputLayout layoutTeacherId, layoutRollNo, layoutClassCode;
    private MaterialButton registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupSpinner();
        setupRegisterLogic();
    }

    private void initViews() {
        roleSpinner = findViewById(R.id.roleSpinner);

        // Input Fields
        etName = findViewById(R.id.name);
        etEmail = findViewById(R.id.email);
        etPassword = findViewById(R.id.password);
        etTeacherId = findViewById(R.id.teacherId);
        etRollNo = findViewById(R.id.rollNo);
        etClassCode = findViewById(R.id.classCode);

        // Layout Containers (for visibility toggling)
        layoutTeacherId = findViewById(R.id.teacherIdLayout);
        layoutRollNo = findViewById(R.id.rollNoLayout);
        layoutClassCode = findViewById(R.id.classCodeLayout);

        registerBtn = findViewById(R.id.registerBtn);
    }

    private void setupSpinner() {
        String[] roles = {"Admin", "Teacher", "Student"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                roles
        );
        roleSpinner.setAdapter(adapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String role = parent.getItemAtPosition(position).toString();

                // Logic based on your requirements:
                // Teacher: TeacherID + ClassCode
                // Student: RollNo + ClassCode
                // Admin: Standard fields only

                switch (role) {
                    case "Teacher":
                        layoutTeacherId.setVisibility(View.VISIBLE);
                        layoutRollNo.setVisibility(View.GONE);
                        layoutClassCode.setVisibility(View.VISIBLE);
                        break;
                    case "Student":
                        layoutTeacherId.setVisibility(View.GONE);
                        layoutRollNo.setVisibility(View.VISIBLE);
                        layoutClassCode.setVisibility(View.VISIBLE);
                        break;
                    default: // Admin
                        layoutTeacherId.setVisibility(View.GONE);
                        layoutRollNo.setVisibility(View.GONE);
                        layoutClassCode.setVisibility(View.GONE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupRegisterLogic() {
        registerBtn.setOnClickListener(v -> {
            String userName = etName.getText().toString().trim();
            String userEmail = etEmail.getText().toString().trim();
            String userPassword = etPassword.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            // Validation
            if (userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all basic fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String extra = "";
            String code = "";

            if ("Teacher".equals(role)) {
                extra = etTeacherId.getText().toString().trim();
                code = etClassCode.getText().toString().trim();

                if (extra.isEmpty()) {
                    etTeacherId.setError("Teacher ID required");
                    return;
                }
                if (code.isEmpty()) {
                    etClassCode.setError("Class Code required");
                    return;
                }
            } else if ("Student".equals(role)) {
                extra = etRollNo.getText().toString().trim();
                code = etClassCode.getText().toString().trim();

                if (extra.isEmpty()) {
                    etRollNo.setError("Roll Number required");
                    return;
                }
                if (code.isEmpty()) {
                    etClassCode.setError("Class Code required");
                    return;
                }
            }

            // Create and store user
            User user = new User(userName, userEmail, userPassword, role, code, extra);

            // Assuming UserStorage handles context-based saving (SharedPreferences)
            UserStorage.addUser(this, user);

            Toast.makeText(this, "Registration successful for " + role, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}