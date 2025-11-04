package com.example.onlineshop.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.onlineshop.Adapter.CategoryAdapter;
import com.example.onlineshop.Adapter.PopularAdapter;
import com.example.onlineshop.Adapter.SliderAdapter;
import com.example.onlineshop.Domain.BannerModel;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.R;
import com.example.onlineshop.ViewModel.MainViewModel;
import com.example.onlineshop.databinding.ActivityMainBinding;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    private MainViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        viewModel = new MainViewModel();
        initCategory();
        initSlider();
        initPopular();
        bottomNavigation();
        setVariable();
    }

    private void setVariable() {
        binding.imageView5.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CartActivity.class)));
    }

    private void bottomNavigation() {
        binding.bottomNavigation.setItemSelected(R.id.home,true);
        binding.bottomNavigation.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int i) {
//                switch (i) {
//                    case R.id.home:
//                        // Already on home
//                        break;
//                    case R.id.favorites:
//                        // TODO: Navigate to favorites
//                        break;
//                    case R.id.cart:
//                        startActivity(new Intent(MainActivity.this, CartActivity.class));
//                        break;
//                    case R.id.profile:
//                        // TODO: Navigate to profile
//                        break;
//                }
            }
        });
    }

    private void initPopular() {
        binding.progressBarPopular.setVisibility(View.VISIBLE);
        viewModel.loadPopular().observeForever(itemsModels -> {
            if(itemsModels!=null && !itemsModels.isEmpty()) {
                binding.popularView.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.HORIZONTAL,false));
                binding.popularView.setAdapter(new PopularAdapter(itemsModels));
                binding.popularView.setNestedScrollingEnabled(true);
            }
            binding.progressBarPopular.setVisibility(View.GONE);
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
        binding.viewPagerSlider.setAdapter(new SliderAdapter(bannerModels,binding.viewPagerSlider));
        binding.viewPagerSlider.setClipToPadding(false);
        binding.viewPagerSlider.setClipChildren(false);
        binding.viewPagerSlider.setOffscreenPageLimit(3);
        binding.viewPagerSlider.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer=new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer);
    }

    private void initCategory() {
        binding.progressBarCategory.setVisibility(View.VISIBLE);
        viewModel.loadCategory().observeForever(categoryModels -> {
            binding.progressBarCategory.setVisibility(View.GONE);
            binding.categoryView.setLayoutManager(new LinearLayoutManager(
                    MainActivity.this,
                    LinearLayoutManager.HORIZONTAL,
                    false
            ));
            binding.categoryView.setAdapter(new CategoryAdapter(categoryModels));
            binding.categoryView.setNestedScrollingEnabled(true);
            binding.progressBarCategory.setVisibility(View.GONE);
        });
    }
}