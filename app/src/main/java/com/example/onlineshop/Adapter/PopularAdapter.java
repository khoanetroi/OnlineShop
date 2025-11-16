package com.example.onlineshop.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.example.onlineshop.Activity.DetailActivity;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.R;
import com.example.onlineshop.databinding.ViewholderPopularBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import android.os.Handler;
import android.os.Looper;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder>{
    ArrayList<ItemsModel> items;
    Context context;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public PopularAdapter(ArrayList<ItemsModel> items) {
        this.items = items;
    }
    
    /**
     * Safely update UI on main thread
     */
    private void updateUIOnMainThread(Runnable update) {
        if (context != null && mainHandler != null) {
            mainHandler.post(() -> {
                try {
                    if (update != null) {
                        update.run();
                    }
                } catch (Exception e) {
                    android.util.Log.e("PopularAdapter", "Error updating UI", e);
                }
            });
        }
    }

    @NonNull
    @Override
    public PopularAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ViewholderPopularBinding binding = ViewholderPopularBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ViewHolder(binding);
    }
    
    /**
     * Get wishlist reference for current user
     * Returns null if user is not logged in
     */
    private DatabaseReference getWishlistRef() {
        if (context == null) {
            return null;
        }
        
        // Check Firebase Auth first
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            return FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        }
        
        // Fallback to UserPreferences if Firebase Auth not available
        try {
            UserPreferences userPreferences = new UserPreferences(context);
            String uid = userPreferences.getUserId();
            if (uid != null && !uid.isEmpty()) {
                return FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
            }
        } catch (Exception e) {
            android.util.Log.e("PopularAdapter", "Error getting user preferences", e);
        }
        
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull PopularAdapter.ViewHolder holder, int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        
        // Clear previous listener references (single-use listeners auto-remove after firing)
        // We just clear references to prevent memory leaks and invalid updates
        holder.listener = null;
        holder.listenerRef = null;
        holder.itemView.setTag(null);
        
        ItemsModel item = items.get(position);
        if (item == null) {
            return;
        }

        holder.binding.titleTxt.setText(item.getTitle() != null ? item.getTitle() : "");
        holder.binding.priceTxt.setText("$" + String.format("%.2f", item.getPrice()));
        holder.binding.ratingTxt.setText("(" + item.getRating() + ")");
        holder.binding.oldPriceTxt.setText("$" + String.format("%.2f", item.getOldPrice()));
        if (item.getOffPercent() != null) {
            holder.binding.offPercentTxt.setText(item.getOffPercent() + " Off");
        }
        holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Load image with null check
        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            RequestOptions options = new RequestOptions();
            options = options.transform(new CenterInside());
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }

        // Store the item tag to verify ViewHolder is still valid
        holder.itemView.setTag(item.getTitle());

        // Check if item is favorited and set heart icon
        checkFavoriteStatus(item, holder);

        // Favorite button click listener - clear previous first
        holder.binding.favBtn.setOnClickListener(null);
        holder.binding.favBtn.setOnClickListener(v -> {
            try {
                // Verify context is still valid
                if (context == null) {
                    android.util.Log.e("PopularAdapter", "Context is null in favBtn click");
                    return;
                }
                
                // Verify holder is still valid
                if (holder == null || holder.binding == null || holder.binding.favBtn == null) {
                    android.util.Log.e("PopularAdapter", "Holder is invalid in favBtn click");
                    return;
                }
                
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= items.size()) {
                    return;
                }

                ItemsModel clickedItem = items.get(adapterPosition);
                if (clickedItem != null && holder.itemView.getTag() != null && 
                    clickedItem.getTitle() != null &&
                    clickedItem.getTitle().equals(holder.itemView.getTag())) {
                    toggleFavorite(clickedItem, holder);
                }
            } catch (Exception e) {
                android.util.Log.e("PopularAdapter", "Error in favBtn click listener", e);
                // Show error to user if possible
                if (context != null) {
                    Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Item click listener - clear previous first
        holder.itemView.setOnClickListener(null);
        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= items.size()) {
                return;
            }
            
            ItemsModel clickedItem = items.get(adapterPosition);
            if (clickedItem != null) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("object", clickedItem);
                context.startActivity(intent);
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).overridePendingTransition(com.example.onlineshop.R.anim.slide_in_right, com.example.onlineshop.R.anim.slide_out_left);
                }
            }
        });
    }

    private void checkFavoriteStatus(ItemsModel item, ViewHolder holder) {
        if (item == null || item.getTitle() == null || item.getTitle().isEmpty()) {
            if (holder.binding != null && holder.binding.favBtn != null) {
                holder.binding.favBtn.setImageResource(R.drawable.heart);
            }
            return;
        }

        // Store item title for validation
        String itemTitle = item.getTitle();

        // Get fresh wishlist reference
        DatabaseReference wishlistRef = getWishlistRef();
        if (wishlistRef == null) {
            if (holder.binding != null && holder.binding.favBtn != null) {
                holder.binding.favBtn.setImageResource(R.drawable.heart);
            }
            return;
        }

        try {
            // Create listener
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        // Verify ViewHolder is still valid (hasn't been recycled)
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        
                        // Verify the item is still the same
                        if (holder.itemView.getTag() == null || !itemTitle.equals(holder.itemView.getTag())) {
                            return;
                        }
                        
                        // Update UI on main thread
                        updateUIOnMainThread(() -> {
                            try {
                                if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                                    return;
                                }
                                if (holder.itemView.getTag() == null || !itemTitle.equals(holder.itemView.getTag())) {
                                    return;
                                }
                                if (holder.binding != null && holder.binding.favBtn != null) {
                                    if (snapshot.exists()) {
                                        holder.binding.favBtn.setImageResource(R.drawable.red_heart);
                                    } else {
                                        holder.binding.favBtn.setImageResource(R.drawable.heart);
                                    }
                                }
                            } catch (Exception e) {
                                android.util.Log.e("PopularAdapter", "Error updating favorite icon", e);
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("PopularAdapter", "Error updating favorite icon", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    android.util.Log.e("PopularAdapter", "Firebase error checking favorite: " + error.getMessage());
                }
            };
            
            // Store listener reference for cleanup
            holder.listener = listener;
            holder.listenerRef = wishlistRef;
            
            // Add listener (using addListenerForSingleValueEvent - it auto-removes after one use)
            try {
                wishlistRef.orderByChild("title").equalTo(itemTitle).addListenerForSingleValueEvent(listener);
            } catch (Exception e) {
                android.util.Log.e("PopularAdapter", "Error adding favorite status listener", e);
                if (holder.binding != null && holder.binding.favBtn != null) {
                    holder.binding.favBtn.setImageResource(R.drawable.heart);
                }
            }
            
        } catch (Exception e) {
            android.util.Log.e("PopularAdapter", "Error checking favorite status", e);
            if (holder.binding != null && holder.binding.favBtn != null) {
                holder.binding.favBtn.setImageResource(R.drawable.heart);
            }
        }
    }

    private void toggleFavorite(ItemsModel item, ViewHolder holder) {
        // Validate context
        if (context == null) {
            android.util.Log.e("PopularAdapter", "Context is null in toggleFavorite");
            return;
        }
        
        // Validate holder and binding
        if (holder == null || holder.binding == null) {
            android.util.Log.e("PopularAdapter", "Holder or binding is null in toggleFavorite");
            return;
        }

        // Get fresh wishlist reference
        DatabaseReference wishlistRef = getWishlistRef();
        if (wishlistRef == null) {
            Toast.makeText(context, "Please login to add favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item == null || item.getTitle() == null || item.getTitle().isEmpty()) {
            Toast.makeText(context, "Invalid item", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Check current favorite status
            wishlistRef.orderByChild("title").equalTo(item.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        // Verify ViewHolder is still valid
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        
                        // Verify the item is still the same
                        if (holder.itemView.getTag() == null || !item.getTitle().equals(holder.itemView.getTag())) {
                            return;
                        }
                        
                        if (snapshot.exists()) {
                            // Remove from wishlist
                            for (DataSnapshot child : snapshot.getChildren()) {
                                child.getRef().removeValue().addOnCompleteListener(task -> {
                                    updateUIOnMainThread(() -> {
                                        try {
                                            // Double-check ViewHolder is still valid before updating UI
                                            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                                                return;
                                            }
                                            if (holder.itemView.getTag() == null || !item.getTitle().equals(holder.itemView.getTag())) {
                                                return;
                                            }
                                            
                                            if (task.isSuccessful()) {
                                                if (holder.binding != null && holder.binding.favBtn != null) {
                                                    holder.binding.favBtn.setImageResource(R.drawable.heart);
                                                }
                                                if (context != null) {
                                                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                if (context != null) {
                                                    Toast.makeText(context, "Failed to remove favorite", Toast.LENGTH_SHORT).show();
                                                }
                                                android.util.Log.e("PopularAdapter", "Remove favorite error", task.getException());
                                            }
                                        } catch (Exception e) {
                                            android.util.Log.e("PopularAdapter", "Error in remove callback", e);
                                        }
                                    });
                                });
                            }
                        } else {
                            // Add to wishlist
                            wishlistRef.push().setValue(item).addOnSuccessListener(unused -> {
                                updateUIOnMainThread(() -> {
                                    try {
                                        // Double-check ViewHolder is still valid before updating UI
                                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                                            return;
                                        }
                                        if (holder.itemView.getTag() == null || !item.getTitle().equals(holder.itemView.getTag())) {
                                            return;
                                        }
                                        
                                        if (holder.binding != null && holder.binding.favBtn != null) {
                                            holder.binding.favBtn.setImageResource(R.drawable.red_heart);
                                        }
                                        if (context != null) {
                                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        android.util.Log.e("PopularAdapter", "Error in add success callback", ex);
                                    }
                                });
                            }).addOnFailureListener(e -> {
                                updateUIOnMainThread(() -> {
                                    try {
                                        if (context != null) {
                                            Toast.makeText(context, "Failed to add favorite: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                        android.util.Log.e("PopularAdapter", "Add favorite error", e);
                                    } catch (Exception ex) {
                                        android.util.Log.e("PopularAdapter", "Error in add failure callback", ex);
                                    }
                                });
                            });
                        }
                    } catch (Exception e) {
                        android.util.Log.e("PopularAdapter", "Error toggling favorite", e);
                        updateUIOnMainThread(() -> {
                            if (context != null) {
                                Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    updateUIOnMainThread(() -> {
                        try {
                            if (context != null) {
                                Toast.makeText(context, "Failed to update favorites: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                            android.util.Log.e("PopularAdapter", "Firebase error: " + error.getMessage(), error.toException());
                        } catch (Exception e) {
                            android.util.Log.e("PopularAdapter", "Error in onCancelled", e);
                        }
                    });
                }
            });
        } catch (Exception e) {
            android.util.Log.e("PopularAdapter", "Error in toggleFavorite", e);
            updateUIOnMainThread(() -> {
                if (context != null) {
                    Toast.makeText(context, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        // Clear listener references (addListenerForSingleValueEvent auto-removes after one use,
        // but we clear references to prevent updating recycled ViewHolders)
        holder.listener = null;
        holder.listenerRef = null;
        // Clear item tag
        holder.itemView.setTag(null);
        // Clear click listeners
        if (holder.binding != null) {
            if (holder.binding.favBtn != null) {
                holder.binding.favBtn.setOnClickListener(null);
            }
        }
        holder.itemView.setOnClickListener(null);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewholderPopularBinding binding;
        ValueEventListener listener;
        DatabaseReference listenerRef;
        
        public ViewHolder(ViewholderPopularBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
