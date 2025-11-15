package com.example.onlineshop.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.AuthRepository;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivitySettingsBinding;

public class SettingsFragment extends Fragment {
    private ActivitySettingsBinding binding;
    private AuthRepository authRepository;
    private UserPreferences userPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivitySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        authRepository = new AuthRepository();
        userPreferences = new UserPreferences(requireContext());

        setupListeners();
    }

    private void setupListeners() {
        binding.editProfileRow.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                ((com.example.onlineshop.Activity.MainContainerActivity) getActivity()).navigateToEditProfile();
            }
        });

        binding.changePasswordRow.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                ((com.example.onlineshop.Activity.MainContainerActivity) getActivity()).navigateToChangePassword();
            }
        });

        binding.notificationsRow.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });

        binding.securityRow.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Security options coming soon", Toast.LENGTH_SHORT).show()
        );

        binding.languageRow.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Language selection coming soon", Toast.LENGTH_SHORT).show()
        );

        binding.legalRow.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Legal and policies coming soon", Toast.LENGTH_SHORT).show()
        );

        binding.helpRow.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Help & support coming soon", Toast.LENGTH_SHORT).show()
        );

        binding.logoutRow.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                authRepository.logout();
                userPreferences.clearSession();
                Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            })
            .setNegativeButton("No", null)
            .show();
    }
}

