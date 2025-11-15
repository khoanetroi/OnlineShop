package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.AuthRepository;
import com.example.onlineshop.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.loginBtn.setOnClickListener(v -> loginUser());
        
        binding.registerTxt.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.forgotPasswordTxt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String email = binding.emailEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.emailEdt.setError("Email is required");
            binding.emailEdt.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEdt.setError("Please enter a valid email");
            binding.emailEdt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEdt.setError("Password is required");
            binding.passwordEdt.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginBtn.setEnabled(false);

        authRepository.login(email, password).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginBtn.setEnabled(true);

            if (result.success) {
                userPreferences.saveUserSession(
                    result.user.getUid(),
                    result.user.getEmail(),
                    result.user.getName()
                );
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainContainerActivity.class));
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
