package com.example.onlineshop.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.onlineshop.Adapter.FavoriteAdapter;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ActivityFavoritesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavoritesFragment extends Fragment {
    private ActivityFavoritesBinding binding;
    private final ArrayList<ItemsModel> allItems = new ArrayList<>();
    private final ArrayList<ItemsModel> displayItems = new ArrayList<>();
    private FavoriteAdapter adapter;
    private DatabaseReference wishlistRef;
    private ValueEventListener wishlistListener;

    private enum SortMode { NONE, LATEST, MOST_POPULAR, CHEAPEST }
    private SortMode currentSort = SortMode.NONE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityFavoritesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Hide bottom navigation from fragment layout since it's in container
        binding.bottomNavigation.setVisibility(View.GONE);
        
        initFirebase();
        initRecyclerView();
        initListeners();
        loadFavorites();
    }

    private void initFirebase() {
        // Use same logic as PopularAdapter - check Firebase Auth first, then fallback to UserPreferences
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        } else {
            // Fallback to UserPreferences if Firebase Auth not available
            UserPreferences userPreferences = new UserPreferences(requireContext());
            uid = userPreferences.getUserId();
        }
        
        if (uid != null && !uid.isEmpty()) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        }
    }

    private void initRecyclerView() {
        binding.favoriteView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        adapter = new FavoriteAdapter(displayItems, requireContext());
        binding.favoriteView.setAdapter(adapter);
    }

    private void initListeners() {
        binding.searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilterAndSort();
            }
        });

        binding.chipAll.setOnClickListener(v -> {
            currentSort = SortMode.NONE;
            updateChipStates();
            applyFilterAndSort();
        });

        binding.chipLatest.setOnClickListener(v -> {
            currentSort = SortMode.LATEST;
            updateChipStates();
            applyFilterAndSort();
        });

        binding.chipMostPopular.setOnClickListener(v -> {
            currentSort = SortMode.MOST_POPULAR;
            updateChipStates();
            applyFilterAndSort();
        });

        binding.chipCheapest.setOnClickListener(v -> {
            currentSort = SortMode.CHEAPEST;
            updateChipStates();
            applyFilterAndSort();
        });

        binding.cartBtn.setOnClickListener(v -> {
            // Navigate to MyCart fragment via parent activity
            if (getActivity() instanceof com.example.onlineshop.Activity.MainContainerActivity) {
                com.example.onlineshop.Activity.MainContainerActivity activity = 
                    (com.example.onlineshop.Activity.MainContainerActivity) getActivity();
                activity.navigateToMyCart();
            }
        });

        binding.notificationBtn.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.onlineshop.Activity.NotificationActivity.class);
            startActivity(intent);
        });
    }

    private void loadFavorites() {
        if (wishlistRef == null) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        try {
            wishlistListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (binding == null || adapter == null) {
                        return; // Fragment destroyed
                    }
                    
                    try {
                        allItems.clear();
                        if (snapshot.exists() && snapshot.hasChildren()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                ItemsModel item = child.getValue(ItemsModel.class);
                                if (item != null) {
                                    allItems.add(item);
                                }
                            }
                        }
                        applyFilterAndSort();
                    } catch (Exception e) {
                        android.util.Log.e("FavoritesFragment", "Error processing favorites", e);
                    } finally {
                        if (binding != null) {
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (binding != null) {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                    android.util.Log.e("FavoritesFragment", "Firebase error: " + error.getMessage());
                }
            };
            wishlistRef.addValueEventListener(wishlistListener);
        } catch (Exception e) {
            android.util.Log.e("FavoritesFragment", "Error setting up listener", e);
            if (binding != null) {
                binding.progressBar.setVisibility(View.GONE);
                binding.emptyTxt.setVisibility(View.VISIBLE);
            }
        }
    }

    private void applyFilterAndSort() {
        if (binding == null || adapter == null) {
            return; // Fragment destroyed
        }
        
        try {
            String query = binding.searchEdt.getText().toString().trim().toLowerCase();

            displayItems.clear();
            for (ItemsModel item : allItems) {
                if (query.isEmpty() || (item.getTitle() != null && item.getTitle().toLowerCase().contains(query))) {
                    displayItems.add(item);
                }
            }

            switch (currentSort) {
                case LATEST:
                    Collections.reverse(displayItems);
                    break;
                case MOST_POPULAR:
                    Collections.sort(displayItems, new Comparator<ItemsModel>() {
                        @Override
                        public int compare(ItemsModel o1, ItemsModel o2) {
                            return Double.compare(o2.getRating(), o1.getRating());
                        }
                    });
                    break;
                case CHEAPEST:
                    Collections.sort(displayItems, new Comparator<ItemsModel>() {
                        @Override
                        public int compare(ItemsModel o1, ItemsModel o2) {
                            return Double.compare(o1.getPrice(), o2.getPrice());
                        }
                    });
                    break;
                case NONE:
                default:
                    break;
            }

            adapter.notifyDataSetChanged();
            binding.emptyTxt.setVisibility(displayItems.isEmpty() ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            android.util.Log.e("FavoritesFragment", "Error applying filter/sort", e);
        }
    }

    private void updateChipStates() {
        int selectedBg = getResources().getColor(R.color.orange);
        int unselectedBg = getResources().getColor(R.color.white);
        int selectedText = getResources().getColor(R.color.white);
        int unselectedText = getResources().getColor(R.color.dark_gray);

        setChipState(binding.chipAll, currentSort == SortMode.NONE, selectedBg, unselectedBg, selectedText, unselectedText);
        setChipState(binding.chipLatest, currentSort == SortMode.LATEST, selectedBg, unselectedBg, selectedText, unselectedText);
        setChipState(binding.chipMostPopular, currentSort == SortMode.MOST_POPULAR, selectedBg, unselectedBg, selectedText, unselectedText);
        setChipState(binding.chipCheapest, currentSort == SortMode.CHEAPEST, selectedBg, unselectedBg, selectedText, unselectedText);
    }

    private void setChipState(android.widget.TextView chip, boolean selected, int selectedBg, int unselectedBg, int selectedText, int unselectedText) {
        chip.setBackgroundColor(selected ? selectedBg : unselectedBg);
        chip.setTextColor(selected ? selectedText : unselectedText);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener to prevent memory leaks
        if (wishlistRef != null && wishlistListener != null) {
            wishlistRef.removeEventListener(wishlistListener);
            wishlistListener = null;
        }
        binding = null;
    }
}

