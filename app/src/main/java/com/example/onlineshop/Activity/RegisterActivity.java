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
import com.example.onlineshop.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(this);

        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.registerBtn.setOnClickListener(v -> registerUser());
        
        binding.loginTxt.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void registerUser() {
        String name = binding.nameEdt.getText().toString().trim();
        String email = binding.emailEdt.getText().toString().trim();
        String password = binding.passwordEdt.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.nameEdt.setError("Họ và tên là bắt buộc");
            binding.nameEdt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailEdt.setError("Email là bắt buộc");
            binding.emailEdt.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEdt.setError("Vui lòng nhập email hợp lệ");
            binding.emailEdt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEdt.setError("Mật khẩu là bắt buộc");
            binding.passwordEdt.requestFocus();
            return;
        }

        if (password.length() < 6) {
            binding.passwordEdt.setError("Mật khẩu phải có ít nhất 6 ký tự");
            binding.passwordEdt.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordEdt.setError("Vui lòng xác nhận mật khẩu");
            binding.confirmPasswordEdt.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEdt.setError("Mật khẩu không khớp");
            binding.confirmPasswordEdt.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerBtn.setEnabled(false);

        authRepository.register(email, password, name).observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.registerBtn.setEnabled(true);

            if (result.success) {
                userPreferences.saveUserSession(
                    result.user.getUid(),
                    result.user.getEmail(),
                    result.user.getName()
                );
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, MainContainerActivity.class));
                finish();
            } else {
                Toast.makeText(this, result.message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
