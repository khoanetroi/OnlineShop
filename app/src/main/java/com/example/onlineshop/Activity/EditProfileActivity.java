package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.onlineshop.Domain.UserModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.AuthRepository;
import com.example.onlineshop.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    private ActivityEditProfileBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;
    private UserModel currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(this);

        loadUserProfile();
        setupListeners();
    }

    private void loadUserProfile() {
        String userId = userPreferences.getUserId();
        
        if (userId != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            authRepository.getUserProfile(userId).observe(this, user -> {
                binding.progressBar.setVisibility(View.GONE);
                if (user != null) {
                    currentUser = user;
                    binding.nameEdt.setText(user.getName());
                    binding.emailEdt.setText(user.getEmail());
                    binding.phoneEdt.setText(user.getPhone());
                    binding.addressEdt.setText(user.getAddress());
                }
            });
        }
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.changePhotoTxt.setOnClickListener(v -> {
            Toast.makeText(this, "Photo upload feature coming soon", Toast.LENGTH_SHORT).show();
        });

        binding.saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.nameEdt.getText().toString().trim();
        String phone = binding.phoneEdt.getText().toString().trim();
        String address = binding.addressEdt.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.nameEdt.setError("Name is required");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.saveBtn.setEnabled(false);

        authRepository.updateUserProfile(currentUser).observe(this, success -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.saveBtn.setEnabled(true);

            if (success) {
                userPreferences.saveUserSession(
                    currentUser.getUid(),
                    currentUser.getEmail(),
                    currentUser.getName()
                );
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
