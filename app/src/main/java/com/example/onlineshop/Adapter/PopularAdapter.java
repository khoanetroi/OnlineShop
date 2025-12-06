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
    
    private DatabaseReference getWishlistRef() {
        if (context == null) {
            return null;
        }
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String uid = firebaseUser.getUid();
            return FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        }
        
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
            holder.binding.offPercentTxt.setText(item.getOffPercent() + " Giảm");
        }
        holder.binding.oldPriceTxt.setPaintFlags(holder.binding.oldPriceTxt.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if (item.getPicUrl() != null && !item.getPicUrl().isEmpty()) {
            RequestOptions options = new RequestOptions();
            options = options.transform(new CenterInside());
            Glide.with(context)
                    .load(item.getPicUrl().get(0))
                    .apply(options)
                    .into(holder.binding.pic);
        }

        holder.itemView.setTag(item.getTitle());

        checkFavoriteStatus(item, holder);

        holder.binding.favBtn.setOnClickListener(null);
        holder.binding.favBtn.setOnClickListener(v -> {
            try {
                if (context == null) {
                    android.util.Log.e("PopularAdapter", "Context is null in favBtn click");
                    return;
                }
                
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
                if (context != null) {
                    Toast.makeText(context, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

        // Add to Cart button click
        holder.binding.addToCartBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION || adapterPosition >= items.size()) {
                return;
            }
            
            ItemsModel clickedItem = items.get(adapterPosition);
            if (clickedItem != null) {
                addToCart(clickedItem);
            }
        });
    }

    private void addToCart(ItemsModel item) {
        if (context == null || item == null) return;
        
        DatabaseReference cartRef = getCartRef();
        if (cartRef == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String itemKey = item.getTitle().replaceAll("[.#$\\[\\]]", "_");
        
        cartRef.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Item already in cart, increase quantity
                    Long currentQty = snapshot.child("quantity").getValue(Long.class);
                    int newQty = (currentQty != null ? currentQty.intValue() : 0) + 1;
                    cartRef.child(itemKey).child("quantity").setValue(newQty);
                    Toast.makeText(context, "Đã cập nhật số lượng trong giỏ hàng", Toast.LENGTH_SHORT).show();
                } else {
                    // Add new item to cart
                    java.util.HashMap<String, Object> cartItem = new java.util.HashMap<>();
                    cartItem.put("title", item.getTitle());
                    cartItem.put("price", item.getPrice());
                    cartItem.put("oldPrice", item.getOldPrice());
                    cartItem.put("picUrl", item.getPicUrl());
                    cartItem.put("quantity", 1);
                    cartItem.put("selectedColor", item.getColor() != null && !item.getColor().isEmpty() ? item.getColor().get(0) : "");
                    cartItem.put("selectedSize", item.getSize() != null && !item.getSize().isEmpty() ? item.getSize().get(0) : "");
                    
                    cartRef.child(itemKey).setValue(cartItem);
                    Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private DatabaseReference getCartRef() {
        if (context == null) return null;
        
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid()).child("cart");
        }
        
        try {
            UserPreferences userPreferences = new UserPreferences(context);
            String uid = userPreferences.getUserId();
            if (uid != null && !uid.isEmpty()) {
                return FirebaseDatabase.getInstance().getReference("Users").child(uid).child("cart");
            }
        } catch (Exception e) {
            android.util.Log.e("PopularAdapter", "Error getting cart ref", e);
        }
        
        return null;
    }

    private void checkFavoriteStatus(ItemsModel item, ViewHolder holder) {
        if (item == null || item.getTitle() == null || item.getTitle().isEmpty()) {
            if (holder.binding != null && holder.binding.favBtn != null) {
                holder.binding.favBtn.setImageResource(R.drawable.heart);
            }
            return;
        }

        String itemTitle = item.getTitle();

        DatabaseReference wishlistRef = getWishlistRef();
        if (wishlistRef == null) {
            if (holder.binding != null && holder.binding.favBtn != null) {
                holder.binding.favBtn.setImageResource(R.drawable.heart);
            }
            return;
        }

        try {
            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        
                        if (holder.itemView.getTag() == null || !itemTitle.equals(holder.itemView.getTag())) {
                            return;
                        }
                        
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
            
            holder.listener = listener;
            holder.listenerRef = wishlistRef;
            
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
        if (context == null) {
            android.util.Log.e("PopularAdapter", "Context is null in toggleFavorite");
            return;
        }
        
        if (holder == null || holder.binding == null) {
            android.util.Log.e("PopularAdapter", "Holder or binding is null in toggleFavorite");
            return;
        }

        DatabaseReference wishlistRef = getWishlistRef();
        if (wishlistRef == null) {
            Toast.makeText(context, "Vui lòng đăng nhập để thêm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        if (item == null || item.getTitle() == null || item.getTitle().isEmpty()) {
            Toast.makeText(context, "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            wishlistRef.orderByChild("title").equalTo(item.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                            return;
                        }
                        
                        if (holder.itemView.getTag() == null || !item.getTitle().equals(holder.itemView.getTag())) {
                            return;
                        }
                        
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                child.getRef().removeValue().addOnCompleteListener(task -> {
                                    updateUIOnMainThread(() -> {
                                        try {
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
                                                    Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                if (context != null) {
                                                    Toast.makeText(context, "Không thể xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
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
                            wishlistRef.push().setValue(item).addOnSuccessListener(unused -> {
                                updateUIOnMainThread(() -> {
                                    try {
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
                                            Toast.makeText(context, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception ex) {
                                        android.util.Log.e("PopularAdapter", "Error in add success callback", ex);
                                    }
                                });
                            }).addOnFailureListener(e -> {
                                updateUIOnMainThread(() -> {
                                    try {
                                        if (context != null) {
                                            Toast.makeText(context, "Không thể thêm yêu thích: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                                Toast.makeText(context, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    updateUIOnMainThread(() -> {
                        try {
                            if (context != null) {
                                Toast.makeText(context, "Không thể cập nhật yêu thích: " + error.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "Đã xảy ra lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        holder.listener = null;
        holder.listenerRef = null;
        holder.itemView.setTag(null);
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
