package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.onlineshop.Fragment.ChangePasswordFragment;
import com.example.onlineshop.Fragment.EditProfileFragment;
import com.example.onlineshop.Fragment.FavoritesFragment;
import com.example.onlineshop.Fragment.HomeFragment;
import com.example.onlineshop.Fragment.MyCartFragment;
import com.example.onlineshop.Fragment.MyOrderFragment;
import com.example.onlineshop.Fragment.SettingsFragment;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityMainContainerBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

public class MainContainerActivity extends AppCompatActivity {
    public ActivityMainContainerBinding binding;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainContainerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
        
        if (getIntent().getBooleanExtra("select_profile", false)) {
            binding.bottomNavigation.setItemSelected(R.id.profile, true);
            loadFragment(new SettingsFragment(), false);
        } else if (getIntent().getBooleanExtra("select_my_order", false)) {
            binding.bottomNavigation.setItemSelected(R.id.my_order, true);
            loadFragment(new MyOrderFragment(), false);
        } else {
            loadFragment(new HomeFragment(), false);
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home, true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
                Fragment fragment = null;
                if (i == R.id.home) {
                    fragment = new HomeFragment();
                } else if (i == R.id.favorites) {
                    fragment = new FavoritesFragment();
                } else if (i == R.id.my_order) {
                    fragment = new MyOrderFragment();
                } else if (i == R.id.profile) {
                    fragment = new SettingsFragment();
                }

                if (fragment != null) {
                    loadFragment(fragment, false);
                }
            }
        });
    }
    
    public void navigateToFragment(int navItemId) {
        binding.bottomNavigation.setItemSelected(navItemId, true);
    }

    public void navigateToMyCart() {
        loadFragment(new MyCartFragment(), true);
    }

    public void navigateToEditProfile() {
        loadFragment(new EditProfileFragment(), true);
    }

    public void navigateToChangePassword() {
        loadFragment(new ChangePasswordFragment(), true);
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        transaction.setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
        );
        
        transaction.replace(R.id.fragmentContainer, fragment);
        
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        
        transaction.commit();
        currentFragment = fragment;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}

