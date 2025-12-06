package com.example.onlineshop.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.onlineshop.Adapter.CategoryAdapter;
import com.example.onlineshop.Adapter.PopularAdapter;
import com.example.onlineshop.Adapter.SliderAdapter;
import com.example.onlineshop.Domain.BannerModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.R;
import com.example.onlineshop.ViewModel.MainViewModel;
import com.example.onlineshop.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private UserPreferences userPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        binding.bottomNavigation.setVisibility(View.GONE);
        
        userPreferences = new UserPreferences(requireContext());
        viewModel = new MainViewModel();
        
        initUserGreeting();
        initCategory();
        initSlider();
        initPopular();
        initNewArrivals();
        initRecommended();
        setVariable();
    }
    
    private void initUserGreeting() {
        // Get username from UserPreferences and display it
        String userName = userPreferences.getUserName();
        if (userName != null && !userName.isEmpty()) {
            binding.textView5.setText(userName);
        } else {
            // Fallback to email if name is not available
            String userEmail = userPreferences.getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                // Extract name from email (before @)
                String nameFromEmail = userEmail.split("@")[0];
                // Capitalize first letter
                nameFromEmail = nameFromEmail.substring(0, 1).toUpperCase() + nameFromEmail.substring(1);
                binding.textView5.setText(nameFromEmail);
            } else {
                binding.textView5.setText("Khách");
            }
        }
    }

    private void setVariable() {
        // Profile image click - navigate to profile
        binding.imageView2.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                com.example.onlineshop.Activity.MainContainerActivity activity = 
                    (com.example.onlineshop.Activity.MainContainerActivity) getActivity();
                activity.navigateToFragment(R.id.profile);
            }
        });

        // Search bar click - navigate to search activity
        binding.editTextText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.SearchActivity.class);
            startActivity(intent);
        });
        
        binding.editTextText.setFocusable(false);
        binding.editTextText.setFocusableInTouchMode(false);

        // See All buttons
        binding.seeAllPopular.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.ProductListActivity.class);
            intent.putExtra("listType", "popular");
            intent.putExtra("title", "Sản Phẩm Phổ Biến");
            startActivity(intent);
        });

        binding.seeAllNewArrivals.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.ProductListActivity.class);
            intent.putExtra("listType", "new_arrivals");
            intent.putExtra("title", "Hàng Mới Về");
            startActivity(intent);
        });

        binding.seeAllRecommended.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.ProductListActivity.class);
            intent.putExtra("listType", "recommended");
            intent.putExtra("title", "Gợi Ý Cho Bạn");
            startActivity(intent);
        });
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observeForever(itemsModels -> {
            if(itemsModels!=null && !itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
        });
    }

    private void initNewArrivals() {
        binding.progressBarNewArrivals.setVisibility(View.VISIBLE);
        viewModel.loadNewArrivals().observeForever(itemsModels -> {
            if(itemsModels != null && !itemsModels.isEmpty()) {
                binding.newArrivalsView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.newArrivalsView.setAdapter(new PopularAdapter(itemsModels));
                binding.newArrivalsView.setNestedScrollingEnabled(true);
            }
            binding.progressBarNewArrivals.setVisibility(View.GONE);
        });
    }

    private void initRecommended() {
        binding.progressBarRecommended.setVisibility(View.VISIBLE);
        viewModel.loadRecommended().observeForever(itemsModels -> {
            if(itemsModels != null && !itemsModels.isEmpty()) {
                binding.recommendedView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
                binding.recommendedView.setAdapter(new PopularAdapter(itemsModels));
                binding.recommendedView.setNestedScrollingEnabled(true);
            }
            binding.progressBarRecommended.setVisibility(View.GONE);
        });
    }

    private void initSlider() {
        binding.progressBarSlider.setVisibility(View.VISIBLE);
        viewModel.loadBanner().observeForever(bannerModels -> {
            if(bannerModels!=null && !bannerModels.isEmpty()) {
                banners(bannerModels);
                binding.progressBarSlider.setVisibility(View.GONE);
            }
        });
    }

    private void banners(ArrayList<BannerModel> bannerModels) {
        binding.viewPagerSlider.setAdapter(new SliderAdapter(bannerModels, binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        if (binding.viewPagerSlider.getChildAt(0) != null) {
            binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        }

        androidx.viewpager2.widget.CompositePageTransformer compositePageTransformer = new androidx.viewpager2.widget.CompositePageTransformer();
        compositePageTransformer.addTransformer(new androidx.viewpager2.widget.MarginPageTransformer(40));
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observeForever(categoryModels -> {
            binding.progressBarCategory.setVisibility(View.GONE);
            binding.categoryView.setLayoutManager(new LinearLayoutManager(
                    requireContext(),
                    LinearLayoutManager.HORIZONTAL,
                    false
            ));
            binding.categoryView.setAdapter(new CategoryAdapter(categoryModels));
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

