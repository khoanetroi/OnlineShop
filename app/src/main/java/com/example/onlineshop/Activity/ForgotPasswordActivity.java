package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.backToLoginTxt.setOnClickListener(v -> finish());

        binding.resetBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.emailEdt.getText().toString().trim();

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

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.resetBtn.setEnabled(false);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.resetBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, 
                            "Password reset email sent. Please check your inbox.", 
                            Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorMessage = "Failed to send reset email";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.resetBtn.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
