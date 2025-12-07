package com.example.onlineshop.Activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Adapter.ColorAdapter;
import com.example.onlineshop.Adapter.PicListAdapter;
import com.example.onlineshop.Adapter.SizeAdapter;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.Respository.CartRepository;
import com.example.onlineshop.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private ItemsModel object;
    private int numberOrder = 1;
    private ManagmentCart managmentCart;
    private CartRepository cartRepository;
    private DatabaseReference wishlistRef;
    private boolean isFavorite = false;
    private ColorAdapter colorAdapter;
    private SizeAdapter sizeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        cartRepository = new CartRepository();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        } else {
            UserPreferences userPreferences = new UserPreferences(this);
            uid = userPreferences.getUserId();
        }
        if (uid != null && !uid.isEmpty()) {
            cartRepository.setUserId(uid);
        }

        initWishlist();
        getBundle();
        initPicList();
        initColor();
        initSize();
        updateCartBadge();
    }

    private void initColor() {
        colorAdapter = new ColorAdapter(object.getColor());
        binding.recyclerColor.setAdapter(colorAdapter);
        binding.recyclerColor.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initSize() {
        sizeAdapter = new SizeAdapter(object.getSize());
        binding.recyclerSize.setAdapter(sizeAdapter);
        binding.recyclerSize.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initPicList() {
        ArrayList<String> picList = new ArrayList<>(object.getPicUrl());
        Glide.with(this)
                .load(picList.get(0))
                .into(binding.pic);
        binding.picList.setAdapter(new PicListAdapter(picList, binding.pic));
        binding.picList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void initWishlist() {
        UserPreferences userPreferences = new UserPreferences(this);
        String uid = userPreferences.getUserId();
        if (uid != null) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        }
    }

    private void getBundle() {
        object = (ItemsModel) getIntent().getSerializableExtra("object");
        if (object == null) {
            finish();
            return;
        }

        binding.titleTxt.setText(object.getTitle());
        
        String discountPrice = "$" + String.format("%.2f", object.getPrice());
        String originalPrice = "$" + String.format("%.2f", object.getOldPrice());
        binding.discountPriceTxt.setText(discountPrice);
        binding.originalPriceTxt.setText(originalPrice);
        
        binding.originalPriceTxt.setPaintFlags(binding.originalPriceTxt.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        
        String ratingText = String.format("%.1f (%d Đánh Giá)", object.getRating(), object.getReview());
        binding.ratingTxt.setText(ratingText);
        
        binding.descriptionTxt.setText(object.getDescription());
        binding.numberItemTxt.setText(String.valueOf(numberOrder));

        binding.minusBtn.setOnClickListener(v -> {
            if (numberOrder > 1) {
                numberOrder--;
                binding.numberItemTxt.setText(String.valueOf(numberOrder));
            }
        });

        binding.plusBtn.setOnClickListener(v -> {
            numberOrder++;
            binding.numberItemTxt.setText(String.valueOf(numberOrder));
        });

        binding.readMoreTxt.setOnClickListener(v -> {
            if (binding.descriptionTxt.getMaxLines() == 3) {
                binding.descriptionTxt.setMaxLines(Integer.MAX_VALUE);
                binding.readMoreTxt.setText("Thu Gọn");
            } else {
                binding.descriptionTxt.setMaxLines(3);
                binding.readMoreTxt.setText("Đọc Thêm");
            }
        });

        binding.addToCartBtn.setOnClickListener(v -> {
            if (object == null) return;
            String selectedColor = colorAdapter != null ? colorAdapter.getSelectedColor() : null;
            String selectedSize = sizeAdapter != null ? sizeAdapter.getSelectedSize() : null;

            ItemsModel cartItem = new ItemsModel();
            cartItem.setTitle(object.getTitle());
            cartItem.setDescription(object.getDescription());
            cartItem.setOffPercent(object.getOffPercent());
            cartItem.setPicUrl(object.getPicUrl());
            cartItem.setPrice(object.getPrice());
            cartItem.setOldPrice(object.getOldPrice());
            cartItem.setReview(object.getReview());
            cartItem.setRating(object.getRating());
            cartItem.setNumberinCart(numberOrder);

            java.util.ArrayList<String> colorList = new java.util.ArrayList<>();
            if (selectedColor != null) {
                colorList.add(selectedColor);
            } else if (object.getColor() != null && !object.getColor().isEmpty()) {
                colorList.add(object.getColor().get(0));
            }
            cartItem.setColor(colorList);

            java.util.ArrayList<String> sizeList = new java.util.ArrayList<>();
            if (selectedSize != null) {
                sizeList.add(selectedSize);
            } else if (object.getSize() != null && !object.getSize().isEmpty()) {
                sizeList.add(object.getSize().get(0));
            }
            cartItem.setSize(sizeList);

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null && cartRepository != null && cartRepository.isUserLoggedIn()) {
                cartRepository.addToCart(cartItem, numberOrder, new CartRepository.OnCartOperationListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(DetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        updateCartBadge();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(DetailActivity.this, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                managmentCart.insertItem(cartItem);
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                updateCartBadge();
            }
        });

        binding.backBtn.setOnClickListener(v -> {
            finish();
            overridePendingTransition(com.example.onlineshop.R.anim.slide_in_left, com.example.onlineshop.R.anim.slide_out_right);
        });

        binding.cartIconBtn.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(DetailActivity.this, com.example.onlineshop.Activity.MainContainerActivity.class);
            intent.putExtra("select_cart", true);
            startActivity(intent);
            finish();
            overridePendingTransition(com.example.onlineshop.R.anim.slide_in_right, com.example.onlineshop.R.anim.slide_out_left);
        });

        checkIsFavorite();
    }

    private void updateCartBadge() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser != null ? firebaseUser.getUid() : null;

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
        ManagmentCart managmentCart = new ManagmentCart(this);
        int itemCount = managmentCart.getListCart().size();
        updateBadgeUI(itemCount);
    }

    private void updateBadgeUI(int count) {
        if (binding != null) {
            if (count > 0) {
                binding.cartBadge.setVisibility(android.view.View.VISIBLE);
                binding.cartBadge.setText(String.valueOf(count > 99 ? "99+" : count));
            } else {
                binding.cartBadge.setVisibility(android.view.View.GONE);
            }
        }
    }

    private void checkIsFavorite() {
        if (wishlistRef == null || object == null || object.getTitle() == null) {
            updateFavIcon();
            return;
        }

        wishlistRef.orderByChild("title").equalTo(object.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                isFavorite = snapshot.exists();
                updateFavIcon();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                updateFavIcon();
            }
        });
    }

    private void toggleFavorite() {
        if (wishlistRef == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        if (object == null || object.getTitle() == null) return;

        if (isFavorite) {
            wishlistRef.orderByChild("title").equalTo(object.getTitle()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue();
                    }
                    isFavorite = false;
                    updateFavIcon();
                    Toast.makeText(DetailActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        } else {
            wishlistRef.push().setValue(object).addOnSuccessListener(unused -> {
                isFavorite = true;
                updateFavIcon();
                Toast.makeText(DetailActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void updateFavIcon() {
    }
}

