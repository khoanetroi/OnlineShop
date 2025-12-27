package com.example.onlineshop.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.onlineshop.Adapter.PopularAdapter;
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.databinding.ActivitySearchBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private ArrayList<ItemsModel> allItems = new ArrayList<>();
    private ArrayList<ItemsModel> filteredItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        loadAllItems();
        setupSearch();
    }

    private void initViews() {
        binding.backBtn.setOnClickListener(v -> finish());
        
        binding.searchResultsView.setLayoutManager(new GridLayoutManager(this, 2));
        
        // Focus on search input
        binding.searchInput.requestFocus();
    }

    private void loadAllItems() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyTxt.setVisibility(View.GONE);
        
        FirebaseDatabase.getInstance().getReference("Items")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    allItems.clear();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        ItemsModel item = child.getValue(ItemsModel.class);
                        if (item != null) {
                            allItems.add(item);
                        }
                    }
                    binding.progressBar.setVisibility(View.GONE);
                    
                    // Show all items initially
                    filteredItems.clear();
                    filteredItems.addAll(allItems);
                    updateResults();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    binding.progressBar.setVisibility(View.GONE);
                }
            });
    }

    private void setupSearch() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filterItems(binding.searchInput.getText().toString());
                return true;
            }
            return false;
        });

        binding.clearBtn.setOnClickListener(v -> {
            binding.searchInput.setText("");
            filteredItems.clear();
            filteredItems.addAll(allItems);
            updateResults();
        });
    }

    private void filterItems(String query) {
        filteredItems.clear();
        
        if (query.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (ItemsModel item : allItems) {
                if (item.getTitle() != null && item.getTitle().toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                } else if (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) {
                    filteredItems.add(item);
                }
            }
        }
        
        updateResults();
    }

    private void updateResults() {
        if (filteredItems.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.searchResultsView.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.searchResultsView.setVisibility(View.VISIBLE);
            binding.searchResultsView.setAdapter(new PopularAdapter(filteredItems));
        }
        
        binding.resultCountTxt.setText(filteredItems.size() + " sản phẩm");
    }
}
