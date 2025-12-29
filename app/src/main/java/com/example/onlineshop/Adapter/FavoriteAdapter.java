package com.example.onlineshop.Adapter;

import android.content.Context;
import android.content.Intent;
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
import com.example.onlineshop.Model.ItemsModel;
import com.example.onlineshop.Helper.UserPreferences;
import com.example.onlineshop.databinding.ViewholderFavoriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final ArrayList<ItemsModel> items;
    private final Context context;
    private final DatabaseReference wishlistRef;

    public FavoriteAdapter(ArrayList<ItemsModel> items, Context context) {
        this.items = items;
        this.context = context;

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        
        if (firebaseUser != null) {
            uid = firebaseUser.getUid();
        } else {
            UserPreferences userPreferences = new UserPreferences(context);
            uid = userPreferences.getUserId();
        }
        
        if (uid != null && !uid.isEmpty()) {
            wishlistRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("wishlist");
        } else {
            wishlistRef = null;
        }
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewholderFavoriteBinding binding = ViewholderFavoriteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        
        ItemsModel item = items.get(position);
        if (item == null || holder.binding == null) {
            return;
        }

        try {
            holder.binding.titleTxt.setText(item.getTitle() != null ? item.getTitle() : "");
            holder.binding.priceTxt.setText(String.format("%.2f", item.getPrice()) + "₫");

            RequestOptions options = new RequestOptions().transform(new CenterInside());
            if (item.getPicUrl() != null && !item.getPicUrl().isEmpty() && item.getPicUrl().get(0) != null) {
                Glide.with(context)
                        .load(item.getPicUrl().get(0))
                        .apply(options)
                        .into(holder.binding.pic);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context == null || item == null) {
                        return;
                    }
                    try {
                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("object", item);
                        context.startActivity(intent);
                        if (context instanceof android.app.Activity) {
                            ((android.app.Activity) context).overridePendingTransition(com.example.onlineshop.R.anim.slide_in_right, com.example.onlineshop.R.anim.slide_out_left);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("FavoriteAdapter", "Error opening detail", e);
                    }
                }
            });

            holder.binding.favBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (context == null || item == null) {
                        return;
                    }
                    try {
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition == RecyclerView.NO_POSITION) {
                            return;
                        }

                        if (wishlistRef == null) {
                            Toast.makeText(context, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        removeFromWishlist(item, adapterPosition);
                    } catch (Exception e) {
                        android.util.Log.e("FavoriteAdapter", "Error in fav button click", e);
                        Toast.makeText(context, "Đã xảy ra lỗi", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("FavoriteAdapter", "Error binding view", e);
        }
    }

    private void removeFromWishlist(ItemsModel item, int position) {
        if (wishlistRef == null) {
            Toast.makeText(context, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
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
                    if (!snapshot.exists()) {
                        Toast.makeText(context, "Không tìm thấy sản phẩm trong yêu thích", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    for (DataSnapshot child : snapshot.getChildren()) {
                        child.getRef().removeValue().addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Không thể xóa khỏi yêu thích: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Không thể cập nhật yêu thích: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewholderFavoriteBinding binding;

        public ViewHolder(ViewholderFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
