package com.example.onlineshop.Activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.onlineshop.Adapter.ColorAdapter;
import com.example.onlineshop.Adapter.PicListAdapter;
import com.example.onlineshop.Adapter.SizeAdapter;
import com.example.onlineshop.Domain.ItemsModel;
import com.example.onlineshop.Helper.ManagmentCart;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.databinding.ActivityDetailBinding;
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
    private DatabaseReference wishlistRef;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        managmentCart = new ManagmentCart(this);
        initWishlist();
        getBundle();
        initPicList();
        initColor();
        initSize();
    }

    private void initColor() {
        binding.recyclerColor.setAdapter(new ColorAdapter(object.getColor()));
        binding.recyclerColor.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,true));
    }

    private void initSize() {
        binding.recyclerSize.setAdapter(new SizeAdapter(object.getSize()));
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
            object.setNumberinCart(numberOrder);
            managmentCart.insertItem(object);
            Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
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

