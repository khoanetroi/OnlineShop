package com.example.onlineshop.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlineshop.Domain.UserModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.AuthRepository;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityEditProfileBinding;

public class EditProfileFragment extends Fragment {
    private ActivityEditProfileBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;
    private UserModel currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(requireContext());

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
                    if (user.getPhone() != null) {
                        binding.phoneEdt.setText(user.getPhone());
                    }
                }
            });
        }
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = binding.nameEdt.getText().toString().trim();
        String phone = binding.phoneEdt.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.nameEdt.setError("Name is required");
            return;
        }

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setPhone(phone);

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
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            } else {
                Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

