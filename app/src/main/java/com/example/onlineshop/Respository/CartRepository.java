package com.example.onlineshop.Respository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineshop.Model.ItemsModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference cartRef;
    private String userId;
    private ValueEventListener cartListener;
    private MutableLiveData<ArrayList<ItemsModel>> cartLiveData;

    public interface OnCartOperationListener {
        void onSuccess();
        void onFailure(String error);
    }

    public CartRepository() {
        cartLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public void setUserId(String userId) {
        if (this.userId != null && cartListener != null && cartRef != null) {
            cartRef.removeEventListener(cartListener);
        }
        
        this.userId = userId;
        if (userId != null && !userId.isEmpty()) {
            cartRef = firebaseDatabase.getReference("Users").child(userId).child("cart");
            Log.d(TAG, "Cart reference set for user: " + userId);
        } else {
            cartRef = null;
        }
    }

    public String getUserId() {
        return userId;
    }

    public boolean isUserLoggedIn() {
        return userId != null && !userId.isEmpty() && cartRef != null;
    }

    public LiveData<ArrayList<ItemsModel>> loadCart() {
        if (cartRef == null) {
            cartLiveData.setValue(new ArrayList<>());
            return cartLiveData;
        }

        cartListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<ItemsModel> cartItems = new ArrayList<>();
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    try {
                        ItemsModel item = childSnapshot.getValue(ItemsModel.class);
                        if (item != null) {
                            Integer quantity = childSnapshot.child("quantity").getValue(Integer.class);
                            if (quantity != null && quantity > 0) {
                                item.setNumberinCart(quantity);
                            } else if (item.getNumberinCart() <= 0) {
                                item.setNumberinCart(1);
                            }

                            if (item.getColor() == null || item.getColor().isEmpty()) {
                                String legacyColor = childSnapshot.child("selectedColor").getValue(String.class);
                                if (legacyColor != null && !legacyColor.isEmpty()) {
                                    ArrayList<String> colors = new ArrayList<>();
                                    colors.add(legacyColor);
                                    item.setColor(colors);
                                }
                            }

                            if (item.getSize() == null || item.getSize().isEmpty()) {
                                String legacySize = childSnapshot.child("selectedSize").getValue(String.class);
                                if (legacySize != null && !legacySize.isEmpty()) {
                                    ArrayList<String> sizes = new ArrayList<>();
                                    sizes.add(legacySize);
                                    item.setSize(sizes);
                                }
                            }

                            cartItems.add(item);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cart item: " + e.getMessage());
                    }
                }
                Log.d(TAG, "Loaded " + cartItems.size() + " items from Firebase cart");
                cartLiveData.setValue(cartItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load cart: " + error.getMessage());
            }
        };

        cartRef.addValueEventListener(cartListener);
        return cartLiveData;
    }

    public void addToCart(ItemsModel item, int quantity, OnCartOperationListener listener) {
        if (cartRef == null || item == null) {
            if (listener != null) listener.onFailure("Invalid operation");
            return;
        }

        String cartItemId = generateCartItemId(item.getTitle(), item.getColor(), item.getSize());
        
        cartRef.child(cartItemId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("itemId", cartItemId);
                cartItem.put("title", item.getTitle());
                cartItem.put("description", item.getDescription());
                cartItem.put("price", item.getPrice());
                cartItem.put("oldPrice", item.getOldPrice());
                cartItem.put("offPercent", item.getOffPercent());
                cartItem.put("picUrl", item.getPicUrl());
                cartItem.put("size", item.getSize());
                cartItem.put("color", item.getColor());
                cartItem.put("rating", item.getRating());
                cartItem.put("review", item.getReview());
                cartItem.put("updatedAt", System.currentTimeMillis());

                int newQuantity = quantity;
                if (snapshot.exists()) {
                    Integer existingQty = snapshot.child("quantity").getValue(Integer.class);
                    if (existingQty != null) {
                        newQuantity = existingQty + quantity;
                    }
                    cartItem.put("addedAt", snapshot.child("addedAt").getValue(Long.class));
                } else {
                    cartItem.put("addedAt", System.currentTimeMillis());
                }
                
                cartItem.put("quantity", Math.min(newQuantity, 50));

                cartRef.child(cartItemId).setValue(cartItem)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Added to cart: " + item.getTitle());
                        if (listener != null) listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add to cart: " + e.getMessage());
                        if (listener != null) listener.onFailure(e.getMessage());
                    });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) listener.onFailure(error.getMessage());
            }
        });
    }

    public void updateQuantity(ItemsModel item, int newQuantity, OnCartOperationListener listener) {
        if (cartRef == null) {
            if (listener != null) listener.onFailure("Not logged in");
            return;
        }

        if (item == null) {
            if (listener != null) listener.onFailure("Invalid item");
            return;
        }

        if (newQuantity <= 0) {
            removeFromCart(item, listener);
            return;
        }

        String cartItemId = generateCartItemId(item.getTitle(), item.getColor(), item.getSize());

        Map<String, Object> updates = new HashMap<>();
        updates.put("quantity", Math.min(newQuantity, 50));
        updates.put("updatedAt", System.currentTimeMillis());

        cartRef.child(cartItemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            if (listener != null) listener.onFailure("Item not found in cart");
                            return;
                        }

                        snapshot.getRef().updateChildren(updates);
                        if (listener != null) listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) listener.onFailure(error.getMessage());
                    }
                });
    }

    public void removeFromCart(ItemsModel item, OnCartOperationListener listener) {
        if (cartRef == null) {
            if (listener != null) listener.onFailure("Not logged in");
            return;
        }

        if (item == null) {
            if (listener != null) listener.onFailure("Invalid item");
            return;
        }

        String cartItemId = generateCartItemId(item.getTitle(), item.getColor(), item.getSize());

        cartRef.child(cartItemId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            if (listener != null) listener.onFailure("Item not found in cart");
                            return;
                        }

                        snapshot.getRef().removeValue();
                        Log.d(TAG, "Removed from cart by id: " + cartItemId);
                        if (listener != null) listener.onSuccess();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (listener != null) listener.onFailure(error.getMessage());
                    }
                });
    }

    public void clearCart(OnCartOperationListener listener) {
        if (cartRef == null) {
            if (listener != null) listener.onFailure("Not logged in");
            return;
        }

        cartRef.removeValue()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Cart cleared");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    public void syncLocalCartToFirebase(ArrayList<ItemsModel> localCart, OnCartOperationListener listener) {
        if (cartRef == null || localCart == null || localCart.isEmpty()) {
            if (listener != null) listener.onSuccess();
            return;
        }

        Map<String, Object> cartData = new HashMap<>();
        long now = System.currentTimeMillis();

        for (ItemsModel item : localCart) {
            String cartItemId = generateCartItemId(item.getTitle(), item.getColor(), item.getSize());
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("itemId", cartItemId);
            cartItem.put("title", item.getTitle());
            cartItem.put("description", item.getDescription());
            cartItem.put("price", item.getPrice());
            cartItem.put("oldPrice", item.getOldPrice());
            cartItem.put("offPercent", item.getOffPercent());
            cartItem.put("picUrl", item.getPicUrl());
            cartItem.put("size", item.getSize());
            cartItem.put("color", item.getColor());
            cartItem.put("rating", item.getRating());
            cartItem.put("review", item.getReview());
            cartItem.put("quantity", Math.max(1, item.getNumberinCart()));
            cartItem.put("addedAt", now);
            cartItem.put("updatedAt", now);
            
            cartData.put(cartItemId, cartItem);
        }

        cartRef.updateChildren(cartData)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Synced " + localCart.size() + " items to Firebase");
                if (listener != null) listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to sync cart: " + e.getMessage());
                if (listener != null) listener.onFailure(e.getMessage());
            });
    }

    private String generateCartItemId(String title, java.util.List<String> colors, java.util.List<String> sizes) {
        StringBuilder key = new StringBuilder();
        if (title != null) {
            key.append(title);
        }
        if (colors != null && !colors.isEmpty()) {
            key.append("_").append(colors.get(0));
        }
        if (sizes != null && !sizes.isEmpty()) {
            key.append("_").append(sizes.get(0));
        }

        String raw = key.toString();
        if (raw.isEmpty()) return "unknown_item";

        return raw.toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    public void removeListener() {
        if (cartRef != null && cartListener != null) {
            cartRef.removeEventListener(cartListener);
        }
    }
}
