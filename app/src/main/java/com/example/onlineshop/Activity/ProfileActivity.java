package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.AuthRepository;
import com.example.onlineshop.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(this);

        loadUserProfile();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        String userId = userPreferences.getUserId();
        String userName = userPreferences.getUserName();
        String userEmail = userPreferences.getUserEmail();

        if (userName != null) {
            binding.nameTxt.setText(userName);
        }
        
        if (userEmail != null) {
            binding.emailTxt.setText(userEmail);
        }

        if (userId != null) {
            authRepository.getUserProfile(userId).observe(this, user -> {
                if (user != null) {
                    binding.nameTxt.setText(user.getName());
                    binding.emailTxt.setText(user.getEmail());
                }
            });
        }
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.putExtra("select_home", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        binding.editProfileBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        binding.ordersBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Orders feature coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.addressBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Address management coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.logoutBtn.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                authRepository.logout();
                userPreferences.clearSession();
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("No", null)
            .show();
    }
}
