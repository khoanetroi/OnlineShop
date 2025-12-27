package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.onlineshop.Adapter.PopularAdapter;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.ViewModel.MainViewModel;
import com.example.onlineshop.databinding.ActivityProductListBinding;

import java.util.ArrayList;

public class ProductListActivity extends AppCompatActivity {
    private ActivityProductListBinding binding;
    private MainViewModel viewModel;
    private String listType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new MainViewModel();
        listType = getIntent().getStringExtra("listType");
        String title = getIntent().getStringExtra("title");

        if (title != null) {
            binding.titleTxt.setText(title);
        }

        binding.backBtn.setOnClickListener(v -> finish());
        binding.productsView.setLayoutManager(new GridLayoutManager(this, 2));

        loadProducts();
    }

    private void loadProducts() {
        binding.progressBar.setVisibility(View.VISIBLE);

        if ("popular".equals(listType)) {
            viewModel.loadPopular().observe(this, this::displayProducts);
        } else if ("new_arrivals".equals(listType)) {
            viewModel.loadNewArrivals().observe(this, this::displayProducts);
        } else if ("recommended".equals(listType)) {
            viewModel.loadRecommended().observe(this, this::displayProducts);
        } else {
            viewModel.loadPopular().observe(this, this::displayProducts);
        }
    }

    private void displayProducts(ArrayList<ItemsModel> items) {
        binding.progressBar.setVisibility(View.GONE);
        if (items != null && !items.isEmpty()) {
            binding.productsView.setAdapter(new PopularAdapter(items));
            binding.emptyTxt.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.VISIBLE);
        }
    }
}
