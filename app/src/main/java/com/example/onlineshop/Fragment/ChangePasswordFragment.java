package com.example.onlineshop.Fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityChangePasswordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {
    private ActivityChangePasswordBinding binding;
    private FirebaseAuth firebaseAuth;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        firebaseAuth = FirebaseAuth.getInstance();

        setupListeners();
    }

    private void setupListeners() {
        binding.backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Password visibility toggles
        binding.newPasswordToggle.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            int selection = binding.newPasswordEdt.getSelectionEnd();
            if (isNewPasswordVisible) {
                binding.newPasswordEdt.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.newPasswordToggle.setImageResource(android.R.drawable.ic_menu_revert);
            } else {
                binding.newPasswordEdt.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
                binding.newPasswordToggle.setImageResource(android.R.drawable.ic_menu_view);
            }
            binding.newPasswordEdt.setSelection(selection);
        });

        binding.confirmPasswordToggle.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            int selection = binding.confirmPasswordEdt.getSelectionEnd();
            if (isConfirmPasswordVisible) {
                binding.confirmPasswordEdt.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                binding.confirmPasswordToggle.setImageResource(android.R.drawable.ic_menu_revert);
            } else {
                binding.confirmPasswordEdt.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
                binding.confirmPasswordToggle.setImageResource(android.R.drawable.ic_menu_view);
            }
            binding.confirmPasswordEdt.setSelection(selection);
        });

        binding.changeBtn.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String newPassword = binding.newPasswordEdt.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEdt.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            binding.newPasswordEdt.setError("New password is required");
            return;
        } else {
            binding.newPasswordEdt.setError(null);
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordEdt.setError("Please confirm password");
            return;
        } else {
            binding.confirmPasswordEdt.setError(null);
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.confirmPasswordEdt.setError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            binding.newPasswordEdt.setError("Password must be at least 6 characters");
            return;
        }

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "You need to be logged in to change password", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.changeBtn.setEnabled(false);

        user.updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.changeBtn.setEnabled(true);

                    if (task.isSuccessful()) {
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    } else {
                        String message = task.getException() != null ? task.getException().getMessage() : "Failed to update password";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

