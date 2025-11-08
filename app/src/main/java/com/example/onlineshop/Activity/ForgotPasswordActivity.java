package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.databinding.ActivityForgotPasswordBinding;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.backToLoginTxt.setOnClickListener(v -> finish());

        binding.resetBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Feature in development", Toast.LENGTH_SHORT).show();
        });
    }
}
