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
import com.example.onlineshop.Model.BannerModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.R;
import com.example.onlineshop.ViewModel.MainViewModel;
import com.example.onlineshop.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        updateCartBadge();
    }
    
    private void initUserGreeting() {
        String userName = userPreferences.getUserName();
        if (userName != null && !userName.isEmpty()) {
            binding.textView5.setText(userName);
        } else {
            String userEmail = userPreferences.getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                String nameFromEmail = userEmail.split("@")[0];
                nameFromEmail = nameFromEmail.substring(0, 1).toUpperCase() + nameFromEmail.substring(1);
                binding.textView5.setText(nameFromEmail);
            } else {
                binding.textView5.setText("Khách");
            }
        }
    }

    private void setVariable() {
        binding.imageView2.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                com.example.onlineshop.Activity.MainContainerActivity activity = 
                    (com.example.onlineshop.Activity.MainContainerActivity) getActivity();
                activity.navigateToFragment(R.id.profile);
            }
        });

        binding.cartBtn.setOnClickListener(v -> {
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                com.example.onlineshop.Activity.MainContainerActivity activity =
                        (com.example.onlineshop.Activity.MainContainerActivity) getActivity();
                activity.navigateToMyCart();
            }
        });

        binding.editTextText.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), com.example.onlineshop.Activity.SearchActivity.class);
            startActivity(intent);
        });
        
        binding.editTextText.setFocusable(false);
        binding.editTextText.setFocusableInTouchMode(false);

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

    private void updateCartBadge() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ? 
                        FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId != null) {
            DatabaseReference cartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(userId)
                    .child("cart");
            
            cartRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int itemCount = (int) snapshot.getChildrenCount();
                    updateBadgeUI(itemCount);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    updateBadgeFromLocalCart();
                }
            });
        } else {
            updateBadgeFromLocalCart();
        }
    }
    
    private void updateBadgeFromLocalCart() {
        ManagmentCart managmentCart = new ManagmentCart(requireContext());
        int itemCount = managmentCart.getListCart().size();
        updateBadgeUI(itemCount);
    }
    
    private void updateBadgeUI(int count) {
        if (binding != null) {
            if (count > 0) {
                binding.cartBadge.setVisibility(View.VISIBLE);
                binding.cartBadge.setText(String.valueOf(count > 99 ? "99+" : count));
            } else {
                binding.cartBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartBadge();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

